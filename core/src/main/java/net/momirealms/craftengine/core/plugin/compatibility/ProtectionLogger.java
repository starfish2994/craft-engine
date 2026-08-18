package net.momirealms.craftengine.core.plugin.compatibility;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.jetbrains.annotations.Nullable;

public interface ProtectionLogger {

    String plugin();

    void logContainerTransaction(Player player,
                                 WorldPosition position,
                                 @Nullable Item oldItem,
                                 @Nullable Item newItem);

    void logItemFrameTransaction(Player player,
                                 WorldPosition position,
                                 Direction direction,
                                 @Nullable Item oldItem,
                                 @Nullable Item newItem);
}
