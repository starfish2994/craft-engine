package net.momirealms.craftengine.core.util;

import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.renderer.ComponentRenderer;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A port of adventure's {@code TextReplacementRenderer} with one fix: when an entire
 * text component is matched (start == 0 and end == content length), the original node's
 * style is preserved and the replacement is pushed down into a child instead of replacing
 * the node in place.
 *
 * <p>adventure's original implementation swaps the node for the replacement and then merges
 * the original style onto it with {@code IF_ABSENT_ON_TARGET}. When the replacement carries
 * its own style (e.g. a color), that style wins and becomes the node's style, which then
 * leaks into the node's existing children. Keeping the original node (as an empty-text node
 * with the original style) and inserting the replacement as a child isolates the replacement
 * style while remaining visually equivalent for the replacement's own text (it inherits the
 * original style through the tree when it does not override it).</p>
 */
final class FixedTextReplacementRenderer implements ComponentRenderer<FixedTextReplacementRenderer.State> {
    static final FixedTextReplacementRenderer INSTANCE = new FixedTextReplacementRenderer();

    private FixedTextReplacementRenderer() {
    }

    @Override
    public @NonNull Component render(final Component component, final State state) {
        final boolean prevFirstMatch = state.firstMatch;
        state.firstMatch = true;

        final List<Component> oldChildren = component.children();
        final int oldChildrenSize = oldChildren.size();
        Style oldStyle = component.style();
        List<Component> children = null;
        Component modified = component;

        if (component instanceof TextComponent tc) {
            final String content = tc.content();
            final Matcher matcher = state.pattern.matcher(content);
            int replacedUntil = 0; // last index handled
            while (matcher.find()) {
                if (matcher.start() == 0) {
                    if (matcher.end() == content.length()) {
                        // Full match: preserve this node's style and push the replacement into a child
                        // so the replacement's own style cannot leak into the existing children.
                        final ComponentLike replacement = state.replacement.apply(matcher, Component.text().content(matcher.group()).style(component.style()));
                        modified = Component.text("", component.style());
                        if (replacement == null) {
                            if (children == null) {
                                children = new ArrayList<>(oldChildrenSize);
                            }
                        } else {
                            final Component replaced = replacement.asComponent();
                            if (replaced.style().hoverEvent() != null) {
                                // the replacement brings its own hover event; drop the original one to avoid a collision
                                oldStyle = oldStyle.hoverEvent(null);
                                modified = modified.style(s -> s.hoverEvent(null));
                            }
                            if (children == null) {
                                children = new ArrayList<>(oldChildrenSize + 1 + replaced.children().size());
                            }
                            children.add(replaced);
                        }
                    } else {
                        // match at the start but not the whole content: work on a child of the root node
                        modified = Component.text("", component.style());
                        final ComponentLike child = state.replacement.apply(matcher, Component.text().content(matcher.group()));
                        if (child != null) {
                            if (children == null) {
                                children = new ArrayList<>(oldChildrenSize + 1);
                            }
                            children.add(child.asComponent());
                        }
                    }
                } else {
                    if (children == null) {
                        children = new ArrayList<>(oldChildrenSize + 2);
                    }
                    if (state.firstMatch) {
                        // truncate parent to content before match
                        modified = ((TextComponent) component).content(content.substring(0, matcher.start()));
                    } else if (replacedUntil < matcher.start()) {
                        children.add(Component.text(content.substring(replacedUntil, matcher.start())));
                    }
                    final ComponentLike builder = state.replacement.apply(matcher, Component.text().content(matcher.group()));
                    if (builder != null) {
                        children.add(builder.asComponent());
                    }
                }
                state.firstMatch = false;
                replacedUntil = matcher.end();
            }
            if (replacedUntil < content.length()) {
                // append trailing content
                if (replacedUntil > 0) {
                    if (children == null) {
                        children = new ArrayList<>(oldChildrenSize);
                    }
                    children.add(Component.text(content.substring(replacedUntil)));
                }
                // otherwise, we haven't modified the component, so nothing to change
            }
        } else if (modified instanceof TranslatableComponent) { // get TranslatableComponent with() args
            final List<TranslationArgument> args = ((TranslatableComponent) modified).arguments();
            List<TranslationArgument> newArgs = null;
            for (int i = 0, size = args.size(); i < size; i++) {
                final TranslationArgument original = args.get(i);
                final TranslationArgument replaced = original.value() instanceof Component ? TranslationArgument.component(this.render((Component) original.value(), state)) : original;
                if (replaced != original) {
                    if (newArgs == null) {
                        newArgs = new ArrayList<>(size);
                        if (i > 0) {
                            newArgs.addAll(args.subList(0, i));
                        }
                    }
                }
                if (newArgs != null) {
                    newArgs.add(replaced);
                }
            }
            if (newArgs != null) {
                modified = ((TranslatableComponent) modified).arguments(newArgs);
            }
        }

        // hover event
        if (state.replaceInsideHoverEvents) {
            final HoverEvent<?> event = oldStyle.hoverEvent();
            if (event != null) {
                final HoverEvent<?> rendered = event.withRenderedValue(this, state);
                if (event != rendered) {
                    modified = modified.style(s -> s.hoverEvent(rendered));
                }
            }
        }

        // children
        boolean first = true;
        for (int i = 0; i < oldChildrenSize; i++) {
            final Component child = oldChildren.get(i);
            final Component replaced = this.render(child, state);
            if (replaced != child) {
                if (children == null) {
                    children = new ArrayList<>(oldChildrenSize);
                }
                if (first) {
                    children.addAll(oldChildren.subList(0, i));
                }
                first = false;
            }
            if (children != null) {
                children.add(replaced);
                first = false;
            }
        }

        state.firstMatch = prevFirstMatch;
        // update the modified component with new children
        if (children != null) {
            return modified.children(children);
        }
        return modified;
    }

    static final class State {
        final Pattern pattern;
        final BiFunction<MatchResult, TextComponent.Builder, ComponentLike> replacement;
        final boolean replaceInsideHoverEvents;
        boolean firstMatch = true;

        State(final Pattern pattern,
              final BiFunction<MatchResult, TextComponent.Builder, ComponentLike> replacement,
              final boolean replaceInsideHoverEvents) {
            this.pattern = pattern;
            this.replacement = replacement;
            this.replaceInsideHoverEvents = replaceInsideHoverEvents;
        }
    }
}
