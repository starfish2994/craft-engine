package net.momirealms.craftengine.proxy.common.text.font;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.proxy.common.util.Key;

public interface Image {

    String miniMessageAt(int row, int col);

    String mineDownAt(int row, int col);

    Key id();

    int codepointAt(int row, int column);

    Component componentAt(int row, int column);
}
