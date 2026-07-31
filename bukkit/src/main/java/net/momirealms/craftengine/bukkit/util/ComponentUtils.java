package net.momirealms.craftengine.bukkit.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.plugin.network.BukkitNetworkManager;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.adventure.text.serializer.gson.GsonComponentSerializerProxy;
import net.momirealms.craftengine.proxy.minecraft.network.chat.ComponentProxy;
import net.momirealms.craftengine.proxy.minecraft.network.chat.ComponentSerializationProxy;
import net.momirealms.craftengine.proxy.minecraft.network.chat.MutableComponentProxy;
import net.momirealms.craftengine.proxy.minecraft.network.chat.contents.PlainTextContentsProxy;
import net.momirealms.craftengine.proxy.minecraft.network.chat.contents.TranslatableContentsProxy;

import java.util.List;

public final class ComponentUtils {
    public static final Codec<Object> ComponentSerialization$CODEC = VersionHelper.isOrAbove1_20_3 ? ComponentSerializationProxy.INSTANCE.getCodec() : null;

    private ComponentUtils() {}

    public static Object adventureToMinecraft(Component component) {
        return jsonElementToMinecraft(AdventureHelper.componentToJsonElement(component));
    }

    public static Object adventureToPaperAdventure(Component component) {
        return jsonElementToPaperAdventure(AdventureHelper.componentToJsonElement(component));
    }

    public static Object jsonElementToMinecraft(JsonElement json) {
        if (VersionHelper.isOrAbove1_21_6) {
            if (json == null) return null;
            return ComponentSerialization$CODEC.parse(RegistryOps.JSON, json).getOrThrow(JsonParseException::new);
        } else if (VersionHelper.isOrAbove1_20_5) {
            return ComponentProxy.SerializerProxy.INSTANCE.fromJson(json, RegistryUtils.getRegistryAccess());
        } else {
            return ComponentProxy.SerializerProxy.INSTANCE.fromJson(json);
        }
    }

    public static Object jsonToMinecraft(String json) {
        if (VersionHelper.isOrAbove1_21_6) {
            JsonElement jsonElement = GsonHelper.get().fromJson(json, JsonElement.class);
            return ComponentSerialization$CODEC.parse(RegistryOps.JSON, jsonElement).getOrThrow(JsonParseException::new);
        } else if (VersionHelper.isOrAbove1_20_5) {
            return ComponentProxy.SerializerProxy.INSTANCE.fromJson(json, RegistryUtils.getRegistryAccess());
        } else {
            return ComponentProxy.SerializerProxy.INSTANCE.fromJson(json);
        }
    }

    public static String minecraftToJson(Object component) {
        if (VersionHelper.isOrAbove1_21_6) {
            JsonElement jsonElement = ComponentSerialization$CODEC.encodeStart(RegistryOps.JSON, component).getOrThrow(JsonParseException::new);
            return GsonHelper.get().toJson(jsonElement);
        } else if (VersionHelper.isOrAbove1_20_5) {
            return ComponentProxy.SerializerProxy.INSTANCE.toJson(component, RegistryUtils.getRegistryAccess());
        } else {
            return ComponentProxy.SerializerProxy.INSTANCE.toJson(component);
        }
    }

    public static String paperAdventureToJson(Object component) {
        return GsonComponentSerializerProxy.GSON.toJson(component);
    }

    public static Object jsonToPaperAdventure(String json) {
        return GsonComponentSerializerProxy.GSON.fromJson(json, net.momirealms.craftengine.proxy.adventure.text.ComponentProxy.CLASS);
    }

    public static JsonElement paperAdventureToJsonElement(Object component) {
        return GsonComponentSerializerProxy.GSON.toJsonTree(component);
    }

    public static Object jsonElementToPaperAdventure(JsonElement json) {
        return GsonComponentSerializerProxy.GSON.fromJson(json, net.momirealms.craftengine.proxy.adventure.text.ComponentProxy.CLASS);
    }

    public static boolean hasNetworkTag(Object component, boolean checkHoverEvent) {
        if (!MutableComponentProxy.CLASS.isInstance(component)) {
            return false;
        }

        Object contents = MutableComponentProxy.INSTANCE.getContents(component);
        if (PlainTextContentsProxy.CLASS.isInstance(contents)) {
            String text = PlainTextContentsProxy.INSTANCE.getText(contents);
            if (BukkitNetworkManager.instance().hasNetworkTag(text)) {
                return true;
            }
        } else if (TranslatableContentsProxy.CLASS.isInstance(contents)) {
            Object[] args = TranslatableContentsProxy.INSTANCE.getArgs(contents);
            for (Object arg : args) {
                if (ComponentProxy.CLASS.isInstance(arg)) {
                    if (hasNetworkTag(arg, checkHoverEvent)) {
                        return true;
                    }
                }
            }
        }

        if (checkHoverEvent) {
            // todo 完成hoverevent
        }

        List<Object> children = MutableComponentProxy.INSTANCE.getSiblings(component);
        if (children.isEmpty()) {
            return false;
        }

        for (Object child : children) {
            if (hasNetworkTag(child, checkHoverEvent)) {
                return true;
            }
        }
        return false;
    }
}
