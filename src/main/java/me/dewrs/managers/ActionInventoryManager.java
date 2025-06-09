package me.dewrs.managers;

import me.dewrs.GrantRank;
import me.dewrs.enums.ChatModuleType;
import me.dewrs.enums.CustomActionType;
import me.dewrs.enums.GrantMenuType;
import me.dewrs.enums.SoundType;
import me.dewrs.model.CustomInventory;
import me.dewrs.model.CustomItem;
import me.dewrs.model.ModifyData;
import me.dewrs.model.internal.InventoryPlayer;
import me.dewrs.utils.MessageUtils;
import me.dewrs.utils.OtherUtils;
import me.dewrs.utils.PermissionUtils;
import me.dewrs.utils.TimeUtils;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ActionInventoryManager {
    private GrantRank plugin;

    public ActionInventoryManager(GrantRank plugin) {
        this.plugin = plugin;
    }

    public void manageActionType(CustomItem customItem, InventoryPlayer inventoryPlayer){
        Player player = Bukkit.getPlayerExact(inventoryPlayer.getName());
        if(player == null){
            return;
        }
        CustomActionType customActionType = customItem.getCustomActionType();
        switch (customActionType) {
            case OPEN_INVENTORY: {
                manageOpenInventory(inventoryPlayer, player, customItem);
                break;
            }
            case SET_TIME: {
                manageSetTime(inventoryPlayer, player, customItem);
                break;
            }
            case SET_CONTEXT: {
                manageSetContext(inventoryPlayer, player);
                break;
            }
            case GRANT_GIVE: {
                manageGrantGive(inventoryPlayer, player);
                break;
            }
            case GRANT: {
                manageGrant(inventoryPlayer, player, customItem);
                break;
            }
            case ADD_PERMISSION: {
                manageAddPermission(inventoryPlayer, player);
                break;
            }
            case NEXT: {
                manageNextPage(inventoryPlayer, player);
                break;
            }
            case BACK: {
                manageBackPage(inventoryPlayer, player);
                break;
            }
            case FINISH_GRANT: {
                manageFinishGrant(inventoryPlayer, player, customItem);
                break;
            }
            default: break;
        }
    }

    private void manageNextPage(InventoryPlayer inventoryPlayer, Player player){
        int page = inventoryPlayer.getActualPage()+1;
        int totalPages;
        if(inventoryPlayer.getCustomInventory().getInv().startsWith("grants")){
            if(plugin.getConfigManager().getMenuType() == GrantMenuType.LUCKPERMS) {
                totalPages = inventoryPlayer.getTotalPages();
            }else{
                totalPages = inventoryPlayer.getTotalPagesCustom();
            }
        }else{
            totalPages = inventoryPlayer.getTotalNodeLogPages();
        }
        if (totalPages-1 < page) {
            return;
        }
        InventoryManager inventoryManager = plugin.getInventoryManager();
        CustomInventory customInventory = OtherUtils.cloneCustomInventory(inventoryPlayer.getCustomInventory());
        String pathInventory = OtherUtils.replaceEndingNumbers(inventoryPlayer.getCustomInventory().getInv(), page);

        customInventory.setInv(pathInventory);
        inventoryPlayer.setActualPage(page);
        inventoryPlayer.setCustomInventory(customInventory);

        inventoryPlayer.setNavigating(true);
        player.openInventory(inventoryManager.createInventory(customInventory, inventoryPlayer));
        inventoryPlayer.setNavigating(false);
        inventoryManager.setInventoryPlayer(inventoryPlayer, customInventory);
        OtherUtils.playSound(player,10, 2, SoundType.OPEN_INV, plugin.getConfigManager());
    }

    private void manageBackPage(InventoryPlayer inventoryPlayer, Player player){
        if(inventoryPlayer.getActualPage() == 0){
            return;
        }
        int page = inventoryPlayer.getActualPage()-1;
        int totalPages;
        if(inventoryPlayer.getCustomInventory().getInv().startsWith("grants")){
            if(plugin.getConfigManager().getMenuType() == GrantMenuType.LUCKPERMS) {
                totalPages = inventoryPlayer.getTotalPages();
            }else{
                totalPages = inventoryPlayer.getTotalPagesCustom();
            }
        }else{
            totalPages = inventoryPlayer.getTotalNodeLogPages();
        }
        if (totalPages-1 < page) {
            return;
        }
        InventoryManager inventoryManager = plugin.getInventoryManager();
        CustomInventory customInventory = OtherUtils.cloneCustomInventory(inventoryPlayer.getCustomInventory());
        String pathInventory = OtherUtils.replaceEndingNumbers(inventoryPlayer.getCustomInventory().getInv(), page);

        customInventory.setInv(pathInventory);
        inventoryPlayer.setActualPage(page);
        inventoryPlayer.setCustomInventory(customInventory);

        inventoryPlayer.setNavigating(true);
        player.openInventory(inventoryManager.createInventory(customInventory, inventoryPlayer));
        inventoryPlayer.setNavigating(false);
        inventoryManager.setInventoryPlayer(inventoryPlayer, customInventory);
        OtherUtils.playSound(player,10, 2, SoundType.OPEN_INV, plugin.getConfigManager());
    }

    private void manageAddPermission(InventoryPlayer inventoryPlayer, Player player){
        if(!PermissionUtils.canGivePermission(player)){
            player.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage(plugin.getMessagesManager().getNoPermission()));
            player.closeInventory();
            return;
        }
        ModifyData modifyData = inventoryPlayer.getModifyData();
        modifyData.setChatSection(ChatModuleType.PERMISSION);
        manageChatMode(inventoryPlayer, player);
    }

    private void manageGrant(InventoryPlayer inventoryPlayer, Player player, CustomItem customItem){
        Group rank = customItem.getGrantToGive();
        if(!PermissionUtils.canGrantRank(player, rank.getName())){
            player.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage(plugin.getMessagesManager().getNoPermission()));
            player.closeInventory();
            OtherUtils.playSound(player,10, 2, SoundType.NO_PERM, plugin.getConfigManager());
            return;
        }
        InventoryManager inventoryManager = plugin.getInventoryManager();
        CustomInventory customInventory = inventoryManager.getCustomInventory("select-time.yml");
        ModifyData modifyData = inventoryPlayer.getModifyData();
        modifyData.setRank(rank);
        player.openInventory(inventoryManager.createInventory(customInventory, inventoryPlayer));
        inventoryManager.setInventoryPlayer(inventoryPlayer, customInventory);
        OtherUtils.playSound(player,10, 2, SoundType.OPEN_INV, plugin.getConfigManager());
    }

    private void manageOpenInventory(InventoryPlayer inventoryPlayer, Player player, CustomItem customItem){
        InventoryManager inventoryManager = plugin.getInventoryManager();
        CustomInventory customInventory = inventoryManager.getCustomInventory(customItem.getInventoryToOpen());
        player.openInventory(inventoryManager.createInventory(customInventory, inventoryPlayer));
        inventoryManager.setInventoryPlayer(inventoryPlayer, customInventory);
        OtherUtils.playSound(player,10, 2, SoundType.OPEN_INV, plugin.getConfigManager());
    }

    private void manageFinishGrant(InventoryPlayer inventoryPlayer, Player player, CustomItem customItem){
        customItem.setInventoryToOpen("grant-finish.yml");
        manageOpenInventory(inventoryPlayer, player, customItem);
    }

    private void manageSetTime(InventoryPlayer inventoryPlayer, Player player, CustomItem customItem) {
        String input = customItem.getCacheFormat();
        ModifyData modifyData = inventoryPlayer.getModifyData();

        if (input.equalsIgnoreCase("custom")) {
            modifyData.setChatSection(ChatModuleType.TIME);
            manageChatMode(inventoryPlayer, player);
            return;
        }
        if (!TimeUtils.isValidTime(input)) {
            player.closeInventory();
            player.sendMessage(GrantRank.prefix + MessageUtils.getColoredMessage(plugin.getMessagesManager().getInvalidTime()));
            return;
        }

        long time;
        if (input.equalsIgnoreCase("perma") || input.equalsIgnoreCase("permanent")) {
            time = -1;
        } else {
            time = Objects.requireNonNull(TimeUtils.getDurationFromTime(input)).toMillis();
        }
        modifyData.setTime(time);

        InventoryManager inventoryManager = plugin.getInventoryManager();
        CustomInventory customInventory = inventoryManager.getCustomInventory("contexts.yml");
        player.openInventory(inventoryManager.createInventory(customInventory, inventoryPlayer));
        inventoryManager.setInventoryPlayer(inventoryPlayer, customInventory);
        OtherUtils.playSound(player,10, 2, SoundType.OPEN_INV, plugin.getConfigManager());
    }

    private void manageSetContext(InventoryPlayer inventoryPlayer, Player player){
        ModifyData modifyData = inventoryPlayer.getModifyData();
        modifyData.setChatSection(ChatModuleType.CONTEXT);
        manageChatMode(inventoryPlayer, player);
    }

    private void manageGrantGive(InventoryPlayer inventoryPlayer, Player player){
        ModifyData modifyData = inventoryPlayer.getModifyData();
        modifyData.setChatSection(ChatModuleType.REASON);
        manageChatMode(inventoryPlayer, player);
    }

    private void manageChatMode(InventoryPlayer inventoryPlayer, Player player){
        ModifyData modifyData = inventoryPlayer.getModifyData();
        modifyData.setChatMode(true);
        player.closeInventory();
        ChatModuleType chatModuleType = modifyData.getChatSection();
        switch (chatModuleType){
            case TIME: {
                player.sendMessage(MessageUtils.getColoredMessage(plugin.getMessagesManager().getChatTime()));
                break;
            }
            case CONTEXT: {
                player.sendMessage(MessageUtils.getColoredMessage(plugin.getMessagesManager().getChatContext()));
                break;
            }
            case REASON: {
                player.sendMessage(MessageUtils.getColoredMessage(plugin.getMessagesManager().getChatReason()));
                break;
            }
            case PERMISSION: {
                player.sendMessage(MessageUtils.getColoredMessage(plugin.getMessagesManager().getChatPermission()));
                break;
            }
        }
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            leftChatMode(inventoryPlayer, player,false);
        }, 300L);
        modifyData.setBukkitTask(task);
    }

    public void leftChatMode(InventoryPlayer inventoryPlayer, Player player, boolean successful){
        ModifyData modifyData = inventoryPlayer.getModifyData();
        BukkitTask task = modifyData.getBukkitTask();
        if(task != null && task.getOwner().getName().equals("GrantRank")) Bukkit.getScheduler().cancelTask(modifyData.getBukkitTask().getTaskId());
        modifyData.setChatMode(false);
        ChatModuleType chatModuleType = modifyData.getChatSection();
        InventoryManager inventoryManager = plugin.getInventoryManager();
        switch (chatModuleType){
            case TIME: {
                CustomInventory customInventory;
                if(successful){
                    customInventory = inventoryManager.getCustomInventory("contexts.yml");
                    player.openInventory(inventoryManager.createInventory(customInventory, inventoryPlayer));
                    inventoryManager.setInventoryPlayer(inventoryPlayer, customInventory);
                }else{
                    customInventory = inventoryPlayer.getCustomInventory();
                    player.openInventory(inventoryManager.createInventory(customInventory, inventoryPlayer));
                }
                OtherUtils.playSound(player,10, 2, SoundType.OPEN_INV, plugin.getConfigManager());
                break;
            }
            case CONTEXT: {
                CustomInventory customInventory = inventoryPlayer.getCustomInventory();
                player.openInventory(inventoryManager.createInventory(customInventory, inventoryPlayer));
                OtherUtils.playSound(player,10, 2, SoundType.OPEN_INV, plugin.getConfigManager());
                break;
            }
            case REASON: {
                if(successful) {
                    plugin.getUserDataManager().giveNodeToPlayer(inventoryPlayer, player);
                    inventoryManager.removeInventoryPlayer(inventoryPlayer);
                }else{
                    player.openInventory(inventoryManager.createInventory(inventoryPlayer.getCustomInventory(), inventoryPlayer));
                    OtherUtils.playSound(player,10, 2, SoundType.OPEN_INV, plugin.getConfigManager());
                }
                break;
            }
            case PERMISSION: {
                CustomInventory customInventory;
                if(successful){
                    customInventory = inventoryManager.getCustomInventory("select-time.yml");
                    player.openInventory(inventoryManager.createInventory(customInventory, inventoryPlayer));
                    inventoryManager.setInventoryPlayer(inventoryPlayer, customInventory);
                }else{
                    customInventory = inventoryPlayer.getCustomInventory();
                    player.openInventory(inventoryManager.createInventory(customInventory, inventoryPlayer));
                }
                OtherUtils.playSound(player,10, 2, SoundType.OPEN_INV, plugin.getConfigManager());
                break;
            }
        }
        modifyData.setBukkitTask(null);
        modifyData.setChatSection(null);
    }
}