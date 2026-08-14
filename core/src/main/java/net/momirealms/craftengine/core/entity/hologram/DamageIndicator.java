package net.momirealms.craftengine.core.entity.hologram;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.player.Player;

import java.util.List;

public interface DamageIndicator {

    void display(Player attacker, Entity victim, double damage, List<Player> viewers);
}
