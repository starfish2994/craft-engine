package net.momirealms.craftengine.bukkit.plugin.network.payload.protocol;


import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.NetworkReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.paper.PaperReflections;
import net.momirealms.craftengine.bukkit.util.RegistryUtils;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.network.ModPacket;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.*;
import org.bukkit.entity.Player;

public record ClientCustomBlockPacket(int size) implements ModPacket {
    public static final ResourceKey<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>> TYPE = ResourceKey.create(
            BuiltInRegistries.MOD_PACKET.key().location(), Key.of("craftengine", "client_custom_block")
    );
    public static final NetworkCodec<FriendlyByteBuf, ClientCustomBlockPacket> CODEC = ModPacket.codec(
            ClientCustomBlockPacket::encode,
            ClientCustomBlockPacket::new
    );

    private ClientCustomBlockPacket(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.size);
    }

    @Override
    public ResourceKey<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>> type() {
        return TYPE;
    }

    @Override
    public void handle(NetWorkUser user) {
        if (user.clientModEnabled()) return; // 防止滥用
        int serverBlockRegistrySize = RegistryUtils.currentBlockRegistrySize();
        if (this.size != serverBlockRegistrySize) {
            user.kick(Component.translatable(
                    "disconnect.craftengine.block_registry_mismatch",
                    TranslationArgument.numeric(this.size),
                    TranslationArgument.numeric(serverBlockRegistrySize)
            ));
            return;
        }
        user.setClientModState(true);
        user.setClientBlockList(new IntIdentityList(this.size));
        if (!VersionHelper.isOrAbove1_20_2()) {
            // 因为旧版本没有配置阶段需要重新发送区块
            try {
                Object chunkLoader = PaperReflections.field$ServerPlayer$chunkLoader.get(user.serverPlayer());
                LongOpenHashSet sentChunks = (LongOpenHashSet) PaperReflections.field$RegionizedPlayerChunkLoader$PlayerChunkLoaderData$sentChunks.get(chunkLoader);
                Object serverLevel = FastNMS.INSTANCE.field$CraftWorld$ServerLevel(((Player) user.platformPlayer()).getWorld());
                Object lightEngine = CoreReflections.method$BlockAndTintGetter$getLightEngine.invoke(serverLevel);
                Object chunkSource = FastNMS.INSTANCE.method$ServerLevel$getChunkSource(serverLevel);
                for (long chunkPos : sentChunks) {
                    int chunkX = (int) chunkPos;
                    int chunkZ = (int) (chunkPos >> 32);
                    Object levelChunk = FastNMS.INSTANCE.method$ServerChunkCache$getChunk(chunkSource, chunkX, chunkZ, false);
                    Object packet = NetworkReflections.constructor$ClientboundLevelChunkWithLightPacket.newInstance(levelChunk, lightEngine, null, null);
                    user.sendPacket(packet, true);
                }
            } catch (Exception e) {
                CraftEngine.instance().logger().warn("Failed to refresh chunk for player " + user.name(), e);
            }
        }
    }

}
