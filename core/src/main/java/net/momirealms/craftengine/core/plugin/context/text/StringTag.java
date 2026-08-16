package net.momirealms.craftengine.core.plugin.context.text;

import net.momirealms.craftengine.core.plugin.context.Context;
import org.jetbrains.annotations.Nullable;

/**
 * A tag in the string template system — like a MiniMessage tag, but produces a plain
 * string directly instead of going through a Component tree.
 *
 * <p>CraftEngine's tag classes implement this interface alongside the MiniMessage
 * {@code TagResolver} (same class, two entry points sharing one implementation).</p>
 *
 * <p>Returning {@code null} leaves the tag as literal text in the output. Throwing
 * propagates to the caller (configuration errors surface loudly).</p>
 */
@FunctionalInterface
public interface StringTag {

    /**
     * Resolves this tag to a string.
     *
     * @param args    the tag arguments (already unquoted/unescaped), may be empty
     * @param context the evaluation context
     * @return the resolved string, or {@code null} to keep the tag as literal text
     */
    @Nullable String resolve(String[] args, Context context);

    /**
     * Precompiles a handler bound to the given arguments. Called once when a
     * {@link StringTemplate} / precompiled expression is built — implementations can
     * resolve and validate external references here (fail-fast at load time) and return
     * a handler that skips the lookup at evaluation time. Throwing here rejects the
     * configuration early.
     *
     * @param args the tag arguments (already unquoted/unescaped)
     * @return the handler to use at evaluation time; {@code this} by default
     */
    default StringTag precompile(String[] args) {
        return this;
    }

    static String requireArg(String[] args, int index, String message) {
        if (index >= args.length) {
            throw new IllegalArgumentException(message);
        }
        return args[index];
    }
}
