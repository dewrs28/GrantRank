package me.dewrs.listeners;

import me.dewrs.GrantRank;
import me.dewrs.managers.ActionInventoryManager;
import me.dewrs.managers.InventoryManager;
import me.dewrs.model.CustomItem;
import me.dewrs.model.ModifyData;
import me.dewrs.model.internal.InventoryPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryListener implements Listener {
    private GrantRank plugin;

    public InventoryListener(GrantRank plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        InventoryManager inventoryManager = plugin.getInventoryManager();
        ActionInventoryManager actionInventoryManager = plugin.getActionInventoryManager();
        InventoryPlayer inventoryPlayer = inventoryManager.getInventoryPlayer(player);
        if(inventoryPlayer == null) return;
        event.setCancelled(true);
        int slot = event.getSlot();
        CustomItem customItem = plugin.getInventoryManager().getCustomItemBySlot(slot, inventoryPlayer);
        if(customItem == null) return;
        actionInventoryManager.manageActionType(customItem, inventoryPlayer);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        InventoryManager inventoryManager = plugin.getInventoryManager();
        InventoryPlayer inventoryPlayer = inventoryManager.getInventoryPlayer((Player) event.getPlayer());
        if(inventoryPlayer == null) {
            return;
        }
        ModifyData modifyData = inventoryPlayer.getModifyData();
        if(modifyData.isChatMode()) {
            return;
        }
        if (!inventoryPlayer.isNavigating()) {
            inventoryPlayer.resetPagination();
        }
        inventoryManager.removeInventoryPlayer(inventoryPlayer);
    }
}
