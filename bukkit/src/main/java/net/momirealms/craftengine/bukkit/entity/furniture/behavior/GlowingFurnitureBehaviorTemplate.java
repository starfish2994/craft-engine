package net.momirealms.craftengine.bukkit.entity.furniture.behavior;

import net.momirealms.craftengine.bukkit.util.DiffUtil;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureDefinition;
import net.momirealms.craftengine.core.entity.furniture.FurnitureVariant;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorFactory;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorTemplate;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureController;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundBlockUpdatePacketProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.BlocksProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.state.BlockStateProxy;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.momirealms.craftengine.bukkit.util.BlockStateUtils.LIGHT_BLOCK_STATES;
import static net.momirealms.craftengine.bukkit.util.BlockStateUtils.WATERLOGGED_LIGHT_BLOCK_STATES;

public final class GlowingFurnitureBehaviorTemplate extends FurnitureBehaviorTemplate {
    public static final FurnitureBehaviorFactory<GlowingFurnitureBehaviorTemplate> FACTORY = new Factory();

    @NotNull
    public final Map<String, List<LightData>> lightDataByVariant;

    private GlowingFurnitureBehaviorTemplate(FurnitureDefinition furniture,
                                             @NotNull Map<String, List<LightData>> lightDataByVariant
    ) {
        super(furniture);
        this.lightDataByVariant = lightDataByVariant;
    }

    @Override
    public FurnitureController createController(Furniture furniture) {
        return new GlowingFurnitureController(furniture, this);
    }

    // 行为处理器
    public static final class GlowingFurnitureController extends FurnitureController {
        private final GlowingFurnitureBehaviorTemplate behavior;

        public GlowingFurnitureController(Furniture furniture, GlowingFurnitureBehaviorTemplate behavior) {
            super(furniture);
            this.behavior = behavior;
        }

        // 变更变体时, 刷新关联的光源
        @Override
        public void onVariantChange(FurnitureVariant previousVariant) {
            List<LightData> oldLightData = this.behavior.lightDataByVariant.get(previousVariant.name());
            List<LightData> lightData = this.behavior.lightDataByVariant.get(furniture.getCurrentVariant().name());
            if ((oldLightData == null || oldLightData.isEmpty()) && lightData == null || lightData.isEmpty()) return; // 都没有配置, 不处理.
            // 获取变化
            DiffUtil.DiffResult<LightData> diffResult = DiffUtil.diff(oldLightData, lightData);
            List<LightData> added = diffResult.added();
            List<LightData> removed = diffResult.removed();
            for (int i = 0; i < added.size(); i++) {
                LightData addedData = added.get(i);
                BlockPos blockPos = BlockPos.fromVec3d(super.furniture.getRelativePosition(addedData.relative));
                for (Player player : furniture.getTrackedBy()) {
                    int newLight = player.addLightData(blockPos, addedData.light());
                    if (newLight != -1) {
                        this.updateLightBlock(player, blockPos, newLight);
                    }
                }
            }
            for (int i = 0; i < removed.size(); i++) {
                LightData removeData = added.get(i);
                BlockPos blockPos = BlockPos.fromVec3d(super.furniture.getRelativePosition(removeData.relative));
                for (Player player : furniture.getTrackedBy()) {
                    int newLight = player.removeLightData(blockPos, removeData.light());
                    if (newLight != -1) {
                        this.updateLightBlock(player, blockPos, newLight);
                    }
                }
            }
        }

        // 追踪到时, 展示关联的光源
        @Override
        public void onPlayerTrack(Player player) {
            List<LightData> lightData = this.behavior.lightDataByVariant.get(furniture.getCurrentVariant().name());
            if (lightData == null || lightData.isEmpty()) return;
            for (int i = 0; i < lightData.size(); i++) {
                LightData addData = lightData.get(i);
                BlockPos blockPos = BlockPos.fromVec3d(super.furniture.getRelativePosition(addData.relative));
                int newLight = player.addLightData(blockPos, addData.light());
                if (newLight != -1) {
                    this.updateLightBlock(player, blockPos, newLight);
                }
            }
        }

        // 离开时, 移除关联的光源
        @Override
        public void onPlayerUntrack(Player player) {
            List<LightData> lightData = this.behavior.lightDataByVariant.get(furniture.getCurrentVariant().name());
            if (lightData == null || lightData.isEmpty()) return;
            for (int i = 0; i < lightData.size(); i++) {
                LightData removeData = lightData.get(i);
                BlockPos blockPos = BlockPos.fromVec3d(super.furniture.getRelativePosition(removeData.relative));
                int newLight = player.removeLightData(blockPos, removeData.light());
                if (newLight != -1) {
                    this.updateLightBlock(player, blockPos, newLight);
                }
            }
        }

        // 更新光照方块
        private void updateLightBlock(Player player, BlockPos blockPos, int lightPower) {
            Object targetBlock = BlockStateProxy.INSTANCE.getBlock(furniture.world().getBlock(blockPos).blockState().literalObject());
            boolean waterlogged = targetBlock == BlocksProxy.WATER;
            if (targetBlock == BlocksProxy.AIR || targetBlock == BlocksProxy.LIGHT || waterlogged) {
                Object pos = LocationUtils.toBlockPos(blockPos);
                Object blockState = waterlogged ? WATERLOGGED_LIGHT_BLOCK_STATES[lightPower] : LIGHT_BLOCK_STATES[lightPower];
                Object packet = ClientboundBlockUpdatePacketProxy.INSTANCE.newInstance$0(pos, blockState);
                player.sendPacket(packet, false);
            }
        }
    }

    // 工厂类
    private static class Factory implements FurnitureBehaviorFactory<GlowingFurnitureBehaviorTemplate> {
        @Override
        public GlowingFurnitureBehaviorTemplate create(FurnitureDefinition furniture, ConfigSection section) {
            // 如果没有配置变体灯光展示规则
            ConfigSection variantsSection = section.getSection("variants");
            Map<String, List<LightData>> lightDataByVariant;
            if (variantsSection == null) {
                lightDataByVariant = Map.of();
            } else {
                // 读取变体展示规则
                lightDataByVariant = new HashMap<>();
                for (String variantName : variantsSection.keySet()) {
                    List<LightData> lightData = variantsSection.getSectionList(variantName, s -> {
                        Vector3f position = s.getVector3f("position", ConfigConstants.ZERO_VECTOR3);
                        int light = s.getValue("light", v -> v.getAsInt(1, 15), 15);
                        return new LightData(position, light);
                    });
                    lightDataByVariant.put(variantName, lightData);
                }
            }
            return new GlowingFurnitureBehaviorTemplate(furniture, lightDataByVariant);
        }
    }

    // 光源数据
    public record LightData (
            Vector3f relative,
            int light
    ) { }
}
