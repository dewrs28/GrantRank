package me.dewrs.listeners;

import me.dewrs.GrantRank;
import me.dewrs.managers.ActionInventoryManager;
import me.dewrs.managers.InventoryManager;
import me.dewrs.model.internal.InventoryPlayer;
import me.dewrs.utils.MessageUtils;
import me.dewrs.utils.PermissionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private GrantRank plugin;

    public PlayerListener(GrantRank plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        InventoryManager inventoryManager = plugin.getInventoryManager();
        InventoryPlayer inventoryPlayer = inventoryManager.getInventoryPlayer(player);
        if(inventoryPlayer == null){
            return;
        }
        inventoryManager.removeInventoryPlayer(inventoryPlayer);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        String latestVersion = plugin.getUpdateCheckerManager().getLatestVersion();
        if((PermissionUtils.isGrantRankAdmin(player) || player.isOp()) && !plugin.version.equals(latestVersion)){
            player.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage("&cThere is a new version of the plugin &8(&e"+latestVersion+"&8)"));
            player.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage("&cYou can download it at: &ehttps://spigotmc.org/resources/????"));
        }
    }
}
