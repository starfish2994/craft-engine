package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.*;
import net.momirealms.craftengine.bukkit.world.BukkitExistingBlock;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateFlags;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.entity.EquipmentSlot;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.item.ItemTags;
import net.momirealms.craftengine.core.item.component.DataComponentKeys;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.LazyReference;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelWriterProxy;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.bukkit.GameEvent;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public final class StrippableBlockBehavior extends BukkitBlockBehavior {
    public static final BlockBehaviorFactory<StrippableBlockBehavior> FACTORY = new Factory();
    private static final SoundData DEFAULT_STRIP_SOUND = SoundData.of(Key.of("minecraft:item.axe.strip"));
    public final String stripped;
    public final LazyReference<BlockStateWrapper> lazyState;
    public final List<String> excludedProperties;
    private final List<Key> tools;
    private final List<Key> toolTags;
    private final SoundData stripSound;

    private StrippableBlockBehavior(BlockDefinition block,
                                    String stripped,
                                    List<String> excludedProperties,
                                    List<Key> tools,
                                    List<Key> toolTags,
                                    SoundData stripSound) {
        super(block);
        this.stripped = stripped;
        this.lazyState = LazyReference.untilNotNull(() -> CraftEngine.instance().blockManager().createBlockState(this.stripped));
        this.excludedProperties = excludedProperties;
        this.tools = tools;
        this.toolTags = toolTags;
        this.stripSound = stripSound;
    }

    private static boolean canBlockAttack(Item item) {
        if (VersionHelper.isOrAbove1_21_5) {
            return item.hasComponent(DataComponentKeys.BLOCKS_ATTACK);
        } else {
            return item.vanillaId().equals(ItemKeys.SHIELD);
        }
    }

    public BlockStateWrapper strippedState() {
        return this.lazyState.get();
    }

    public CompoundTag filter(CompoundTag properties) {
        for (String property : this.excludedProperties) {
            properties.remove(property);
        }
        return properties;
    }

    public boolean isValidTool(Item item) {
        if (this.tools.contains(item.id())) {
            return true;
        }
        for (Key tag : this.toolTags) {
            if (item.hasVanillaTag(tag) || item.hasPluginTag(tag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canUseOnBlockIfSecondaryUseActive(UseOnContext context, ImmutableBlockState state) {
        Item item = context.getItem();
        return !ItemUtils.isEmpty(item) && isValidTool(item);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        Player player = context.getPlayer();
        // no adventure mode for the moment
        if (player != null && player.isAdventureMode()) {
            return InteractionResult.PASS;
        }
        Item offHandItem = player != null ? player.getItemInHand(InteractionHand.OFF_HAND) : null;
        // is using a shield
        if (context.getHand() == InteractionHand.MAIN_HAND && !ItemUtils.isEmpty(offHandItem) && canBlockAttack(offHandItem) && !player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        Item item = context.getItem();
        if (ItemUtils.isEmpty(item) || !isValidTool(item)) {
            return InteractionResult.PASS;
        }
        BlockStateWrapper newState = strippedState();
        if (newState == null) {
            CraftEngine.instance().logger().warn("stripped block " + this.stripped + " does not exist");
            return InteractionResult.FAIL;
        }

        newState = newState.withProperties(filter(state.propertiesNbt()));
        BukkitExistingBlock clicked = (BukkitExistingBlock) context.getLevel().getBlock(context.getClickedPos());
        org.bukkit.entity.Player bukkitPlayer = null;
        if (player != null) {
            bukkitPlayer = ((org.bukkit.entity.Player) player.platformPlayer());
            // Call bukkit event
            EntityChangeBlockEvent event = new EntityChangeBlockEvent(bukkitPlayer, clicked.block(), BlockStateUtils.fromBlockData(newState.minecraftState()));
            if (EventUtils.fireAndCheckCancel(event)) {
                return InteractionResult.FAIL;
            }
        }

        BlockPos pos = context.getClickedPos();
        context.getLevel().playBlockSound(Vec3d.atCenterOf(pos), this.stripSound);
        LevelWriterProxy.INSTANCE.setBlock(context.getLevel().minecraftWorld(), LocationUtils.toBlockPos(pos), newState.minecraftState(), UpdateFlags.UPDATE_ALL_IMMEDIATE);
        LevelUtils.sendGameEvent(clicked.block().getWorld(), bukkitPlayer, GameEvent.BLOCK_CHANGE, new Vector(pos.x(), pos.y(), pos.z()));
        Material material = MaterialUtils.getMaterial(item.vanillaId());
        if (bukkitPlayer != null) {
            bukkitPlayer.setStatistic(Statistic.USE_ITEM, material, bukkitPlayer.getStatistic(Statistic.USE_ITEM, material) + 1);

            // resend swing if it's not interactable on client side
            if (!InteractUtils.isInteractable(
                    bukkitPlayer, BlockStateUtils.fromBlockData(state.visualBlockState().minecraftState()),
                    context.getHitResult(), item
            ) || player.isSecondaryUseActive()) {
                player.swingHand(context.getHand());
            }
            // shrink item amount
            item.hurtAndBreak(1, player, context.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
        return InteractionResult.SUCCESS_AND_CANCEL;
    }

    private static class Factory implements BlockBehaviorFactory<StrippableBlockBehavior> {
        private static final String[] EXCLUDED_PROPERTIES = ConfigKeys.of("excluded_properties");
        private static final String[] TOOLS = ConfigKeys.of("tool(s)");

        @Override
        public StrippableBlockBehavior create(BlockDefinition block, ConfigSection section) {
            List<Key> tools = new ArrayList<>();
            List<Key> toolTags = new ArrayList<>();
            for (String entry : section.getStringList(TOOLS, List.of("#" + ItemTags.AXES))) {
                if (entry.startsWith("#")) {
                    toolTags.add(Key.of(entry.substring(1)));
                } else {
                    tools.add(Key.of(entry));
                }
            }
            SoundData stripSound = section.getValue("sound", v -> SoundData.fromConfig(v, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.FIXED_1), DEFAULT_STRIP_SOUND);
            return new StrippableBlockBehavior(
                    block,
                    section.getNonNullString("stripped"),
                    section.getStringList(EXCLUDED_PROPERTIES),
                    tools,
                    toolTags,
                    stripSound
            );
        }
    }
}
