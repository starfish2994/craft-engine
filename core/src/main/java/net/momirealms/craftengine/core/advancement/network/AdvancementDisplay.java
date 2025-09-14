package net.momirealms.craftengine.core.advancement.network;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.advancement.AdvancementType;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.NetworkTextReplaceContext;
import net.momirealms.craftengine.core.plugin.text.component.ComponentProvider;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;
import java.util.Optional;

public class AdvancementDisplay {
    public static final int FLAG_BACKGROUND = 0b001;
    public static final int FLAG_SHOW_TOAST = 0b010;
    public static final int FLAG_HIDDEN = 0b100;
    private Component title;
    private Component description;
    private Item<Object> icon;
    private Optional<Key> background;
    private final AdvancementType type;
    private final boolean showToast;
    private final boolean hidden;
    private float x;
    private float y;

    public AdvancementDisplay(Component title,
                              Component description,
                              Item<Object> icon,
                              Optional<Key> background,
                              AdvancementType type,
                              boolean showToast,
                              boolean hidden,
                              float x,
                              float y) {
        this.type = type;
        this.showToast = showToast;
        this.hidden = hidden;
        this.background = background;
        this.description = description;
        this.icon = icon;
        this.title = title;
        this.x = x;
        this.y = y;
    }

    public void applyClientboundData(Player player) {
        this.icon = CraftEngine.instance().itemManager().s2c(this.icon, player);
        if (Config.interceptAdvancement()) {
            Map<String, ComponentProvider> tokens1 = CraftEngine.instance().fontManager().matchTags(AdventureHelper.componentToJson(this.title));
            if (!tokens1.isEmpty()) {
                this.title = AdventureHelper.replaceText(this.title, tokens1, NetworkTextReplaceContext.of(player));
            }
            Map<String, ComponentProvider> tokens2 = CraftEngine.instance().fontManager().matchTags(AdventureHelper.componentToJson(this.description));
            if (!tokens2.isEmpty()) {
                this.description = AdventureHelper.replaceText(this.description, tokens2, NetworkTextReplaceContext.of(player));
            }
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeComponent(this.title);
        buf.writeComponent(this.description);
        CraftEngine.instance().itemManager().encode(buf, this.icon);
        buf.writeVarInt(this.type.ordinal());
        int flags = 0;
        if (this.background.isPresent()) {
            flags |= FLAG_BACKGROUND;
        }
        if (this.showToast) {
            flags |= FLAG_SHOW_TOAST;
        }
        if (this.hidden) {
            flags |= FLAG_HIDDEN;
        }
        buf.writeInt(flags);
        this.background.ifPresent(buf::writeKey);
        buf.writeFloat(this.x);
        buf.writeFloat(this.y);
    }

    public static AdvancementDisplay read(FriendlyByteBuf buf) {
        Component title = buf.readComponent();
        Component description = buf.readComponent();
        Item<Object> icon = CraftEngine.instance().itemManager().decode(buf);
        AdvancementType type = AdvancementType.byId(buf.readVarInt());
        int flags = buf.readInt();
        boolean hasBackground = (flags & 1) != 0;
        Optional<Key> background = hasBackground ? Optional.of(buf.readKey()) : Optional.empty();
        boolean showToast = (flags & 2) != 0;
        boolean hidden = (flags & 4) != 0;
        float x = buf.readFloat();
        float y = buf.readFloat();
        return new AdvancementDisplay(title, description, icon, background, type, showToast, hidden, x, y);
    }
}
