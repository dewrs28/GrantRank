package me.dewrs.managers;

import me.dewrs.GrantRank;
import me.dewrs.enums.CustomActionType;
import me.dewrs.enums.GrantMenuType;
import me.dewrs.enums.NodeType;
import me.dewrs.logger.LogSender;
import me.dewrs.model.CustomInventory;
import me.dewrs.model.CustomItem;
import me.dewrs.model.ModifyData;
import me.dewrs.model.NodeLog;
import me.dewrs.model.internal.InventoryPlayer;
import me.dewrs.utils.ItemUtils;
import me.dewrs.utils.MessageUtils;
import me.dewrs.utils.OtherUtils;
import me.dewrs.utils.TimeUtils;
import net.luckperms.api.context.Context;
import net.luckperms.api.context.MutableContextSet;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class InventoryManager {

    private ArrayList<CustomInventory> inventories;
    private ArrayList<InventoryPlayer> players;
    private GrantRank plugin;

    public InventoryManager(GrantRank plugin) {
        this.plugin = plugin;
        inventories = new ArrayList<>();
        players = new ArrayList<>();
    }

    @SuppressWarnings("All")
    public Inventory createInventory(CustomInventory customInventory, InventoryPlayer inventoryPlayer) {
        Inventory inv = Bukkit.createInventory(null, 9 * customInventory.getRows(), MessageUtils.getColoredMessage(customInventory.getTitle()
                .replaceAll("%player%", inventoryPlayer.getTargetName())));
        String path = customInventory.getInv();
        if(path.startsWith("grants.yml")){
            if (plugin.getConfigManager().getMenuType().equals(GrantMenuType.LUCKPERMS)){
                setItemGrantLPInInventory(customInventory, inv, inventoryPlayer);
            }else{
                setItemGrantCustom(inv, inventoryPlayer);
            }
        }else if(path.startsWith("nodes-logs.yml")){
            setItemNodeLogInInventory(customInventory, inv, inventoryPlayer);
        }
        for (CustomItem item : customInventory.getCustomItems()) {
            if (item.getCustomActionType().equals(CustomActionType.GRANT)
                    || item.getCustomActionType().equals(CustomActionType.NODE_LOG)) continue;
            ItemStack itemStack = ItemUtils.getItemStackFromCustomItem(item);
            itemStack = replaceItemVariables(itemStack, path, inventoryPlayer);
            setItemInInventory(inv, itemStack, item.getSlot());
        }
        return inv;
    }

    public void setupGrantPagination(InventoryPlayer inventoryPlayer, int maxItemsPerPage) {
        Map<Group, Integer> groupMap = orderGrantItems(plugin.getHookManager().sortWeightGroupsLuckPerms());
        List<Group> allGroups = new ArrayList<>(groupMap.keySet());

        List<List<Group>> pages = new ArrayList<>();
        for (int i = 0; i < allGroups.size(); i += maxItemsPerPage) {
            int end = Math.min(i + maxItemsPerPage, allGroups.size());
            pages.add(allGroups.subList(i, end));
        }

        inventoryPlayer.setPaginatedGroups(pages);
    }

    public void setupCustomPagination(InventoryPlayer inventoryPlayer) {
        Map<Integer, Map<String, CustomItem>> groupMap = plugin.getRanksConfigManager().getRankCustomItems();
        Map<Integer, List<Map.Entry<Group, CustomItem>>> pages = new LinkedHashMap<>();

        for (Map.Entry<Integer, Map<String, CustomItem>> pageEntry : groupMap.entrySet()) {
            int pageNumber = pageEntry.getKey();

            Map<String, CustomItem> groupItems = pageEntry.getValue();
            List<Map.Entry<Group, CustomItem>> groupEntries = new ArrayList<>();
            for (Map.Entry<String, CustomItem> entry : groupItems.entrySet()) {
                CustomItem customItem = entry.getValue();
                Group group = plugin.getLuckPermsApi().getGroupManager().getGroup(entry.getKey());
                if (group == null) {
                    continue;
                }
                groupEntries.add(new AbstractMap.SimpleEntry<>(group, customItem));
            }
            pages.put(pageNumber, groupEntries);
        }
        List<List<Map.Entry<Group, CustomItem>>> orderedPages = new ArrayList<>();
        for (int i = 1; i <= pages.size(); i++) {
            orderedPages.add(pages.getOrDefault(i, Collections.emptyList()));
        }
        inventoryPlayer.setPaginatedCustomGroups(orderedPages);
    }

    public void setupNodeLogPagination(InventoryPlayer inventoryPlayer, CustomInventory customInventory, Player player, int maxItemsPerPage) {
        plugin.getStorageManager().getNodeLogs(nodeLogs -> {
            List<List<NodeLog>> pages = new ArrayList<>();
            List<NodeLog> currentPage = new ArrayList<>();
            for (NodeLog log : nodeLogs.values()) {
                if (currentPage.size() >= maxItemsPerPage) {
                    pages.add(currentPage);
                    currentPage = new ArrayList<>();
                }
                currentPage.add(log);
            }
            if (!currentPage.isEmpty()) {
                pages.add(currentPage);
            }
            inventoryPlayer.setPaginatedNodeLogs(pages);
            player.openInventory(createInventory(customInventory, inventoryPlayer));
        });
    }

    @SuppressWarnings("All")
    private ItemStack replaceItemVariables(ItemStack itemStack, String path, InventoryPlayer inventoryPlayer){
        ActionInventoryManager actionInventoryManager = plugin.getActionInventoryManager();
        HookManager hookManager = plugin.getHookManager();
        ModifyData modifyData = inventoryPlayer.getModifyData();
        switch (path) {
            case "grant-finish.yml":
            case "contexts.yml": {
                if (path.equals("grant-finish.yml")) {
                    Map<String, String> variables = new HashMap<>();
                    variables.put("%time%", TimeUtils.getTimeFromMilis(modifyData.getTime()));
                    if(OtherUtils.getNodeType(modifyData) == NodeType.RANK) {
                        String prefix = hookManager.getGroupPrefixLuckPerms(modifyData.getRank());
                        if(prefix != null) variables.put("%node%", prefix);
                    }else if(OtherUtils.getNodeType(modifyData) == NodeType.PERMISSION){
                        variables.put("%node%", modifyData.getPermission());
                    }
                    itemStack = ItemUtils.getItemStackReplaceVariables(itemStack, variables);
                }
                itemStack = ItemUtils.getItemStackReplaceMultiLine(itemStack, "%contexts%", modifyData.getContexts());
                break;
            }
        }
        return itemStack;
    }

    private void setItemGrantCustom(Inventory inventory, InventoryPlayer inventoryPlayer) {
        int page = inventoryPlayer.getActualPage();
        List<Map.Entry<Group, CustomItem>> groupsToShow = inventoryPlayer.getCustomGroupsForPage(page);
        removeNoUsedItems(0, inventoryPlayer.getCustomInventory());
        for (Map.Entry<Group, CustomItem> entry : groupsToShow) {
            Group group = entry.getKey();
            CustomItem customItem = entry.getValue();
            inventoryPlayer.getCustomInventory().getCustomItems().add(customItem);

            Map<String, String> variables = new HashMap<>();
            String prefix = plugin.getHookManager().getGroupPrefixLuckPerms(group);
            variables.put("%prefix%", prefix != null ? prefix : group.getName());
            variables.put("%name%", group.getName());
            ItemStack itemStack = ItemUtils.getItemStackFromCustomItem(customItem);
            ItemStack itemStackWithVariables = ItemUtils.getItemStackReplaceVariables(itemStack, variables);

            customItem.setGrantToGive(group);
            inventory.setItem(customItem.getSlot(), itemStackWithVariables);
        }
    }

    private void setItemGrantLPInInventory(CustomInventory customInventory, Inventory inventory, InventoryPlayer inventoryPlayer) {
        int page = inventoryPlayer.getActualPage();
        List<Group> groupsToShow = inventoryPlayer.getGroupsForPage(page);

        int slot = 0;
        removeNoUsedItems(slot, customInventory);
        for (Group group : groupsToShow) {
            CustomItem customItem = ItemUtils.cloneCustomItem(plugin.getConfigManager().getItemLpGrantsMenu());

            inventoryPlayer.getCustomInventory().getCustomItems().add(customItem);
            ItemStack itemStack = ItemUtils.getItemStackFromCustomItem(customItem);

            Map<String, String> variables = new HashMap<>();
            String prefix = plugin.getHookManager().getGroupPrefixLuckPerms(group);
            variables.put("%prefix%", prefix != null ? prefix : group.getName());
            variables.put("%name%", group.getName());

            ItemStack itemStackWithVariables = ItemUtils.getItemStackReplaceVariables(itemStack, variables);
            customItem.setGrantToGive(group);
            customItem.setSlot(slot);

            inventory.setItem(slot, itemStackWithVariables);
            slot++;
        }
    }

    private Map<Group, Integer> orderGrantItems(Map<Group, Integer> listGroups) {
        Map<Group, Integer> newGroups = new LinkedHashMap<>(listGroups);
        mainLoop:
        for (Map.Entry<Group, Integer> entry : listGroups.entrySet()){
            Group group = entry.getKey();
            for(String g : plugin.getConfigManager().getExcludeLpGroups()){
                if(group.getName().equals(g)){
                    newGroups.remove(group);
                    continue mainLoop;
                }
            }
        }
        return newGroups;
    }

    private void removeNoUsedItems(int limit, CustomInventory customInventory) {
        customInventory.getCustomItems().removeIf(item -> item.getSlot() >= limit && item.getSlot() < (customInventory.getRows() - 2) * 9);
    }

    @SuppressWarnings("All")
    private void setItemNodeLogInInventory(CustomInventory customInventory, Inventory inventory, InventoryPlayer inventoryPlayer) {
        int page = inventoryPlayer.getActualPage();
        List<NodeLog> nodesToShow = inventoryPlayer.getNodesForPage(page);
        int slot = 0;
        for (NodeLog nodeLog : nodesToShow) {
            String id = nodeLog.getId()+"";
            String nameUser = nodeLog.getName_user();
            String nameOperator = nodeLog.getName_operator();
            String node = nodeLog.getNode();
            String duration = TimeUtils.getTimeFromMilis(nodeLog.getExpiry());
            String reason = nodeLog.getReason();
            String date = TimeUtils.getDateFromMillis(nodeLog.getCreation_time());
            MutableContextSet contextSet = nodeLog.getContextSet();
            ArrayList<String> contextList = new ArrayList<>();
            for (Context c : contextSet.toSet()) {
                String key = c.getKey();
                String value = c.getValue();
                contextList.add(key + "=" + value);
            }
            CustomItem customItem = ItemUtils.cloneCustomItem(plugin.getConfigManager().getItemNodeLog());
            ItemStack itemStack = ItemUtils.getItemStackFromCustomItem(customItem);
            Map<String, String> variables = new HashMap<>();
            variables.put("%id%", id);
            variables.put("%node%", node);
            variables.put("%user%", nameUser);
            variables.put("%operator%", nameOperator);
            variables.put("%duration%", duration);
            variables.put("%reason%", reason);
            variables.put("%date%", date);
            ItemStack itemStackWithVariables = ItemUtils.getItemStackReplaceVariables(itemStack, variables);
            itemStackWithVariables = ItemUtils.getItemStackReplaceMultiLine(itemStack, "%contexts%", contextList);
            inventory.setItem(slot, itemStackWithVariables);
            slot++;
        }
    }

    private void setItemInInventory(Inventory inventory, ItemStack itemStack, int slot) {
        inventory.setItem(slot, itemStack);
    }

    public CustomInventory getCustomInventory(String path) {
        for (CustomInventory customInventory : inventories) {
            if (customInventory.getInv().equals(path)) {
                return customInventory;
            }
        }
        return null;
    }

    public InventoryPlayer getInventoryPlayer(Player player) {
        for (InventoryPlayer inventoryPlayer : players) {
            if (inventoryPlayer.getName().equals(player.getName())) {
                return inventoryPlayer;
            }
        }
        return null;
    }

    public CustomItem getCustomItemBySlot(int slot, InventoryPlayer inventoryPlayer) {
        for (CustomItem i : inventoryPlayer.getCustomInventory().getCustomItems()) {
            if (i.getSlot() == slot) {
                return i;
            }
        }
        return null;
    }

    public void setInventoryPlayer(InventoryPlayer inventoryPlayer, CustomInventory customInventory) {
        if (!players.contains(inventoryPlayer)) {
            players.add(inventoryPlayer);
        }
        inventoryPlayer.setCustomInventory(customInventory);
    }

    public void removeInventoryPlayer(InventoryPlayer inventoryPlayer){
        players.remove(inventoryPlayer);
    }

    public ArrayList<CustomInventory> getInventories() {
        return inventories;
    }

    public void setInventories(ArrayList<CustomInventory> inventories) {
        this.inventories = inventories;
    }

    public ArrayList<InventoryPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<InventoryPlayer> players) {
        this.players = players;
    }
}