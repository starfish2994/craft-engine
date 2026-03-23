package net.momirealms.craftengine.core.pack.host.impl;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.momirealms.craftengine.core.pack.host.*;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.HashUtils;
import net.momirealms.craftengine.core.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class OneDriveHost implements ResourcePackHost {
    public static final ResourcePackHostFactory<OneDriveHost> FACTORY = new Factory();
    private final String clientId;
    private final String clientSecret;
    private final ProxySelector proxy;
    private final String uploadPath;
    private Tuple<@NotNull String, @NotNull String, @NotNull Date> refreshToken;
    private String sha1;
    private String fileId;

    public OneDriveHost(String clientId,
                        String clientSecret,
                        String refreshToken,
                        String uploadPath,
                        ProxySelector proxy) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.proxy = proxy;
        this.uploadPath = uploadPath;
        this.refreshToken = Tuple.of(refreshToken, "", new Date());
        readCacheFromDisk();
    }

    @Override
    public boolean canUpload() {
        return true;
    }

    @Override
    public ResourcePackHostType<OneDriveHost> type() {
        return ResourcePackHosts.ONEDRIVE;
    }

    public void readCacheFromDisk() {
        Path cachePath = CraftEngine.instance().dataFolderPath().resolve("cache").resolve("onedrive.json");
        if (!Files.exists(cachePath) || !Files.isRegularFile(cachePath)) return;
        try (InputStream is = Files.newInputStream(cachePath)) {
            Map<String, String> cache = GsonHelper.get().fromJson(
                    new InputStreamReader(is, StandardCharsets.UTF_8),
                    new TypeToken<Map<String, String>>(){}.getType()
            );

            this.refreshToken = Tuple.of(
                    Objects.requireNonNull(cache.get("refresh-token")),
                    Objects.requireNonNull(cache.get("access-token")),
                    new Date(Long.parseLong(cache.get("refresh-token-expires-in"))));
            this.sha1 = cache.get("sha1");
            this.fileId = cache.get("file-id");

            CraftEngine.instance().logger().info(TranslationManager.instance().plainTranslation("host.onedrive.a"));
        } catch (Exception e) {
            CraftEngine.instance().logger().warn(TranslationManager.instance().plainTranslation("host.onedrive.b", cachePath.toString()), e);
        }
    }

    public void saveCacheToDisk() {
        Map<String, String> cache = new HashMap<>();
        cache.put("refresh-token", this.refreshToken.left());
        cache.put("access-token", this.refreshToken.mid());
        cache.put("refresh-token-expires-in", String.valueOf(this.refreshToken.right().getTime()));
        cache.put("sha1", this.sha1);
        cache.put("file-id", this.fileId);
        Path cachePath = CraftEngine.instance().dataFolderPath().resolve("cache").resolve("onedrive.json");
        try {
            Files.createDirectories(cachePath.getParent());
            Files.writeString(
                    cachePath,
                    GsonHelper.get().toJson(cache),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            CraftEngine.instance().logger().warn(TranslationManager.instance().plainTranslation("host.onedrive.c"), e);
        }
    }

    @Override
    public CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player) {
        CompletableFuture<List<ResourcePackDownloadData>> future = new CompletableFuture<>();
        CraftEngine.instance().scheduler().executeAsync(() -> {
            try (HttpClient client = HttpClient.newBuilder().proxy(this.proxy).build()) {
                String accessToken = getOrRefreshJwtToken();
                saveCacheToDisk();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://graph.microsoft.com/v1.0/drive/items/" + this.fileId))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", "application/octet-stream")
                        .GET()
                        .build();
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() != 200) {
                                CraftEngine.instance().logger().severe(TranslationManager.instance().plainTranslation("host.onedrive.d", String.valueOf(response.statusCode()), response.body()));
                                future.completeExceptionally(new IOException(TranslationManager.instance().plainTranslation("host.onedrive.e", String.valueOf(response.statusCode()), response.body())));
                                return;
                            }
                            String downloadUrl = GsonHelper.parseJsonToJsonObject(response.body()).get("@microsoft.graph.downloadUrl").getAsString();
                            future.complete(List.of(new ResourcePackDownloadData(
                                    downloadUrl,
                                    UUID.nameUUIDFromBytes(this.sha1.getBytes(StandardCharsets.UTF_8)),
                                    this.sha1
                            )));
                        })
                        .exceptionally(ex -> {
                            CraftEngine.instance().logger().severe(TranslationManager.instance().plainTranslation("host.onedrive.f"), ex);
                            future.completeExceptionally(ex);
                            return null;
                        });
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> upload(Path resourcePackPath) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        CraftEngine.instance().scheduler().executeAsync(() -> {
            this.sha1 = HashUtils.calculateLocalFileSha1(resourcePackPath);
            String accessToken = getOrRefreshJwtToken();
            try (HttpClient client = HttpClient.newBuilder().proxy(this.proxy).build()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://graph.microsoft.com/v1.0/drive/root:/" + this.uploadPath + ":/content"))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", "application/octet-stream")
                        .PUT(HttpRequest.BodyPublishers.ofFile(resourcePackPath))
                        .build();
                long uploadStart = System.currentTimeMillis();
                CraftEngine.instance().logger().info(TranslationManager.instance().plainTranslation("host.onedrive.g"));
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            long elapsedTime = System.currentTimeMillis() - uploadStart;
                            if (response.statusCode() == 200 || response.statusCode() == 201) {
                                CraftEngine.instance().logger().info(TranslationManager.instance().plainTranslation("host.onedrive.h", String.valueOf(elapsedTime)));
                                this.fileId = GsonHelper.parseJsonToJsonObject(response.body()).get("id").getAsString();
                                saveCacheToDisk();
                                future.complete(null);
                            } else {
                                CraftEngine.instance().logger().severe(TranslationManager.instance().plainTranslation("host.onedrive.i", String.valueOf(elapsedTime), response.body()));
                                future.completeExceptionally(new RuntimeException(TranslationManager.instance().plainTranslation("host.onedrive.e", String.valueOf(response.statusCode()), response.body())));
                            }
                        })
                        .exceptionally(ex -> {
                            CraftEngine.instance().logger().severe(TranslationManager.instance().plainTranslation("host.onedrive.j"), ex);
                            future.completeExceptionally(ex);
                            return null;
                        });
            } catch (FileNotFoundException e) {
                CraftEngine.instance().logger().warn(TranslationManager.instance().plainTranslation("host.onedrive.k"), e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private String getOrRefreshJwtToken() {
        if (this.refreshToken == null || this.refreshToken.mid().isEmpty() || this.refreshToken.right().before(new Date())) {
            try (HttpClient client = HttpClient.newBuilder().proxy(this.proxy).build()) {
                String formData = "client_id=" + URLEncoder.encode(this.clientId, StandardCharsets.UTF_8) +
                        "&client_secret=" + URLEncoder.encode(Objects.requireNonNull(this.clientSecret), StandardCharsets.UTF_8) +
                        "&refresh_token=" + URLEncoder.encode(this.refreshToken.left(), StandardCharsets.UTF_8) +
                        "&grant_type=refresh_token" +
                        "&scope=Files.ReadWrite.All+offline_access";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://login.microsoftonline.com/common/oauth2/v2.0/token"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(formData))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    CraftEngine.instance().logger().severe(TranslationManager.instance().plainTranslation("host.onedrive.l", String.valueOf(response.statusCode()), response.body()));
                    return this.refreshToken != null ? this.refreshToken.mid() : "";
                }

                JsonObject jsonData = GsonHelper.parseJsonToJsonObject(response.body());
                if (jsonData.has("error")) {
                    CraftEngine.instance().logger().warn(TranslationManager.instance().plainTranslation("host.onedrive.m", jsonData.toString()));
                    throw new RuntimeException(TranslationManager.instance().plainTranslation("host.onedrive.n", jsonData.toString()));
                }
                long expiresInMillis = jsonData.get("expires_in").getAsInt() * 1000L;
                this.refreshToken = Tuple.of(
                        jsonData.get("refresh_token").getAsString(),
                        jsonData.get("access_token").getAsString(),
                        new Date(System.currentTimeMillis() + expiresInMillis - 10_000)
                );
            } catch (IOException | InterruptedException e) {
                CraftEngine.instance().logger().severe(TranslationManager.instance().plainTranslation("host.onedrive.o"), e);
                throw new RuntimeException(TranslationManager.instance().plainTranslation("host.onedrive.p"), e);
            }
        }

        return this.refreshToken.mid();
    }

    private static class Factory implements ResourcePackHostFactory<OneDriveHost> {
        private static final String[] USE_ENVIRONMENT_VARIABLES = new String[] {"use_environment_variables", "use-environment-variables"};
        private static final String[] CLIENT_ID = new String[] {"client_id", "client-id"};
        private static final String[] CLIENT_SECRET = new String[] {"client_secret", "client-secret"};
        private static final String[] REFRESH_TOKEN = new String[] {"refresh_token", "refresh-token"};
        private static final String[] UPLOAD_PATH = new String[] {"upload_path", "upload-path"};

        @Override
        public OneDriveHost create(ConfigSection section) {
            boolean useEnv = section.getBoolean(USE_ENVIRONMENT_VARIABLES);
            String clientId = useEnv ? getNonNullEnvironmentVariable(section, "CE_ONEDRIVE_CLIENT_ID") : section.getNonEmptyString(CLIENT_ID);
            String clientSecret = useEnv ? getNonNullEnvironmentVariable(section, "CE_ONEDRIVE_CLIENT_SECRET") : section.getNonEmptyString(CLIENT_SECRET);
            String refreshToken = useEnv ? getNonNullEnvironmentVariable(section, "CE_ONEDRIVE_REFRESH_TOKEN") : section.getNonEmptyString(REFRESH_TOKEN);
            String uploadPath = section.getString(UPLOAD_PATH, "resource_pack.zip");
            return new OneDriveHost(clientId, clientSecret, refreshToken, uploadPath, getProxySelector(section.getSection("proxy")));
        }
    }
}