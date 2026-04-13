package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.equipment.Equipment;
import net.momirealms.craftengine.core.item.network.NetworkItemHandler;
import net.momirealms.craftengine.core.item.recipe.DatapackRecipeResult;
import net.momirealms.craftengine.core.item.updater.ItemUpdateResult;
import net.momirealms.craftengine.core.pack.model.definition.ModernItemModel;
import net.momirealms.craftengine.core.pack.model.generation.ModelGenerator;
import net.momirealms.craftengine.core.pack.model.legacy.LegacyOverridesModel;
import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.UniqueKey;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public interface ItemManager extends Manageable, ModelGenerator {

    Map<Key, Equipment> equipments();

    ConfigParser[] parsers();

    Map<Key, TreeSet<LegacyOverridesModel>> legacyItemOverrides();

    Map<Key, TreeMap<Integer, ModernItemModel>> modernItemOverrides();

    Map<Key, ModernItemModel> modernItemModels1_21_4();

    Map<Key, TreeSet<LegacyOverridesModel>> modernItemModels1_21_2();

    Collection<Key> vanillaItems();

    @Nullable
    Item createCustomWrappedItem(Key id, @Nullable Player player);

    @Nullable
    Item createWrappedItem(Key id, @Nullable Player player);

    @NotNull
    Item wrap(Object itemStack);

    Item fromByteArray(byte[] bytes);

    Map<Key, ItemDefinition> loadedItems();

    @Deprecated(forRemoval = true)
    default Collection<Key> items() {
        return loadedItems().keySet();
    }

    Optional<Equipment> getEquipment(Key key);

    Optional<ItemDefinition> getItemDefinition(Key key);

    Optional<ItemBehavior> getItemBehavior(Key key);

    Optional<? extends BuildableItem> getVanillaItem(Key key);

    UniqueKey getIngredientKey(Item item);

    NetworkItemHandler networkItemHandler();

    default Optional<? extends BuildableItem> getBuildableItem(Key key) {
        Optional<ItemDefinition> item = getItemDefinition(key);
        if (item.isPresent()) {
            return item;
        }
        return getVanillaItem(key);
    }

    Optional<ItemDefinition> getCustomItemByPathOnly(String path);

    default List<UniqueKey> itemIdsByTag(Key tag) {
        List<UniqueKey> items = new ArrayList<>();
        items.addAll(vanillaItemIdsByTag(tag));
        items.addAll(customItemIdsByTag(tag));
        return items;
    }

    List<UniqueKey> vanillaItemIdsByTag(Key tag);

    List<UniqueKey> customItemIdsByTag(Key tag);

    int getFuelTime(Key id);

    Collection<Key> itemTags();

    Collection<Suggestion> cachedCustomItemSuggestions();

    Collection<Suggestion> cachedTotemSuggestions();

    boolean isVanillaItem(Key item);

    Optional<Item> c2s(Item item);

    Optional<Item> s2c(Item item, @Nullable Player player);

    Item applyTrim(Item base, Item addition, Item template, Key pattern);

    Item build(DatapackRecipeResult result);

    List<UniqueKey> getIngredientSubstitutes(Key item);

    ItemUpdateResult updateItem(Item item, Supplier<ItemBuildContext> contextSupplier);

    Item emptyItem();
}