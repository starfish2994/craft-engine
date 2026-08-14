package net.momirealms.craftengine.core.attribute.damage;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.Context;

import java.util.List;

public interface DamageIndicator {

    void display(Player attacker, Entity victim, List<Player> viewers, Context context);
}
