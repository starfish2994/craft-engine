package net.momirealms.craftengine.bukkit.compatibility.denizen;

import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.ScriptEvent;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import net.momirealms.craftengine.bukkit.compatibility.denizen.commands.PlaceCEBlockCommand;
import net.momirealms.craftengine.bukkit.compatibility.denizen.commands.RemoveCEBlockCommand;
import net.momirealms.craftengine.bukkit.compatibility.denizen.commands.RemoveCEFurnitureCommand;
import net.momirealms.craftengine.bukkit.compatibility.denizen.commands.SpawnCEFurnitureCommand;
import net.momirealms.craftengine.bukkit.compatibility.denizen.events.*;
import net.momirealms.craftengine.bukkit.compatibility.denizen.tags.CraftEngineTags;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public final class DenizenHook {
    private DenizenHook() {}

    public static void register() {
        // script events
        ScriptEvent.registerScriptEvent(PlayerBreaksCEBlockScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerBreaksCEFurnitureScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerPlacesCEBlockScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerPlacesCEFurnitureScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerAttemptsPlaceCEBlockScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerAttemptsPlaceCEFurnitureScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerClicksCEBlockScriptEvent.class);
        ScriptEvent.registerScriptEvent(PlayerClicksCEFurnitureScriptEvent.class);
        ScriptEvent.registerScriptEvent(CEReloadedScriptEvent.class);
        // script commands
        DenizenCore.commandRegistry.registerCommand(PlaceCEBlockCommand.class);
        DenizenCore.commandRegistry.registerCommand(SpawnCEFurnitureCommand.class);
        DenizenCore.commandRegistry.registerCommand(RemoveCEFurnitureCommand.class);
        DenizenCore.commandRegistry.registerCommand(RemoveCEBlockCommand.class);
        // object tags
        CraftEngineTags.register();
        // Denizen 会把事件行中 "namespace:id" 形式的词解析为开关（switch），
        // 需要把 CraftEngine 的命名空间登记为 notSwitches，事件匹配器才能写带命名空间的 id
        registerNamespacesAsNotSwitches();
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onCraftEngineReload(CraftEngineReloadEvent event) {
                registerNamespacesAsNotSwitches();
                // CE 内容异步加载完成时，Denizen 通常已解析过脚本（事件路径在缺少命名空间时构建失败），
                // 重建 Denizen 的事件索引，使带命名空间匹配器的事件路径重新注册
                ScriptEvent.reload();
            }
        }, BukkitCraftEngine.instance().javaPlugin());
    }

    public static void registerNamespacesAsNotSwitches() {
        collectNamespaces(CraftEngineBlocks.loadedBlocks());
        collectNamespaces(CraftEngineItems.loadedItems());
        collectNamespaces(CraftEngineFurniture.loadedFurniture());
    }

    private static void collectNamespaces(Map<Key, ?> map) {
        for (Key key : map.keySet()) {
            ScriptEvent.ScriptPath.notSwitches.add(key.namespace());
        }
    }
}
