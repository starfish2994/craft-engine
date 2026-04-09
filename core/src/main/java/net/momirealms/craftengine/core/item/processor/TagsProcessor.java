package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.component.DataComponentKeys;
import net.momirealms.craftengine.core.item.network.NetworkItemHandler;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.Tag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

// todo 更好支持参数
public final class TagsProcessor implements ItemProcessor {
    public static final ItemProcessorFactory<TagsProcessor> FACTORY = new Factory();
    private final Map<String, Object> arguments;

    public TagsProcessor(Map<String, Object> arguments) {
        this.arguments = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            if (entry.getKey().charAt(0) == '@') {
                this.arguments.put(entry.getKey().substring(1), entry.getValue());
            } else {
                if (entry.getValue() instanceof Map<?,?> innerMap) {
                    processTags(entry.getKey(), MiscUtils.castToMap(innerMap), this.arguments::put);
                } else {
                    this.arguments.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public Map<String, Object> tags() {
        return this.arguments;
    }

    private void processTags(String path, Map<String, Object> arguments, BiConsumer<String, Object> callback) {
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            if (entry.getKey().charAt(0) == '@') {
                callback.accept(path + "." + entry.getKey().substring(1), entry.getValue());
            } else {
                if (entry.getValue() instanceof Map<?,?> innerMap) {
                    processTags(path + "." + entry.getKey(), MiscUtils.castToMap(innerMap), callback);
                } else {
                    callback.accept(path + "." + entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    public Item apply(Item item, ItemBuildContext context) {
        for (Map.Entry<String, Object> entry : this.arguments.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String[] split = key.split("\\.");
            item.setTag(value, (Object[]) split);
        }
        return item;
    }

    @Override
    public Item prepareNetworkItem(Item item, ItemBuildContext context, CompoundTag networkData) {
        if (VersionHelper.isOrAbove1_20_5()) {
            Tag previous = item.getComponentAsSparrowTag(DataComponentKeys.CUSTOM_DATA);
            if (previous != null) {
                networkData.put(DataComponentKeys.CUSTOM_DATA.asString(), NetworkItemHandler.pack(NetworkItemHandler.Operation.ADD, previous));
            } else {
                networkData.put(DataComponentKeys.CUSTOM_DATA.asString(), NetworkItemHandler.pack(NetworkItemHandler.Operation.REMOVE));
            }
        } else {
            for (Map.Entry<String, Object> entry : this.arguments.entrySet()) {
                String key = entry.getKey();
                String[] split = key.split("\\.");
                Tag previous = item.getSparrowTag((Object[]) split);
                if (previous != null) {
                    networkData.put(entry.getKey(), NetworkItemHandler.pack(NetworkItemHandler.Operation.ADD, previous));
                } else {
                    networkData.put(entry.getKey(), NetworkItemHandler.pack(NetworkItemHandler.Operation.REMOVE));
                }
            }
        }
        return item;
    }

    private static class Factory implements ItemProcessorFactory<TagsProcessor> {

        @Override
        public TagsProcessor create(ConfigValue value) {
            return new TagsProcessor(value.getAsMap());
        }
    }
}
