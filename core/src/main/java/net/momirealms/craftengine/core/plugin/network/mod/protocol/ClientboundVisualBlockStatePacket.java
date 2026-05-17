package net.momirealms.craftengine.core.plugin.network.mod.protocol;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.plugin.network.mod.ClientCustomPacket;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

public record ClientboundVisualBlockStatePacket(int[] data) implements ClientCustomPacket {
    public static final Key ID = Key.ce("visual_block_state");
    public static final NetworkCodec<FriendlyByteBuf, ClientboundVisualBlockStatePacket> CODEC = ClientCustomPacket.codec(
            (packet, buf) -> buf.writeVarIntArray(packet.data),
            buf -> new ClientboundVisualBlockStatePacket(buf.readVarIntArray())
    );

    public static ClientboundVisualBlockStatePacket create() {
        int vanillaBlockStateCount = CraftEngine.instance().blockManager().vanillaBlockStateCount();
        int serverSideBlockCount = Config.serverSideBlocks();
        int[] mappings = new int[serverSideBlockCount];
        for (int i = 0; i < serverSideBlockCount; i++) {
            ImmutableBlockState state = CraftEngine.instance().blockManager().getImmutableBlockStateUnsafe(i + vanillaBlockStateCount);
            if (state.isEmpty()) continue;
            mappings[state.customBlockState().registryId() - vanillaBlockStateCount] = state.visualBlockState().registryId();
        }
        return new ClientboundVisualBlockStatePacket(mappings);
    }

    @Override
    public Key id() {
        return ID;
    }

    @Override
    public NetworkCodec<FriendlyByteBuf, ClientboundVisualBlockStatePacket> codec() {
        return CODEC;
    }
}
