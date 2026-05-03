package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.context.text.TextProvider;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ArgumentsProcessor implements ItemProcessor {
    public static final ItemProcessorFactory<ArgumentsProcessor> FACTORY = new Factory();
    public static final String ARGUMENTS_TAG = "craftengine:arguments";
    private final Map<String, TextProvider> arguments;

    public ArgumentsProcessor(Map<String, TextProvider> arguments) {
        this.arguments = arguments;
    }

    public Map<String, TextProvider> arguments() {
        return arguments;
    }

    @Override
    public Item apply(Item item, ItemBuildContext context) {
        Map<String, String> processed = new HashMap<>();
        for (Map.Entry<String, TextProvider> entry : this.arguments.entrySet()) {
            processed.put(entry.getKey(), entry.getValue().get(context));
        }
        item.setTag(processed, ARGUMENTS_TAG);
        return item;
    }

    private static class Factory implements ItemProcessorFactory<ArgumentsProcessor> {

        @Override
        public ArgumentsProcessor create(ConfigValue value) {
            Map<String, TextProvider> arguments = new LinkedHashMap<>();
            ConfigSection section = value.getAsSection();
            for (String key : section.keySet()) {
                arguments.put(key, section.getValue(key, ConfigValue::getAsText));
            }
            return new ArgumentsProcessor(arguments);
        }
    }
}
