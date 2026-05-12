package net.momirealms.craftengine.proxy.common.network.packet;

import io.netty.buffer.ByteBuf;
import net.momirealms.craftengine.proxy.common.network.protocol.ConnectionState;
import net.momirealms.craftengine.proxy.common.network.protocol.PacketSide;
import net.momirealms.craftengine.proxy.common.network.protocol.packettype.PacketTypeCommon;
import net.momirealms.craftengine.proxy.common.network.protocol.player.ClientVersion;
import net.momirealms.craftengine.proxy.common.util.Cancellable;
import net.momirealms.craftengine.proxy.common.util.ProxyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public final class PacketContext implements Cancellable {
    private final PacketSide side;
    private final ConnectionState state;
    private final ClientVersion clientVersion;
    private final int packetId;
    private final @Nullable PacketTypeCommon packetType;
    private final ProxyByteBuf payload;
    private final int payloadReaderIndex;
    private @Nullable ProxyByteBuf replacementPayload;
    private boolean cancelled;
    private boolean changed;

    public PacketContext(
            PacketSide side,
            ConnectionState state,
            ClientVersion clientVersion,
            int packetId,
            @Nullable PacketTypeCommon packetType,
            ProxyByteBuf payload,
            int payloadReaderIndex
    ) {
        this.side = Objects.requireNonNull(side, "side");
        this.state = Objects.requireNonNull(state, "state");
        this.clientVersion = Objects.requireNonNull(clientVersion, "clientVersion");
        this.packetId = packetId;
        this.packetType = packetType;
        this.payload = Objects.requireNonNull(payload, "payload");
        this.payloadReaderIndex = payloadReaderIndex;
    }

    public PacketSide side() {
        return this.side;
    }

    public ConnectionState state() {
        return this.state;
    }

    public ClientVersion clientVersion() {
        return this.clientVersion;
    }

    public int packetID() {
        return this.packetId;
    }

    @Nullable
    public PacketTypeCommon packetType() {
        return this.packetType;
    }

    public ProxyByteBuf payload() {
        this.payload.readerIndex(this.payloadReaderIndex);
        return this.payload;
    }

    public void rewritePayload(Consumer<ProxyByteBuf> init) {
        ProxyByteBuf replacement = new ProxyByteBuf(this.payload.source().alloc().buffer());
        boolean replaced = false;
        try {
            init.accept(replacement);
            if (this.replacementPayload != null && this.replacementPayload != replacement) {
                this.replacementPayload.release();
            }
            this.replacementPayload = replacement;
            this.replacementPayload.readerIndex(0);
            this.changed = true;
            replaced = true;
        } finally {
            if (!replaced) {
                replacement.release();
            }
        }
    }

    @Nullable
    public ByteBuf replacementPayloadSource() {
        return this.replacementPayload == null ? null : this.replacementPayload.source();
    }

    public void releaseReplacementPayload() {
        if (this.replacementPayload != null) {
            this.replacementPayload.release();
            this.replacementPayload = null;
        }
    }

    public boolean changed() {
        return this.changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
