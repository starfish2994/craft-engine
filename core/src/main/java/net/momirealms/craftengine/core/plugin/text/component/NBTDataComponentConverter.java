package net.momirealms.craftengine.core.plugin.text.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.event.DataComponentValueConverterRegistry;
import net.kyori.adventure.text.serializer.gson.GsonDataComponentValue;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.TagParser;
import net.momirealms.sparrow.nbt.EndTag;
import net.momirealms.sparrow.nbt.Tag;
import net.momirealms.sparrow.nbt.adventure.NBTDataComponentValue;
import net.momirealms.sparrow.nbt.adventure.NBTTagHolder;
import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.constructor.SConstructor2;
import net.momirealms.sparrow.reflection.constructor.matcher.ConstructorMatcher;
import net.momirealms.sparrow.reflection.field.matcher.FieldMatcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NBTDataComponentConverter {
    private NBTDataComponentConverter() {}

    @SuppressWarnings("unchecked")
    public static void register() {
        Map<Class<?>, Map<Class<?>, Object>> CACHE = (Map<Class<?>, Map<Class<?>, Object>>) SparrowClass.of(SparrowClass.find("net.kyori.adventure.text.event.DataComponentValueConverterRegistry$ConversionCache"))
                .getDeclaredSparrowField(FieldMatcher.named("CACHE"))
                .mh()
                .get(null);

        // NBT -> Gson
        DataComponentValueConverterRegistry.Conversion<NBTDataComponentValue.NBTDataComponentValueImpl, GsonDataComponentValue> convertor01 = DataComponentValueConverterRegistry.Conversion.convert(
                NBTDataComponentValue.NBTDataComponentValueImpl.class,
                GsonDataComponentValue.class,
                (key, srcValue) -> GsonDataComponentValue.gsonDataComponentValue(CraftEngine.instance().platform().sparrowNBTToJson(srcValue.tag()))
        );
        // Removed NBT -> GSON
        DataComponentValueConverterRegistry.Conversion<NBTDataComponentValue.RemovedNBTDataComponentValue, GsonDataComponentValue> convertor02 = DataComponentValueConverterRegistry.Conversion.convert(
                NBTDataComponentValue.RemovedNBTDataComponentValue.class,
                GsonDataComponentValue.class,
                (key, srcValue) -> GsonDataComponentValue.gsonDataComponentValue(JsonNull.INSTANCE)
        );
        Class<?> gsonClass = SparrowClass.find("net.kyori.adventure.text.serializer.gson.GsonDataComponentValueImpl");
        Class<?> removedGsonClass = SparrowClass.find("net.kyori.adventure.text.serializer.gson.GsonDataComponentValueImpl$RemovedGsonComponentValueImpl");
        // GSON -> NBT
        DataComponentValueConverterRegistry.Conversion<?, NBTDataComponentValue> convertor03 = DataComponentValueConverterRegistry.Conversion.convert(
                gsonClass,
                NBTDataComponentValue.class,
                (key, srcValue) -> {
                    JsonElement element = ((GsonDataComponentValue) srcValue).element();
                    if (element.isJsonNull()) {
                        return NBTDataComponentValue.removed();
                    } else {
                        return NBTDataComponentValue.of(CraftEngine.instance().platform().jsonToSparrowNBT(element));
                    }
                }
        );
        // Removed GSON -> NBT
        DataComponentValueConverterRegistry.Conversion<?, NBTDataComponentValue> convertor04 = DataComponentValueConverterRegistry.Conversion.convert(
                removedGsonClass,
                NBTDataComponentValue.class,
                (key, srcValue) -> NBTDataComponentValue.removed()
        );
        // Binary Holder -> GSON
        DataComponentValueConverterRegistry.Conversion<BinaryTagHolder, GsonDataComponentValue> convertor10 = DataComponentValueConverterRegistry.Conversion.convert(
                BinaryTagHolder.class,
                GsonDataComponentValue.class,
                (key, srcValue) -> GsonDataComponentValue.gsonDataComponentValue(GsonHelper.get().fromJson(srcValue.toString(), JsonElement.class))
        );
        // Binary Holder -> NBT
        DataComponentValueConverterRegistry.Conversion<BinaryTagHolder, NBTDataComponentValue> convertor11 = DataComponentValueConverterRegistry.Conversion.convert(
                BinaryTagHolder.class,
                NBTDataComponentValue.class,
                (key, srcValue) -> {
                    try {
                        Tag tag = TagParser.parseTagFully(srcValue.string());
                        if (tag == EndTag.INSTANCE) {
                            return NBTDataComponentValue.removed();
                        } else {
                            return NBTDataComponentValue.nbtDataComponentValue(tag);
                        }
                    } catch (Exception e) {
                        CraftEngine.instance().logger().warn("Failed to parse NBTDataComponentValue from " + srcValue, e);
                        return NBTDataComponentValue.removed();
                    }
                }
        );
        // NBT Holder -> GSON
        DataComponentValueConverterRegistry.Conversion<NBTTagHolder, GsonDataComponentValue> convertor12 = DataComponentValueConverterRegistry.Conversion.convert(
                NBTTagHolder.class,
                GsonDataComponentValue.class,
                (key, srcValue) -> {
                    try {
                        Tag tag = srcValue.tag();
                        if (tag == EndTag.INSTANCE) {
                            return GsonDataComponentValue.gsonDataComponentValue(JsonNull.INSTANCE);
                        } else {
                            return GsonDataComponentValue.gsonDataComponentValue(CraftEngine.instance().platform().sparrowNBTToJson(tag));
                        }
                    } catch (Exception e) {
                        CraftEngine.instance().logger().warn("Failed to parse NBTDataComponentValue from " + srcValue, e);
                        return GsonDataComponentValue.gsonDataComponentValue(JsonNull.INSTANCE);
                    }
                }
        );
        // NBT Holder -> NBT
        DataComponentValueConverterRegistry.Conversion<NBTTagHolder, NBTDataComponentValue> convertor13 = DataComponentValueConverterRegistry.Conversion.convert(
                NBTTagHolder.class,
                NBTDataComponentValue.class,
                (key, srcValue) -> {
                    try {
                        Tag tag = srcValue.tag();
                        if (tag == EndTag.INSTANCE) {
                            return NBTDataComponentValue.removed();
                        } else {
                            return NBTDataComponentValue.nbtDataComponentValue(tag);
                        }
                    } catch (Exception e) {
                        CraftEngine.instance().logger().warn("Failed to parse NBTDataComponentValue from " + srcValue, e);
                        return NBTDataComponentValue.removed();
                    }
                }
        );

        SConstructor2 constructor = SparrowClass.of(SparrowClass.find("net.kyori.adventure.text.event.DataComponentValueConverterRegistry$RegisteredConversion"))
                .getDeclaredSparrowConstructor(ConstructorMatcher.takeArguments(Key.class, DataComponentValueConverterRegistry.Conversion.class))
                .asm$2();

        CACHE.computeIfAbsent(NBTDataComponentValue.NBTDataComponentValueImpl.class, $ -> new ConcurrentHashMap<>())
                .computeIfAbsent(GsonDataComponentValue.class, $ -> constructor.newInstance(Key.key("craftengine", "serializer/nbt"), convertor01));
        CACHE.computeIfAbsent(NBTDataComponentValue.RemovedNBTDataComponentValue.class, $ -> new ConcurrentHashMap<>())
                .computeIfAbsent(GsonDataComponentValue.class, $ -> constructor.newInstance(Key.key("craftengine", "serializer/nbt"), convertor02));
        CACHE.computeIfAbsent(gsonClass, $ -> new ConcurrentHashMap<>())
                .computeIfAbsent(NBTDataComponentValue.class, $ -> constructor.newInstance(Key.key("craftengine", "serializer/gson"), convertor03));
        CACHE.computeIfAbsent(removedGsonClass, $ -> new ConcurrentHashMap<>())
                .computeIfAbsent(NBTDataComponentValue.class, $ -> constructor.newInstance(Key.key("craftengine", "serializer/gson"), convertor04));
        CACHE.computeIfAbsent(SparrowClass.find("net.kyori.adventure.nbt.api.BinaryTagHolderImpl"), $ -> new ConcurrentHashMap<>())
                .computeIfAbsent(GsonDataComponentValue.class, $ -> constructor.newInstance(Key.key("craftengine", "serializer/binary"), convertor10));
        CACHE.computeIfAbsent(SparrowClass.find("net.kyori.adventure.nbt.api.BinaryTagHolderImpl"), $ -> new ConcurrentHashMap<>())
                .computeIfAbsent(NBTDataComponentValue.class, $ -> constructor.newInstance(Key.key("craftengine", "serializer/binary"), convertor11));
        CACHE.computeIfAbsent(NBTTagHolder.class, $ -> new ConcurrentHashMap<>())
                .computeIfAbsent(GsonDataComponentValue.class, $ -> constructor.newInstance(Key.key("craftengine", "serializer/binary"), convertor12));
        CACHE.computeIfAbsent(NBTTagHolder.class, $ -> new ConcurrentHashMap<>())
                .computeIfAbsent(NBTDataComponentValue.class, $ -> constructor.newInstance(Key.key("craftengine", "serializer/binary"), convertor13));
    }
}
