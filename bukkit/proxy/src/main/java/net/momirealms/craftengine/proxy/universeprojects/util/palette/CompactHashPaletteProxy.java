package net.momirealms.craftengine.proxy.universeprojects.util.palette;

import net.momirealms.craftengine.proxy.minecraft.world.level.chunk.HashMapPaletteProxy;
import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "com.universeprojects.util.palette.CompactHashPalette", optional = true)
public interface CompactHashPaletteProxy extends HashMapPaletteProxy {
    CompactHashPaletteProxy INSTANCE = ASMProxyFactory.create(CompactHashPaletteProxy.class);
    Class<?> CLASS = SparrowClass.find("com.universeprojects.util.palette.CompactHashPalette");

    @FieldGetter(name = "entries")
    Object[] getEntries(Object target);
}
