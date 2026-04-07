package net.momirealms.craftengine.bukkit.entity.furniture.behavior;

import net.momirealms.craftengine.bukkit.util.DiffUtil;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureDefinition;
import net.momirealms.craftengine.core.entity.furniture.FurnitureVariant;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorFactory;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorTemplate;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureController;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GlowingFurnitureBehaviorTemplate extends FurnitureBehaviorTemplate {
    public static final FurnitureBehaviorFactory<GlowingFurnitureBehaviorTemplate> FACTORY = new Factory();
    public static final Key PAYLOAD_ID = Key.ce("light");

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

        @Override
        public void onAsyncPlayerTrack(Player player) {
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

        @Override
        public void onAsyncPlayerUntrack(Player player) {
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

        // 更新光照方块 TODO 传入多个, 最后转成 SectionBlockUpdate.
        private void updateLightBlock(Player player, BlockPos blockPos, int lightPower) {
            int x = blockPos.x();
            int y = blockPos.y();
            int z = blockPos.z();

            byte[] data = new byte[13];

            // x (int -> 4字节，大端序)
            data[0] = (byte) (x >> 24);
            data[1] = (byte) (x >> 16);
            data[2] = (byte) (x >> 8);
            data[3] = (byte) x;

            // y (int -> 4字节)
            data[4] = (byte) (y >> 24);
            data[5] = (byte) (y >> 16);
            data[6] = (byte) (y >> 8);
            data[7] = (byte) y;

            // z (int -> 4字节)
            data[8] = (byte) (z >> 24);
            data[9] = (byte) (z >> 16);
            data[10] = (byte) (z >> 8);
            data[11] = (byte) z;

            // lightPower (0~15，只占用低4位)
            data[12] = (byte) (lightPower & 0x0F);

            player.sendCustomPayload(PAYLOAD_ID, data);
        }
    }

    // 工厂类
    private static class Factory implements FurnitureBehaviorFactory<GlowingFurnitureBehaviorTemplate> {
        @Override
        public GlowingFurnitureBehaviorTemplate create(FurnitureDefinition furniture, ConfigSection section) {
            if (!Config.enableGlowingFurnitureBehavior()) {
                throw new IllegalStateException("GlowingFurnitureBehavior is not enabled!");
            }
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
