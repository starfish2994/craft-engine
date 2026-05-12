package net.momirealms.craftengine.proxy.common.network;

import io.netty.channel.Channel;
import net.momirealms.craftengine.proxy.common.network.protocol.ConnectionState;
import net.momirealms.craftengine.proxy.common.network.protocol.player.ClientVersion;
import net.momirealms.craftengine.proxy.common.platform.ProxyPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class ChannelConnection implements ProtocolStateHolder  {
    private final Channel channel;
    private volatile @Nullable ProxyPlayer player; // 登录完成后绑定的玩家
    private volatile int protocolVersion = -1; // handshake 读取到的原始协议号
    private volatile ClientVersion clientVersion = ClientVersion.UNKNOWN; // 由协议号映射出的客户端版本
    private volatile ConnectionState decoderState = ConnectionState.HANDSHAKING; // 客户端到服务端方向状态
    private volatile ConnectionState encoderState = ConnectionState.HANDSHAKING; // 服务端到客户端方向状态

    public ChannelConnection(Channel channel) {
        this.channel = channel;
    }

    public Channel channel() {
        return this.channel;
    }

    // 返回已绑定的玩家, BungeeCord 登录流程未完成时为 null
    @Nullable
    public ProxyPlayer player() {
        return this.player;
    }

    // 绑定平台玩家身份, 协议状态仍由 ChannelConnection 持有.
    public void bind(ProxyPlayer player) {
        this.player = player;
    }

    // 当 BungeeCord 上报匹配的断开连接事件时解除玩家绑定
    public void unbind(UUID uuid) {
        ProxyPlayer current = this.player;
        if (current != null && Objects.equals(current.uuid(), uuid)) {
            this.player = null;
        }
    }

    @Override
    public ClientVersion clientVersion() {
        return this.clientVersion;
    }

    @Override
    public int protocolVersion() {
        return this.protocolVersion;
    }

    @Override
    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
        this.clientVersion = protocolVersion < 0 ? ClientVersion.UNKNOWN : ClientVersion.getById(protocolVersion);
    }

    @Override
    public void setConnectionState(ConnectionState connectionState) {
        ConnectionState state = Objects.requireNonNull(connectionState, "connectionState");
        this.decoderState = state;
        this.encoderState = state;
    }

    @Override
    public ConnectionState decoderState() {
        return this.decoderState;
    }

    @Override
    public ConnectionState encoderState() {
        return this.encoderState;
    }

    @Override
    public void setDecoderState(ConnectionState decoderState) {
        this.decoderState = Objects.requireNonNull(decoderState, "decoderState");
    }

    @Override
    public void setEncoderState(ConnectionState encoderState) {
        this.encoderState = Objects.requireNonNull(encoderState, "encoderState");
    }
}
