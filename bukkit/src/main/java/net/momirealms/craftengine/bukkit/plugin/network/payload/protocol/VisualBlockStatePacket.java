package net.momirealms.craftengine.bukkit.plugin.network.payload.protocol;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.network.ModPacket;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

public record VisualBlockStatePacket(int[] data) implements ModPacket {
    public static final ResourceKey<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>> TYPE = ResourceKey.create(
            BuiltInRegistries.MOD_PACKET.key().location(), Key.of("craftengine", "visual_block_state")
    );
    public static final NetworkCodec<FriendlyByteBuf, VisualBlockStatePacket> CODEC = ModPacket.codec(
            VisualBlockStatePacket::encode,
            VisualBlockStatePacket::new
    );

    private VisualBlockStatePacket(FriendlyByteBuf buf) {
        this(buf.readVarIntArray());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeVarIntArray(this.data);
    }

    @Override
    public ResourceKey<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>> type() {
        return TYPE;
    }

    public static VisualBlockStatePacket create() {
        int vanillaBlockStateCount = BlockStateUtils.vanillaBlockStateCount();
        int serverSideBlockCount = Config.serverSideBlocks();
        int[] mappings = new int[serverSideBlockCount];
        for (int i = 0; i < serverSideBlockCount; i++) {
            ImmutableBlockState state = BukkitBlockManager.instance().getImmutableBlockStateUnsafe(i + vanillaBlockStateCount);
            if (state.isEmpty()) continue;
            mappings[state.customBlockState().registryId() - vanillaBlockStateCount] = state.vanillaBlockState().registryId();
        }
        return new VisualBlockStatePacket(mappings);
    }

}
