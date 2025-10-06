package net.momirealms.craftengine.core.pack.allocator.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface CacheFileStorage<A> {

    CompletableFuture<A> load();

    CompletableFuture<Void> save(A value);

    boolean needForceUpdate();

    static <A> LocalFileCacheStorage<A> local(Path path, CacheFileType<A> type) {
        return new LocalFileCacheStorage<>(path, type);
    }

    abstract class AbstractRemoteFileCacheStorage<A> implements CacheFileStorage<A> {

        @Override
        public boolean needForceUpdate() {
            return true;
        }
    }

    class LocalFileCacheStorage<A> implements CacheFileStorage<A> {
        private final CacheFileType<A> fileType;
        private final Path filePath;
        private long lastModified = 0L;

        public LocalFileCacheStorage(Path filePath, CacheFileType<A> type) {
            this.filePath = filePath;
            this.fileType = type;
            updateLastModified();
        }

        @Override
        public boolean needForceUpdate() {
            try {
                if (!Files.exists(this.filePath)) {
                    return this.lastModified != 0L; // 文件被删除了，需要更新
                }
                long currentModified = Files.getLastModifiedTime(this.filePath).toMillis();
                if (currentModified > this.lastModified) {
                    this.lastModified = currentModified;
                    return true; // 文件被修改了，需要强制更新
                }
                return false;
            } catch (IOException e) {
                // 如果无法读取文件信息，保守起见返回 true 强制更新
                return true;
            }
        }

        @Override
        public CompletableFuture<A> load() {
            if (!Files.exists(this.filePath)) {
                this.lastModified = 0L; // 重置最后修改时间
                return CompletableFuture.completedFuture(this.fileType.create());
            }
            try {
                A result = this.fileType.read(this.filePath);
                updateLastModified(); // 加载成功后更新最后修改时间
                return CompletableFuture.completedFuture(result);
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }
        }

        @Override
        public CompletableFuture<Void> save(A value) {
            try {
                this.fileType.write(this.filePath, value);
                updateLastModified(); // 保存成功后更新最后修改时间
                return CompletableFuture.completedFuture(null);
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }
        }

        /**
         * 更新最后修改时间
         */
        private void updateLastModified() {
            try {
                if (Files.exists(this.filePath)) {
                    this.lastModified = Files.getLastModifiedTime(filePath).toMillis();
                } else {
                    this.lastModified = 0L;
                }
            } catch (IOException e) {
                this.lastModified = 0L; // 出错时重置
            }
        }
    }
}
