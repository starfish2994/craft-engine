package net.momirealms.craftengine.core.item.processor;

import com.google.gson.JsonElement;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.component.DataComponentKeys;
import net.momirealms.craftengine.core.item.network.NetworkItemHandler;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.context.text.TextProvider;
import net.momirealms.craftengine.core.plugin.context.text.TextProviders;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.TagParser;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ComponentsProcessor implements ItemProcessor {
    public static final ItemProcessorFactory<ComponentsProcessor> FACTORY = new Factory();
    private final List<DynamicComponentProvider> arguments;
    private DynamicComponentProvider customData = null;

    public ComponentsProcessor(Map<String, Object> map) {
        List<DynamicComponentProvider> arguments = new ArrayList<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Key key = Key.of(entry.getKey());
            if (key.equals(DataComponentKeys.CUSTOM_DATA)) {
                this.customData = getProvider(key, entry.getValue());
            } else {
                arguments.add(getProvider(key, entry.getValue()));
            }
        }
        this.arguments = arguments;
    }

    public ComponentsProcessor(DynamicComponentProvider customData, List<DynamicComponentProvider> arguments) {
        this.customData = customData;
        this.arguments = arguments;
    }

    @Override
    public Item apply(Item item, ItemBuildContext context) {
        for (DynamicComponentProvider argument : arguments) {
            item.setSparrowTagComponent(argument.type, argument.function.apply(context));
        }
        if (this.customData != null) {
            CompoundTag tag = (CompoundTag) item.getSparrowTag(DataComponentKeys.CUSTOM_DATA);
            if (tag != null) {
                for (Map.Entry<String, Tag> entry : ((CompoundTag) this.customData.function.apply(context)).entrySet()) {
                    tag.put(entry.getKey(), entry.getValue());
                }
                item.setComponent(DataComponentKeys.CUSTOM_DATA, tag);
            } else {
                item.setComponent(DataComponentKeys.CUSTOM_DATA, this.customData.function.apply(context));
            }
        }
        return item;
    }

    @Override
    public Item prepareNetworkItem(Item item, ItemBuildContext context, CompoundTag networkData) {
        for (DynamicComponentProvider argument : this.arguments) {
            String componentType = argument.type.asString();
            Tag previous = item.getComponentAsSparrowTag(componentType);
            if (previous != null) {
                networkData.put(componentType, NetworkItemHandler.pack(NetworkItemHandler.Operation.ADD, previous));
            } else {
                networkData.put(componentType, NetworkItemHandler.pack(NetworkItemHandler.Operation.REMOVE));
            }
        }
        return item;
    }

    public record DynamicComponentProvider(Key type, Function<ItemBuildContext, Tag> function) {
    }

    // todo 未来需要支持普通yaml格式使用 <>，最好先重构textprovider与Item
    static DynamicComponentProvider getProvider(Key key, Object value) {
        if (value instanceof String stringValue) {
            if (stringValue.startsWith("(json) ")) {
                // todo 需要未来先 tokenized 后再判断，而不是使用 < > 作为依据
                if (stringValue.contains("<") && stringValue.contains(">")) {
                    TextProvider provider = TextProviders.fromString(stringValue.substring("(json) ".length()));
                    return new DynamicComponentProvider(key, c -> {
                        JsonElement element = GsonHelper.get().fromJson(provider.get(c), JsonElement.class);
                        return CraftEngine.instance().platform().jsonToSparrowNBT(element);
                    });
                } else {
                    JsonElement element = GsonHelper.get().fromJson(stringValue.substring("(json) ".length()), JsonElement.class);
                    Tag tag = CraftEngine.instance().platform().jsonToSparrowNBT(element);
                    return new DynamicComponentProvider(key, c -> tag);
                }
            } else if (stringValue.startsWith("(snbt) ")) {
                if (stringValue.contains("<") && stringValue.contains(">")) {
                    TextProvider provider = TextProviders.fromString(stringValue.substring("(snbt) ".length()));
                    return new DynamicComponentProvider(key, c -> TagParser.parseTagFully(provider.get(c)));
                } else {
                    Tag tag = TagParser.parseTagFully(stringValue.substring("(snbt) ".length()));
                    return new DynamicComponentProvider(key, c -> tag);
                }
            }
        }
        Tag tag = CraftEngine.instance().platform().javaToSparrowNBT(value);
        return new DynamicComponentProvider(key, c -> tag);
    }

    private static class Factory implements ItemProcessorFactory<ComponentsProcessor> {

        @Override
        public ComponentsProcessor create(ConfigValue value) {
            ConfigSection componentsSection = value.getAsSection();
            DynamicComponentProvider customData = null;
            List<DynamicComponentProvider> arguments = new ArrayList<>();
            for (Map.Entry<String, Object> componentEntry : componentsSection.values().entrySet()) {
                Key key = Key.of(componentEntry.getKey());
                Object componentValue = componentEntry.getValue();
                if (key.equals(DataComponentKeys.CUSTOM_DATA)) {
                    customData = getProvider(key, componentValue);
                } else {
                    arguments.add(getProvider(key, componentValue));
                }
            }
            return new ComponentsProcessor(customData, arguments);
        }
    }
}
