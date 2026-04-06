package net.momirealms.craftengine.bukkit.item;

import net.momirealms.craftengine.bukkit.util.RegistryOps;
import net.momirealms.craftengine.core.item.ItemWrapper;
import net.momirealms.craftengine.proxy.minecraft.nbt.CompoundTagProxy;
import net.momirealms.craftengine.proxy.minecraft.nbt.TagProxy;
import net.momirealms.craftengine.proxy.minecraft.world.item.ItemStackProxy;
import net.momirealms.sparrow.nbt.Tag;
import org.bukkit.inventory.ItemStack;

public final class LegacyItemWrapper extends BukkitItemWrapper {
    public LegacyItemWrapper(Object itemStack) {
        super(itemStack);
    }

    public LegacyItemWrapper(ItemStack itemStack) {
        super(itemStack);
    }

    public boolean setTag(Object value, Object... path) {
        Object finalNMSTag;
        if (value instanceof Tag tag) {
            finalNMSTag = RegistryOps.SPARROW_NBT.convertTo(RegistryOps.NBT, tag);
        } else if (TagProxy.CLASS.isInstance(value)) {
            finalNMSTag = value;
        } else {
            finalNMSTag = RegistryOps.JAVA.convertTo(RegistryOps.NBT, value);
        }

        if (path == null || path.length == 0) {
            if (CompoundTagProxy.CLASS.isInstance(finalNMSTag)) {
                ItemStackProxy.INSTANCE.setTag(this.itemStack, finalNMSTag);
                return true;
            }
            return false;
        }

        Object currentTag = ItemStackProxy.INSTANCE.getOrCreateTag(this.itemStack);

        for (int i = 0; i < path.length - 1; i++) {
            Object pathSegment = path[i];
            if (pathSegment == null) return false;
            Object childTag = CompoundTagProxy.INSTANCE.get(currentTag, pathSegment.toString());
            if (!CompoundTagProxy.CLASS.isInstance(childTag)) {
                childTag = CompoundTagProxy.INSTANCE.newInstance();
                CompoundTagProxy.INSTANCE.put(currentTag, pathSegment.toString(), childTag);
            }
            currentTag = childTag;
        }

        String finalKey = path[path.length - 1].toString();
        CompoundTagProxy.INSTANCE.put(currentTag, finalKey, finalNMSTag);
        return true;
    }

    @SuppressWarnings("unchecked")
    public <V> V getJavaTag(Object... path) {
        Object tag = getExactTag(path);
        if (tag == null) return null;
        return (V) RegistryOps.NBT.convertTo(RegistryOps.JAVA, tag);
    }

    public Tag getNBTTag(Object... path) {
        Object tag = getExactTag(path);
        if (tag == null) return null;
        return RegistryOps.NBT.convertTo(RegistryOps.SPARROW_NBT, tag);
    }

    @SuppressWarnings("DuplicatedCode")
    public Object getExactTag(Object... path) {
        Object compoundTag = ItemStackProxy.INSTANCE.getTag(this.itemStack);
        if (compoundTag == null) return null;
        Object currentTag = compoundTag;
        if (path == null || path.length == 0) {
            return currentTag;
        }
        for (int i = 0; i < path.length; i++) {
            Object pathSegment = path[i];
            if (pathSegment == null) return null;
            currentTag = CompoundTagProxy.INSTANCE.get(currentTag, path[i].toString());
            if (currentTag == null) return null;
            if (i == path.length - 1) {
                return currentTag;
            }
            if (!CompoundTagProxy.CLASS.isInstance(currentTag)) {
                return null;
            }
        }
        return null;
    }

    public boolean remove(Object... path) {
        Object compoundTag = ItemStackProxy.INSTANCE.getTag(this.itemStack);
        if (compoundTag == null || path == null || path.length == 0) return false;

        if (path.length == 1) {
            String key = path[0].toString();
            if (CompoundTagProxy.INSTANCE.get(compoundTag, key) != null) {
                CompoundTagProxy.INSTANCE.remove(compoundTag, key);
                return true;
            }
        }

        Object currentTag = compoundTag;
        for (int i = 0; i < path.length - 1; i++) {
            Object pathSegment = path[i];
            if (pathSegment == null) return false;
            currentTag = CompoundTagProxy.INSTANCE.get(currentTag, path[i].toString());
            if (!CompoundTagProxy.CLASS.isInstance(currentTag)) {
                return false;
            }
        }

        String finalKey = path[path.length - 1].toString();
        if (CompoundTagProxy.INSTANCE.get(currentTag, finalKey) != null) {
            CompoundTagProxy.INSTANCE.remove(currentTag, finalKey);
            return true;
        }
        return false;
    }

    public boolean hasTag(Object... path) {
        return getExactTag(path) != null;
    }

    @Override
    public ItemWrapper copy() {
        return new LegacyItemWrapper(ItemStackProxy.INSTANCE.copy(this.itemStack));
    }

    @Override
    public ItemWrapper copyWithCount(int count) {
        return new LegacyItemWrapper(ItemStackProxy.INSTANCE.copyWithCount(this.itemStack, count));
    }
}