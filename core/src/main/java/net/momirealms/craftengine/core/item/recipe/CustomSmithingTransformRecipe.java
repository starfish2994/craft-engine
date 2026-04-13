package net.momirealms.craftengine.core.item.recipe;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.component.value.Enchantment;
import net.momirealms.craftengine.core.item.processor.ItemProcessor;
import net.momirealms.craftengine.core.item.processor.ItemProcessors;
import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.item.recipe.input.SmithingInput;
import net.momirealms.craftengine.core.item.recipe.result.ApplyItemDataPostProcessor;
import net.momirealms.craftengine.core.item.recipe.result.CustomRecipeResult;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.CommonFunctions;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceKey;
import net.momirealms.craftengine.core.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CustomSmithingTransformRecipe extends AbstractFixedResultRecipe
        implements ConditionalRecipe, VisualResultRecipe, FunctionalRecipe {
    public static final Serializer SERIALIZER = new Serializer();
    private final Ingredient base;
    private final Ingredient template;
    private final Ingredient addition;
    private final boolean mergeComponents;
    private final boolean mergeEnchantments;
    private final List<ItemDataProcessor> processors;
    private final Predicate<Context> condition;
    private final Function<Context>[] smithingFunctions;
    private final CustomRecipeResult visualResult;
    private final boolean ingredientCountSupport;

    public CustomSmithingTransformRecipe(Key id,
                                         boolean showNotification,
                                         @Nullable Ingredient template,
                                         @NotNull Ingredient base,
                                         @Nullable Ingredient addition,
                                         CustomRecipeResult result,
                                         @Nullable CustomRecipeResult visualResult,
                                         List<ItemDataProcessor> processors,
                                         boolean mergeComponents,
                                         boolean mergeEnchantments,
                                         Function<Context>[] smithingFunctions,
                                         Predicate<Context> condition,
                                         boolean ingredientCountSupport
    ) {
        super(id, showNotification, result);
        this.base = base;
        this.template = template;
        this.addition = addition;
        this.processors = processors;
        this.mergeComponents = mergeComponents;
        this.mergeEnchantments = mergeEnchantments;
        this.condition = condition;
        this.smithingFunctions = smithingFunctions;
        this.visualResult = visualResult;
        this.ingredientCountSupport = ingredientCountSupport;
    }

    public boolean ingredientCountSupport() {
        return this.ingredientCountSupport;
    }

    public boolean mergeComponents() {
        return this.mergeComponents;
    }

    public boolean mergeEnchantments() {
        return this.mergeEnchantments;
    }

    @Override
    public Function<Context>[] functions() {
        return this.smithingFunctions;
    }

    @Override
    public CustomRecipeResult visualResult() {
        return this.visualResult;
    }

    @Override
    public boolean canUse(Context context) {
        if (this.condition != null) return this.condition.test(context);
        return true;
    }

    @Override
    public boolean hasCondition() {
        return this.condition != null;
    }

    @Override
    public void takeInput(@NotNull RecipeInput input, int ignore) {
        SmithingInput smithingInput = (SmithingInput) input;
        super.takeIngredient(this.base, smithingInput.base().item(), ignore);
        if (this.template != null) super.takeIngredient(this.template, smithingInput.template().item(), ignore);
        if (this.addition != null) super.takeIngredient(this.addition, smithingInput.addition().item(), ignore);
    }

    @Override
    public boolean matches(RecipeInput input) {
        SmithingInput smithingInput = (SmithingInput) input;
        return checkIngredient(this.base, smithingInput.base())
                && checkIngredient(this.template, smithingInput.template())
                && checkIngredient(this.addition, smithingInput.addition());
    }

    private boolean checkIngredient(Ingredient ingredient, UniqueIdItem item) {
        if (ingredient != null) {
            if (item == null || item.isEmpty()) {
                return false;
            }
            return ingredient.test(item);
        } else {
            return item == null || item.isEmpty();
        }
    }

    @Override
    public List<Ingredient> ingredientsInUse() {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(this.base);
        if (this.template != null) {
            ingredients.add(this.template);
        }
        if (this.addition != null) {
            ingredients.add(this.addition);
        }
        return ingredients;
    }

    @Override
    public @NotNull Key serializerType() {
        return RecipeSerializers.SMITHING_TRANSFORM;
    }

    @Override
    public RecipeType type() {
        return RecipeType.SMITHING;
    }

    @Override
    public Item assembleVisual(RecipeInput input, ItemBuildContext context) {
        SmithingInput smithingInput = ((SmithingInput) input);
        Item base = smithingInput.base().item();
        Item result = this.visualResult().buildItem(context);
        return createSmithingResult(base, result);
    }

    @Override
    public Item assemble(RecipeInput input, ItemBuildContext context) {
        SmithingInput smithingInput = ((SmithingInput) input);
        Item base = smithingInput.base().item();
        Item result = this.result(context);
        return createSmithingResult(base, result);
    }

    private Item createSmithingResult(Item base, Item result) {
        Item finalResult = result;
        if (this.mergeComponents) {
            finalResult = base.mergeCopy(result);
        }
        if (this.processors != null) {
            for (ItemDataProcessor processor : this.processors) {
                processor.accept(base, result, finalResult);
            }
        }
        return finalResult;
    }

    @NotNull
    public Ingredient base() {
        return this.base;
    }

    @Nullable
    public Ingredient template() {
        return template;
    }

    @Nullable
    public Ingredient addition() {
        return addition;
    }

    @SuppressWarnings({"DuplicatedCode"})
    public static class Serializer extends AbstractRecipeSerializer<CustomSmithingTransformRecipe> {
        private static final String[] TEMPLATE_TYPE = new String[]{"template_type", "template-type"};
        private static final String[] MERGE_COMPONENTS = new String[]{"merge-components", "merge_components"};
        private static final String[] MERGE_ENCHANTMENTS = new String[]{"merge-enchantments", "merge_enchantments"};

        @SuppressWarnings("unchecked")
        @Override
        public CustomSmithingTransformRecipe readConfig(Key id, ConfigSection section) {
            Ingredient templateIngredient = section.getValue(TEMPLATE_TYPE, super::parseIngredient);
            Ingredient baseIngredient = section.getNonNullValue("base", ConfigConstants.ARGUMENT_LIST, super::parseIngredient);
            Ingredient additionIngredient = section.getValue("addition", super::parseIngredient);
            boolean countSupport = false;
            if (/* !countSupport && */ templateIngredient != null && templateIngredient.count() > 1) {
                countSupport = true;
            }
            if (!countSupport && additionIngredient != null && additionIngredient.count() > 1) {
                countSupport = true;
            }
            if (!countSupport && /* baseIngredient != null && */ baseIngredient.count() > 1) {
                countSupport = true;
            }
            return new CustomSmithingTransformRecipe(id,
                    section.getBoolean(SHOW_NOTIFICATIONS, true),
                    templateIngredient,
                    baseIngredient,
                    additionIngredient,
                    super.parseResult(section.getNonNullValue("result", ConfigConstants.ARGUMENT_SECTION)),
                    section.getValue(VISUAL_RESULT, super::parseResult),
                    section.getList(POST_PROCESSOR, ItemDataProcessors::fromConfig),
                    section.getBoolean(MERGE_COMPONENTS, true),
                    section.getBoolean(MERGE_ENCHANTMENTS, false),
                    section.getList(FUNCTIONS, CommonFunctions::fromConfig).toArray(new Function[0]),
                    MiscUtils.allOf(section.getList(CONDITIONS, CommonConditions::fromConfig)),
                    countSupport
            );
        }

        @Override
        public CustomSmithingTransformRecipe readJson(Key id, JsonObject json) {
            return new CustomSmithingTransformRecipe(
                    id,
                    true,
                    parseVanillaIngredient(VANILLA_RECIPE_HELPER.singleIngredient(json.get("template"))),
                    Objects.requireNonNull(parseVanillaIngredient(VANILLA_RECIPE_HELPER.singleIngredient(json.get("base")))),
                    parseVanillaIngredient(VANILLA_RECIPE_HELPER.singleIngredient(json.get("addition"))),
                    parseResult(VANILLA_RECIPE_HELPER.smithingResult(json.getAsJsonObject("result"))),
                    null,
                    null,
                    true,
                    false,
                    null,
                    null,
                    false
            );
        }
    }

    public static final class ItemDataProcessors {
        public static final ItemDataProcessor.Type<KeepComponents> KEEP_COMPONENTS = register(Key.ce("keep_components"), KeepComponents.FACTORY);
        public static final ItemDataProcessor.Type<KeepTags> KEEP_TAGS = register(Key.ce("keep_tags"), KeepTags.FACTORY);
        public static final ItemDataProcessor.Type<KeepCustomData> KEEP_CUSTOM_DATA = register(Key.ce("keep_custom_data"), KeepCustomData.FACTORY);
        public static final ItemDataProcessor.Type<MergeEnchantments> MERGE_ENCHANTMENTS = register(Key.ce("merge_enchantments"), MergeEnchantments.FACTORY);
        public static final ItemDataProcessor.Type<ApplyData> APPLY_DATA = register(Key.ce("apply_data"), ApplyData.FACTORY);

        private ItemDataProcessors() {}

        public static ItemDataProcessor fromConfig(ConfigValue value) {
            return fromConfig(value.getAsSection());
        }

        public static ItemDataProcessor fromConfig(ConfigSection section) {
            String type = section.getNonEmptyString("type");
            Key key = Key.ce(type);
            ItemDataProcessor.Type<? extends CustomSmithingTransformRecipe.ItemDataProcessor> processorType = BuiltInRegistries.SMITHING_RESULT_PROCESSOR_TYPE.getValue(key);
            if (processorType == null) {
                throw new KnownResourceException("resource.recipe.smithing_transform.post_processor.unknown_type", section.assemblePath("type"), key.asString());
            }
            return processorType.factory.create(section);
        }

        public static <T extends ItemDataProcessor> ItemDataProcessor.Type<T> register(Key key, ItemDataProcessor.Factory<T> factory) {
            ItemDataProcessor.Type<T> type = new ItemDataProcessor.Type<>(key, factory);
            ((WritableRegistry<ItemDataProcessor.Type<? extends CustomSmithingTransformRecipe.ItemDataProcessor>>) BuiltInRegistries.SMITHING_RESULT_PROCESSOR_TYPE)
                    .register(ResourceKey.create(Registries.SMITHING_RESULT_PROCESSOR_TYPE.location(), key), type);
            return type;
        }
    }

    public interface ItemDataProcessor extends TriConsumer<Item, Item, Item> {

        interface Factory<T extends ItemDataProcessor> {
            T create(ConfigSection section);
        }

        record Type<T extends ItemDataProcessor>(Key id, Factory<T> factory) {}
    }

    public static class MergeEnchantments implements ItemDataProcessor {
        public static final MergeEnchantments INSTANCE = new MergeEnchantments();
        public static final ItemDataProcessor.Factory<MergeEnchantments> FACTORY = new Factory();

        @Override
        public void accept(Item item1, Item item2, Item item3) {
            item1.enchantments().ifPresent(e1 -> {
                item3.enchantments().ifPresent(e2 -> {
                    item3.setEnchantments(Stream.concat(e1.stream(), e2.stream())
                            .collect(Collectors.toMap(
                                    Enchantment::id,
                                    enchantment -> enchantment,
                                    (existing, replacement) ->
                                            existing.level() > replacement.level() ? existing : replacement
                            ))
                            .values()
                            .stream()
                            .toList());
                });
            });
        }

        private static class Factory implements ItemDataProcessor.Factory<MergeEnchantments> {

            @Override
            public MergeEnchantments create(ConfigSection section) {
                return INSTANCE;
            }
        }
    }

    public static class ApplyData implements ItemDataProcessor {
        public static final ItemDataProcessor.Factory<ApplyData> FACTORY = new Factory();
        private final ItemProcessor[] modifiers;

        public ApplyData(ItemProcessor[] modifiers) {
            this.modifiers = modifiers;
        }

        @Override
        public void accept(Item item1, Item item2, Item item3) {
            for (ItemProcessor modifier : this.modifiers) {
                item3.apply(modifier, ItemBuildContext.EMPTY);
            }
        }

        private static class Factory implements ItemDataProcessor.Factory<ApplyData> {

            @Override
            public ApplyData create(ConfigSection section) {
                List<ItemProcessor> modifiers = new ArrayList<>();
                ItemProcessors.collectProcessors(section.getNonNullSection("data"), modifiers::add);
                return new ApplyData(modifiers.toArray(new ItemProcessor[0]));
            }
        }
    }

    public static class KeepCustomData implements ItemDataProcessor {
        public static final ItemDataProcessor.Factory<KeepCustomData> FACTORY = new Factory();
        private final List<String[]> paths;

        public KeepCustomData(List<String[]> data) {
            this.paths = data;
        }

        @Override
        public void accept(Item item1, Item item2, Item item3) {
            for (String[] path : this.paths) {
                Object dataObj = item1.getTagAsJava((Object[]) path);
                if (dataObj != null) {
                    item3.setTag(dataObj, (Object[]) path);
                }
            }
        }

        private static class Factory implements ItemDataProcessor.Factory<KeepCustomData> {
            private static final String[] TAGS = new String[]{"tags", "paths"};

            @Override
            public KeepCustomData create(ConfigSection section) {
                return new KeepCustomData(section.getNonEmptyList(TAGS, v -> v.getAsString().split("\\.")));
            }
        }
    }

    public static class KeepComponents implements ItemDataProcessor {
        public static final ItemDataProcessor.Factory<KeepComponents> FACTORY = new Factory();
        private final List<Key> components;

        public KeepComponents(List<Key> components) {
            this.components = components;
        }

        @Override
        public void accept(Item item1, Item item2, Item item3) {
            for (Key component : this.components) {
                Object componentObj = item1.getExactComponent(component);
                if (componentObj != null) {
                    item3.setExactComponent(component, componentObj);
                }
            }
        }

        private static class Factory implements ItemDataProcessor.Factory<KeepComponents> {
            private static final Key CUSTOM_DATA = Key.of("minecraft", "custom_data");

            @Override
            public KeepComponents create(ConfigSection section) {
                return new KeepComponents(section.getNonEmptyList("components", ConfigValue::getAsIdentifier).stream().filter(it -> !CUSTOM_DATA.equals(it)).toList());
            }
        }
    }

    public static class KeepTags implements ItemDataProcessor {
        public static final ItemDataProcessor.Factory<KeepTags> FACTORY = new Factory();
        private final List<String[]> tags;

        public KeepTags(List<String[]> tags) {
            this.tags = tags;
        }

        @Override
        public void accept(Item item1, Item item2, Item item3) {
            for (String[] tag : this.tags) {
                Object tagObj = item1.getTagAsJava((Object[]) tag);
                if (tagObj != null) {
                    item3.setTag(tagObj, (Object[]) tag);
                }
            }
        }

        private static class Factory implements ItemDataProcessor.Factory<KeepTags> {

            @Override
            public KeepTags create(ConfigSection section) {
                return new KeepTags(section.getNonEmptyList("tags", v -> v.getAsString().split("\\.")));
            }
        }
    }
}
