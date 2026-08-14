package net.momirealms.craftengine.core.loot.source;

import net.momirealms.craftengine.core.loot.Loot;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.config.*;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class LootSource {
    protected static final String[] OVERWRITE = ConfigKeys.of("overwrite");
    protected static final String[] LEGACY_OVERRIDE = ConfigKeys.of("override");
    protected static final String[] LOOT = ConfigKeys.of("loot(s)");
    protected static final String[] TARGET = ConfigKeys.of("target(s)");

    private final LootSourceType<?> type;
    private final Predicate<LootContext> condition;
    private final Loot loot;
    private final EnumSet<OverwriteMode> overwrite;

    public LootSource(LootSourceType<?> type, Predicate<LootContext> condition, Loot loot, EnumSet<OverwriteMode> overwrite) {
        this.type = type;
        this.condition = condition;
        this.loot = loot;
        this.overwrite = overwrite;
    }

    public static final class Factory implements LootSourceFactory<LootSource> {

        @Override
        public LootSource create(LootSourceType<?> type, Key id, ConfigSection section) {
            rejectTargets(type, section);
            return new LootSource(type, parseConditions(section), parseLoot(section), parseOverwrite(section));
        }
    }

    protected static Predicate<LootContext> parseConditions(ConfigSection section) {
        return MiscUtils.allOf(section.getList(ConfigKeys.of("condition(s)"), CommonConditions::fromConfig));
    }

    protected static Loot parseLoot(ConfigSection section) {
        return section.getNonNullValue(LOOT, ConfigConstants.ARGUMENT_ANY, ConfigValue::getAsLoot);
    }

    protected static List<Key> parseTargets(ConfigSection section) {
        List<Key> targets = section.getList(TARGET, ConfigValue::getAsIdentifier);
        for (Key target : targets) {
            if (target.value().contains("[")) {
                throw new KnownResourceException("loot.source.invalid_target", section.assemblePath(TARGET[0]), target.toString());
            }
        }
        return targets;
    }

    protected static void rejectTargets(LootSourceType<?> type, ConfigSection section) {
        if (section.getValue(TARGET) != null) {
            throw new KnownResourceException("loot.source.target_not_allowed", section.assemblePath(TARGET[0]), type.id().toString());
        }
    }

    protected static EnumSet<OverwriteMode> parseOverwrite(ConfigSection section) {
        ConfigValue value = section.getValue(OVERWRITE);
        if (value == null) {
            // 兼容旧版 vanilla-loots 的 override 布尔键,true 仅覆写物品
            if (section.getValue(LEGACY_OVERRIDE) != null && section.getBoolean(LEGACY_OVERRIDE)) {
                return EnumSet.of(OverwriteMode.ITEMS);
            }
            return EnumSet.noneOf(OverwriteMode.class);
        }
        EnumSet<OverwriteMode> set = EnumSet.noneOf(OverwriteMode.class);
        List<String> tokens = value.is(List.class)
                ? value.getAsList(v -> v.getAsString().toLowerCase(Locale.ROOT))
                : List.of(value.getAsString().toLowerCase(Locale.ROOT));
        for (String token : tokens) {
            switch (token) {
                case "none" -> { /* empty set */ }
                case "all" -> {
                    set.add(OverwriteMode.ITEMS);
                    set.add(OverwriteMode.EXPERIENCE);
                }
                case "items", "item" -> set.add(OverwriteMode.ITEMS);
                case "experience", "exp" -> set.add(OverwriteMode.EXPERIENCE);
                default -> throw new KnownResourceException("loot.source.unknown_overwrite_mode", section.assemblePath(OVERWRITE[0]), token);
            }
        }
        return set;
    }

    public LootSourceType<?> type() {
        return this.type;
    }

    public List<Key> targets() {
        return List.of();
    }

    public Loot loot() {
        return this.loot;
    }

    public boolean matches(LootContext context) {
        return this.condition.test(context);
    }

    public boolean overwriteItems() {
        return this.overwrite.contains(OverwriteMode.ITEMS);
    }

    public boolean overwriteExperience() {
        return this.overwrite.contains(OverwriteMode.EXPERIENCE);
    }
}
