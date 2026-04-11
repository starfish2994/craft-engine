package net.momirealms.craftengine.core.pack.host;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ResourcePackHost {

    CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(NetWorkUser user);

    CompletableFuture<Void> upload(Path resourcePackPath);

    default CompletableFuture<ResourcePackResponseAction> response(NetWorkUser user, ResourcePackResponseAction action) {
        // 检查是否是拒绝
        if (Config.kickOnDeclined()) {
            if (action == ResourcePackResponseAction.DECLINED || action == ResourcePackResponseAction.DISCARDED) {
                user.kick(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                return CompletableFuture.completedFuture(action);
            }
        }
        // 检查是否失败
        if (Config.kickOnFailedApply()) {
            if (action == ResourcePackResponseAction.FAILED_DOWNLOAD || action == ResourcePackResponseAction.INVALID_URL) {
                user.kick(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                return CompletableFuture.completedFuture(action);
            }
        }
        return CompletableFuture.completedFuture(action);
    }

    boolean canUpload();

    ResourcePackHostType<? extends ResourcePackHost> type();
}
