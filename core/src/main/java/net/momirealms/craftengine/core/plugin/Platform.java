package net.momirealms.craftengine.core.plugin;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.trade.MerchantOffer;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.particle.ParticleType;
import net.momirealms.sparrow.nbt.Tag;

import java.util.List;

public interface Platform {

    void dispatchCommand(String command);

    Object snbtToJava(String nbt);

    Tag jsonToSparrowNBT(JsonElement json);

    Tag snbtToSparrowNBT(String nbt);

    Tag javaToSparrowNBT(Object object);

    World getWorld(String name);

    ParticleType getParticleType(Key name);
}
