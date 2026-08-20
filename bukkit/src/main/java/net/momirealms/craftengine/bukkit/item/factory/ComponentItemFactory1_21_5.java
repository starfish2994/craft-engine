package net.momirealms.craftengine.bukkit.item.factory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.item.ComponentItemWrapper;
import net.momirealms.craftengine.bukkit.item.DataComponentTypes;
import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributeModifier;
import net.momirealms.craftengine.core.item.component.DataComponentKeys;
import net.momirealms.craftengine.core.item.component.value.JukeboxPlayable;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.ListTag;
import net.momirealms.sparrow.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ComponentItemFactory1_21_5 extends ComponentItemFactory1_21_4 {

    public ComponentItemFactory1_21_5(CraftEngine plugin) {
        super(plugin);
    }

    @Override
    protected void customNameJson(ComponentItemWrapper item, JsonElement json) {
        if (json == null) {
            item.resetComponent(DataComponentTypes.CUSTOM_NAME);
        } else {
            item.setJsonComponent(DataComponentTypes.CUSTOM_NAME, json);
        }
    }

    @Override
    protected Optional<JsonElement> customNameJson(ComponentItemWrapper item) {
        return item.getComponentAsJson(DataComponentTypes.CUSTOM_NAME);
    }

    @Override
    protected void customNameComponent(ComponentItemWrapper item, Component component) {
        if (component == null) {
            item.resetComponent(DataComponentTypes.CUSTOM_NAME);
        } else {
            item.setSparrowNBTComponent(DataComponentTypes.CUSTOM_NAME, AdventureHelper.componentToNbt(component));
        }
    }

    @Override
    protected Optional<Component> customNameComponent(ComponentItemWrapper item) {
        return customNameJson(item).map(AdventureHelper::jsonElementToComponent);
    }

    @Override
    protected void itemNameJson(ComponentItemWrapper item, JsonElement json) {
        if (json == null) {
            item.resetComponent(DataComponentTypes.ITEM_NAME);
        } else {
            item.setJsonComponent(DataComponentTypes.ITEM_NAME, json);
        }
    }

    @Override
    protected void itemNameComponent(ComponentItemWrapper item, Component component) {
        if (component == null) {
            item.resetComponent(DataComponentTypes.ITEM_NAME);
        } else {
            item.setSparrowNBTComponent(DataComponentTypes.ITEM_NAME, AdventureHelper.componentToNbt(component));
        }
    }

    @Override
    protected Optional<JsonElement> itemNameJson(ComponentItemWrapper item) {
        return item.getComponentAsJson(DataComponentTypes.ITEM_NAME);
    }

    @Override
    protected Optional<JsonArray> loreJson(ComponentItemWrapper item) {
        if (!item.hasComponent(DataComponentTypes.LORE)) return Optional.empty();
        Optional<JsonElement> json = item.getComponentAsJson(DataComponentTypes.LORE);
        if (json.isEmpty()) return Optional.empty();
        return Optional.of(json.get().getAsJsonArray());
    }

    @Override
    protected void loreComponent(ComponentItemWrapper item, List<Component> lore) {
        if (lore == null || lore.isEmpty()) {
            item.resetComponent(DataComponentTypes.LORE);
        } else {
            List<Tag> loreTags = new ArrayList<>();
            for (Component component : lore) {
                loreTags.add(AdventureHelper.componentToTag(component));
            }
            item.setSparrowNBTComponent(DataComponentTypes.LORE, new ListTag(loreTags));
        }
    }

    @Override
    protected void loreJson(ComponentItemWrapper item, JsonArray lore) {
        if (lore == null || lore.isEmpty()) {
            item.resetComponent(DataComponentTypes.LORE);
        } else {
            item.setJsonComponent(DataComponentTypes.LORE, lore);
        }
    }

    @Override
    protected Optional<JukeboxPlayable> jukeboxSong(ComponentItemWrapper item) {
        if (!item.hasComponent(DataComponentTypes.JUKEBOX_PLAYABLE)) return Optional.empty();
        String song = (String) item.getComponentAsJava(DataComponentTypes.JUKEBOX_PLAYABLE).orElse(null);
        if (song == null) return Optional.empty();
        return Optional.of(new JukeboxPlayable(song, true));
    }

    @Override
    protected void jukeboxSong(ComponentItemWrapper item, JukeboxPlayable data) {
        item.setJavaComponent(DataComponentTypes.JUKEBOX_PLAYABLE, data.song());
    }

    @Override
    protected void attributeModifiers(ComponentItemWrapper item, List<VanillaAttributeModifier> modifierList) {
        ListTag modifiers = new ListTag();
        for (VanillaAttributeModifier modifier : modifierList) {
            CompoundTag modifierTag = new CompoundTag();
            modifierTag.putString("type", modifier.type());
            modifierTag.putString("slot", modifier.slot().name().toLowerCase(Locale.ENGLISH));
            modifierTag.putString("id", modifier.id().toString());
            modifierTag.putDouble("amount", modifier.amount());
            modifierTag.putString("operation", modifier.operation().id());
            VanillaAttributeModifier.Display display = modifier.display();
            if (VersionHelper.isOrAbove1_21_6 && display != null) {
                CompoundTag displayTag = new CompoundTag();
                VanillaAttributeModifier.Display.Type displayType = display.type();
                displayTag.putString("type", displayType.name().toLowerCase(Locale.ENGLISH));
                if (displayType == VanillaAttributeModifier.Display.Type.OVERRIDE) {
                    displayTag.put("value", AdventureHelper.componentToTag(display.value()));
                }
                modifierTag.put("display", displayTag);
            }
            modifiers.add(modifierTag);
        }
        item.setSparrowNBTComponent(DataComponentKeys.ATTRIBUTE_MODIFIERS, modifiers);
    }
}
