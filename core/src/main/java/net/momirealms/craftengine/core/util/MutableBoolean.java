package net.momirealms.craftengine.core.util;

public class MutableBoolean {
    private boolean value;

    public MutableBoolean(boolean value) {
        this.value = value;
    }

    public boolean booleanValue() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }
}
