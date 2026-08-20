package net.momirealms.craftengine.core.world.particle;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.LazyReference;

import java.util.HashMap;
import java.util.Map;

public final class ParticleDataTypes {
    public static final Map<Key, java.util.function.Function<ConfigSection, ParticleData>> TYPES = new HashMap<>();
    private static final String[] BLOCK_STATE = ConfigKeys.of("blockstate|block_state");
    private static final String[] TARGET_X = ConfigKeys.of("target_x");
    private static final String[] TARGET_Y = ConfigKeys.of("target_y");
    private static final String[] TARGET_Z = ConfigKeys.of("target_z");
    private static final String[] ARRIVAL_TIME = ConfigKeys.of("arrival_time");
    private static final String[] WATER_BLOCKS = ConfigKeys.of("blocks|water_blocks");
    private static final String[] BURST_IMPULSE_BASE = ConfigKeys.of("base|burst_impulse_base");
    private static final String[] ROLL = ConfigKeys.of("roll|charge");

    static {
        registerParticleData(section -> {
                    final String blockState = section.getNonNullString(BLOCK_STATE);
                    return new BlockStateData(LazyReference.untilNotNull(() -> CraftEngine.instance().blockManager().createBlockState(blockState)));
                },
                ParticleTypes.BLOCK, ParticleTypes.FALLING_DUST, ParticleTypes.DUST_PILLAR, ParticleTypes.BLOCK_CRUMBLE, ParticleTypes.BLOCK_MARKER);
        registerParticleData(section -> new ColorData(
                        section.getNonNullValue("color", ConfigConstants.ARGUMENT_COLOR).getAsColor()
                ),
                ParticleTypes.ENTITY_EFFECT, ParticleTypes.TINTED_LEAVES);
        registerParticleData(section -> new SculkChargeData(
                        section.getFloat(ROLL)
                ),
                ParticleTypes.SCULK_CHARGE);
        registerParticleData(section -> new GeyserData(
                        section.getInt(WATER_BLOCKS)
                ),
                ParticleTypes.GEYSER, ParticleTypes.GEYSER_PLUME);
        registerParticleData(section -> new GeyserBaseData(
                        section.getInt(WATER_BLOCKS),
                        section.getFloat(BURST_IMPULSE_BASE)
                ),
                ParticleTypes.GEYSER_BASE, ParticleTypes.GEYSER_POOF);
        registerParticleData(section -> new ShriekData(
                        section.getInt("shriek")
                ),
                ParticleTypes.SHRIEK);
        registerParticleData(section -> new DustData(
                        section.getNonNullValue("color", ConfigConstants.ARGUMENT_COLOR).getAsColor(),
                        section.getFloat("scale", 1f)
                ),
                ParticleTypes.DUST);
        registerParticleData(section -> new DustTransitionData(
                        section.getNonNullValue("from", ConfigConstants.ARGUMENT_COLOR).getAsColor(),
                        section.getNonNullValue("to", ConfigConstants.ARGUMENT_COLOR).getAsColor(),
                        section.getFloat("scale", 1f)
                ),
                ParticleTypes.DUST_COLOR_TRANSITION);
        registerParticleData(section -> {
                    final Key itemId = section.getNonNullIdentifier("item");
                    return new ItemStackData(LazyReference.untilNotNull(() -> Item.byId(itemId)));
                },
                ParticleTypes.ITEM);
        registerParticleData(section -> new VibrationData(
                        section.getNumber(TARGET_X, ConfigConstants.CONSTANT_ZERO),
                        section.getNumber(TARGET_Y, ConfigConstants.CONSTANT_ZERO),
                        section.getNumber(TARGET_Z, ConfigConstants.CONSTANT_ZERO),
                        section.getNumber(ARRIVAL_TIME, ConfigConstants.CONSTANT_TEN)
                ),
                ParticleTypes.VIBRATION);
        registerParticleData(section -> new TrailData(
                        section.getNumber(TARGET_X, ConfigConstants.CONSTANT_ZERO),
                        section.getNumber(TARGET_Y, ConfigConstants.CONSTANT_ZERO),
                        section.getNumber(TARGET_Z, ConfigConstants.CONSTANT_ZERO),
                        section.getNonNullValue("color", ConfigConstants.ARGUMENT_COLOR).getAsColor(),
                        section.getNumber("duration", ConfigConstants.CONSTANT_TEN)
                ),
                ParticleTypes.TRAIL);
        registerParticleData(section -> new SpellParticleData(
                        section.getNonNullValue("color", ConfigConstants.ARGUMENT_COLOR).getAsColor(),
                        section.getFloat("power", 1f)
                ),
                ParticleTypes.SPELL);
    }

    public static void registerParticleData(java.util.function.Function<ConfigSection, ParticleData> function, Key... types) {
        for (Key type : types) {
            TYPES.put(type, function);
        }
    }
}
