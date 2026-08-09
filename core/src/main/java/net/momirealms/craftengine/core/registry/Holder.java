package net.momirealms.craftengine.core.registry;

import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public interface Holder<T> {

    static <T> Holder<T> direct(T value) {
        return new Direct<>(value);
    }

    T value();

    boolean isBound();

    boolean matchesKey(Key id);

    boolean matchesKey(ResourceKey<T> key);

    boolean matchesPredicate(Predicate<ResourceKey<T>> predicate);

    Optional<ResourceKey<T>> keyOptional();

    HolderKind kind();

    boolean serializableIn(Owner<T> owner);

    default String registeredName() {
        return this.keyOptional().map(key -> key.location().toString()).orElse("[unregistered]");
    }

    enum HolderKind {
        REFERENCE,
        DIRECT
    }

    interface Owner<T> {
        default boolean canSerializeIn(Owner<T> other) {
            return other == this;
        }
    }

    record Direct<T>(T value) implements Holder<T> {
        @Override
        public boolean isBound() {
            return true;
        }

        @Override
        public boolean matchesKey(Key id) {
            return false;
        }

        @Override
        public boolean matchesKey(ResourceKey<T> key) {
            return false;
        }

        @Override
        public boolean matchesPredicate(Predicate<ResourceKey<T>> predicate) {
            return false;
        }

        @Override
        public Optional<ResourceKey<T>> keyOptional() {
            return Optional.empty();
        }

        @Override
        public HolderKind kind() {
            return HolderKind.DIRECT;
        }

        @Override
        public boolean serializableIn(Owner<T> owner) {
            return true;
        }

        @Override
        public @NotNull String toString() {
            return "Direct{" + this.value + "}";
        }
    }

    class Reference<T> implements Holder<T> {
        private final Owner<T> owner;
        @Nullable
        private ResourceKey<T> key;
        @Nullable
        private T value;

        public Reference(Owner<T> owner, @Nullable ResourceKey<T> key, @Nullable T value) {
            this.owner = owner;
            this.key = key;
            this.value = value;
        }

        public static <T> Reference<T> create(Owner<T> owner, ResourceKey<T> registryKey) {
            return new Reference<>(owner, registryKey, null);
        }

        public static <T> Reference<T> createConstant(Owner<T> owner, ResourceKey<T> registryKey, T value) {
            return new Constant<>(owner, registryKey, value);
        }

        public ResourceKey<T> key() {
            if (this.key == null) {
                throw new IllegalStateException("Trying to access unbound value '" + this.value + "' from registry " + this.owner);
            }
            return this.key;
        }

        @Override
        public T value() {
            if (this.value == null) {
                throw new IllegalStateException("Trying to access unbound value '" + this.key + "' from registry " + this.owner);
            }
            return this.value;
        }

        @Override
        public boolean matchesKey(Key id) {
            return this.key().location().equals(id);
        }

        @Override
        public boolean matchesKey(ResourceKey<T> key) {
            return this.key() == key;
        }

        @Override
        public boolean matchesPredicate(Predicate<ResourceKey<T>> predicate) {
            return predicate.test(this.key());
        }

        @Override
        public boolean serializableIn(Owner<T> owner) {
            return this.owner.canSerializeIn(owner);
        }

        @Override
        public Optional<ResourceKey<T>> keyOptional() {
            return Optional.of(this.key());
        }

        @Override
        public HolderKind kind() {
            return HolderKind.REFERENCE;
        }

        @Override
        public boolean isBound() {
            return this.key != null && this.value != null;
        }

        public void bindKey(ResourceKey<T> registryKey) {
            if (this.key != null && registryKey != this.key) {
                throw new IllegalStateException("Can't change holder key: existing=" + this.key + ", new=" + registryKey);
            }
            this.key = registryKey;
        }

        public void bindValue(T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Reference{" + this.key + "=" + this.value + "}";
        }

        static class Constant<A> extends Reference<A> {

            public Constant(Owner<A> owner, @Nullable ResourceKey<A> key, @Nullable A value) {
                super(owner, key, value);
            }

            @Override
            public void bindValue(A value) {
                throw new UnsupportedOperationException();
            }
        }
    }
}
