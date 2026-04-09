package net.momirealms.craftengine.bukkit.entity.furniture.behavior;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.BlockKeys;
import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.block.UpdateFlags;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureDefinition;
import net.momirealms.craftengine.core.entity.furniture.FurnitureLightData;
import net.momirealms.craftengine.core.entity.furniture.FurnitureVariant;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorFactory;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorTemplate;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureController;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GlowingFurnitureBehaviorTemplate extends FurnitureBehaviorTemplate {
    public static final FurnitureBehaviorFactory<GlowingFurnitureBehaviorTemplate> FACTORY = new Factory();
    public static final Key PAYLOAD_ID = Key.ce("light");
    public static final Object[] LIGHT_BLOCK_STATES = new Object[16];
    public static final Object[] WATERLOGGED_LIGHT_BLOCK_STATES = new Object[16];
    public static final int[] LIGHT_BLOCK_STATES_ID = new int[16];
    public static final int[] WATERLOGGED_LIGHT_BLOCK_STATES_ID = new int[16];
    public static final int AIR_BLOCK_STATE_ID;
    public static final int WATER_BLOCK_STATE_ID;
    public static final Map<UUID, FurnitureLightData> LIGHT_DATA = new ConcurrentHashMap<>();

    static {
        LIGHT_BLOCK_STATES[0] = BlockStateUtils.blockDataToBlockState(Bukkit.createBlockData("minecraft:air"));
        WATERLOGGED_LIGHT_BLOCK_STATES[0] = BlockStateUtils.blockDataToBlockState(Bukkit.createBlockData("minecraft:water"));
        for (int i = 1; i < 16; i++) {
            LIGHT_BLOCK_STATES[i] = BlockStateUtils.blockDataToBlockState(Bukkit.createBlockData("minecraft:light[level=" + i + "]"));
            LIGHT_BLOCK_STATES_ID[i] = BlockStateUtils.blockStateToId(LIGHT_BLOCK_STATES[i]);
            WATERLOGGED_LIGHT_BLOCK_STATES[i] = BlockStateUtils.blockDataToBlockState(Bukkit.createBlockData("minecraft:light[level=" + i + ",waterlogged=true]"));
            WATERLOGGED_LIGHT_BLOCK_STATES_ID[i] = BlockStateUtils.blockStateToId(WATERLOGGED_LIGHT_BLOCK_STATES[i]);
        }
        AIR_BLOCK_STATE_ID = BlockStateUtils.blockStateToId(LIGHT_BLOCK_STATES[0]);
        LIGHT_BLOCK_STATES_ID[0] = AIR_BLOCK_STATE_ID;
        WATER_BLOCK_STATE_ID = BlockStateUtils.blockStateToId(WATERLOGGED_LIGHT_BLOCK_STATES[0]);
        WATERLOGGED_LIGHT_BLOCK_STATES_ID[0] = WATER_BLOCK_STATE_ID;
    }

    @NotNull
    public final Map<String, List<LightData>> lightDataByVariant;
    public final List<LightData> defaultLightData;

    private GlowingFurnitureBehaviorTemplate(FurnitureDefinition furniture,
                                             @NotNull Map<String, List<LightData>> lightDataByVariant,
                                             List<LightData> defaultLightData
    ) {
        super(furniture);
        this.lightDataByVariant = lightDataByVariant;
        this.defaultLightData = defaultLightData;
    }

    @Override
    public FurnitureController createController(Furniture furniture) {
        return new GlowingFurnitureController(furniture, this);
    }

    public List<LightData> getLightDataByVariant(final String variant) {
        return this.lightDataByVariant.getOrDefault(variant, this.defaultLightData);
    }

    // 行为处理器
    public static final class GlowingFurnitureController extends FurnitureController {
        private final GlowingFurnitureBehaviorTemplate behavior;
        private boolean unloaded = false;

        public GlowingFurnitureController(Furniture furniture, GlowingFurnitureBehaviorTemplate behavior) {
            super(furniture);
            this.behavior = behavior;
        }

        // 变更变体时, 刷新关联的光源
        @Override
        public void onVariantChange(FurnitureVariant previousVariant) {
            List<LightData> oldLightData = this.behavior.getLightDataByVariant(previousVariant.name());
            List<LightData> lightData = this.behavior.getLightDataByVariant(furniture.getCurrentVariant().name());
            if (oldLightData.isEmpty() && lightData.isEmpty()) return; // 都没有配置, 不处理.
            // 获取变化
            MiscUtils.DiffResult<LightData> diffResult = MiscUtils.diff(oldLightData, lightData);
            List<LightData> added = diffResult.added();
            List<LightData> removed = diffResult.removed();

            FurnitureLightData realLightData = getOrCreateLightData();

            for (int i = 0; i < added.size(); i++) {
                LightData addedData = added.get(i);
                BlockPos blockPos = BlockPos.fromVec3d(super.furniture.getRelativePosition(addedData.relative));
                for (Player player : furniture.getTrackedBy()) {
                    int newLight = player.furnitureLightData().addLightData(blockPos, addedData.light());
                    if (newLight != -1) {
                        this.updateLightBlock(player, blockPos, newLight);
                    }
                }
                int newLight = realLightData.addLightData(blockPos, addedData.light());
                if (newLight != -1) {
                    this.updateServerLightBlock(blockPos, newLight);
                }
            }
            for (int i = 0; i < removed.size(); i++) {
                LightData removeData = removed.get(i);
                BlockPos blockPos = BlockPos.fromVec3d(super.furniture.getRelativePosition(removeData.relative));
                for (Player player : super.furniture.getTrackedBy()) {
                    int newLight = player.furnitureLightData().removeLightData(blockPos, removeData.light());
                    if (newLight != -1) {
                        this.updateLightBlock(player, blockPos, newLight);
                    }
                }
                int newLight = realLightData.removeLightData(blockPos, removeData.light());
                if (newLight != -1) {
                    this.updateServerLightBlock(blockPos, newLight);
                }
            }
        }

        @Override
        public void preRemove(@Nullable Player player) {
            this.removeLightBlock(true);
            this.unloaded = true;
        }

        @Override
        public void onLoad() {
            this.unloaded = false;
            this.setLightBlock();
        }

        @Override
        public void onUnload() {
            if (!this.unloaded) {
                this.removeLightBlock(false);
            }
        }

        private void setLightBlock() {
            List<LightData> lightData = this.behavior.getLightDataByVariant(super.furniture.currentVariant().name());
            if (!lightData.isEmpty()) {
                FurnitureLightData realLightData = getOrCreateLightData();
                for (int i = 0; i < lightData.size(); i++) {
                    LightData addData = lightData.get(i);
                    BlockPos blockPos = BlockPos.fromVec3d(super.furniture.getRelativePosition(addData.relative));
                    int changed = realLightData.addLightData(blockPos, addData.light());
                    if (changed != -1) {
                        updateServerLightBlock(blockPos, changed);
                    }
                }
            }
        }

        private void removeLightBlock(boolean remove) {
            List<LightData> lightData = this.behavior.getLightDataByVariant(super.furniture.currentVariant().name());
            if (!lightData.isEmpty()) {
                FurnitureLightData realLightData = getOrCreateLightData();
                for (int i = 0; i < lightData.size(); i++) {
                    LightData removeData = lightData.get(i);
                    BlockPos blockPos = BlockPos.fromVec3d(super.furniture.getRelativePosition(removeData.relative));
                    int changed = realLightData.removeLightData(blockPos, removeData.light());
                    if (changed != -1 && remove) {
                        updateServerLightBlock(blockPos, changed);
                    }
                }
            }
        }

        private void updateServerLightBlock(BlockPos blockPos, int level) {
            BlockStateWrapper blockState = super.furniture.world().getBlockState(blockPos);
            int stateId = blockState.registryId();
            if (stateId == AIR_BLOCK_STATE_ID) {
                Object targetState = LIGHT_BLOCK_STATES[level];
                super.furniture.world().setBlockState(blockPos, BlockStateUtils.toBlockStateWrapper(targetState), UpdateFlags.UPDATE_ALL);
            } else if (stateId == WATER_BLOCK_STATE_ID) {
                Object targetState = WATERLOGGED_LIGHT_BLOCK_STATES[level];
                super.furniture.world().setBlockState(blockPos, BlockStateUtils.toBlockStateWrapper(targetState), UpdateFlags.UPDATE_ALL);
            } else {
                Key blockId = blockState.ownerId();
                if (blockId.equals(BlockKeys.LIGHT)) {
                    if (level == 0) {
                        boolean waterlogged = blockState.getProperty("waterlogged");
                        if (waterlogged) {
                            super.furniture.world().setBlockState(blockPos, BlockStateUtils.toBlockStateWrapper(WATERLOGGED_LIGHT_BLOCK_STATES[0]), UpdateFlags.UPDATE_ALL);
                        } else {
                            super.furniture.world().setBlockState(blockPos, BlockStateUtils.toBlockStateWrapper(LIGHT_BLOCK_STATES[0]), UpdateFlags.UPDATE_ALL);
                        }
                    } else {
                        super.furniture.world().setBlockState(blockPos, blockState.withProperty("level", String.valueOf(level)), UpdateFlags.UPDATE_ALL);
                    }
                }
            }
        }

        private FurnitureLightData getOrCreateLightData() {
            UUID uuid = super.furniture.world().uuid();
            return LIGHT_DATA.computeIfAbsent(uuid, k -> new FurnitureLightData());
        }

        @Override
        public void onAsyncPlayerTrack(Player player) {
            List<LightData> lightData = this.behavior.lightDataByVariant.get(furniture.getCurrentVariant().name());
            if (lightData == null || lightData.isEmpty()) return;
            for (int i = 0; i < lightData.size(); i++) {
                LightData addData = lightData.get(i);
                BlockPos blockPos = BlockPos.fromVec3d(super.furniture.getRelativePosition(addData.relative));
                int newLight = player.furnitureLightData().addLightData(blockPos, addData.light());
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
                int newLight = player.furnitureLightData().removeLightData(blockPos, removeData.light());
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
            if (!Config.enableFurnitureLightSystem()) {
                throw new IllegalStateException("'furniture.light-system.enable' is not enabled in config.yml");
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
                    List<LightData> lightData = variantsSection.getList(variantName, this::parseLightData);
                    lightDataByVariant.put(variantName, lightData);
                }
            }
            return new GlowingFurnitureBehaviorTemplate(furniture, lightDataByVariant, section.getList("lights", this::parseLightData));
        }

        private LightData parseLightData(ConfigValue v) {
            if (v.is(Map.class)) {
                ConfigSection s = v.getAsSection();
                Vector3f position = s.getVector3f("position", ConfigConstants.ZERO_VECTOR3);
                int light = s.getValue("level", a -> a.getAsInt(1, 15), 15);
                return new LightData(position, light);
            } else {
                ConfigValue[] split = v.splitValues(" ", 2);
                if (split.length == 1) {
                    return new LightData(split[0].getAsVector3f(), 15);
                } else {
                    return new LightData(split[0].getAsVector3f(), split[1].getAsInt(1, 15));
                }
            }
        }
    }

    // 光源数据
    public record LightData (
            Vector3f relative,
            int light
    ) { }
}
