package me.dewrs.commands;

import me.dewrs.GrantRank;
import me.dewrs.enums.GrantMenuType;
import me.dewrs.enums.SoundType;
import me.dewrs.managers.InventoryManager;
import me.dewrs.managers.UserDataManager;
import me.dewrs.model.CustomInventory;
import me.dewrs.model.ModifyData;
import me.dewrs.model.internal.InventoryPlayer;
import me.dewrs.utils.MessageUtils;
import me.dewrs.utils.OtherUtils;
import me.dewrs.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GrantCommand implements CommandExecutor {
    private GrantRank plugin;
    public GrantCommand(GrantRank plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage("&cOnly players can do this!"));
            return true;
        }
        Player player = (Player) sender;
        if(!PermissionUtils.canUseGrants(player)){
            player.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage(plugin.getMessagesManager().getNoPermission()));
            OtherUtils.playSound(player, 10, 2, SoundType.NO_PERM, plugin.getConfigManager());
            return true;
        }

        if(args.length == 0){
            player.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage(plugin.getMessagesManager().getNoArguments()
                    .replaceAll("%c%", "/grant (player)")));
            return true;
        }

        String nameTarget = args[0];
        if(nameTarget.length() > 16){
            player.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage(plugin.getMessagesManager().getNicknameTooLong()));
            return true;
        }
        UserDataManager userDataManager = plugin.getUserDataManager();
        CustomInventory customInventory = plugin.getInventoryManager().getCustomInventory("grants.yml");
        userDataManager.fetchStoredUUID(nameTarget)
                .thenAccept(uuid -> {
                    if (uuid == null) {
                        player.sendMessage(GrantRank.prefix + MessageUtils.getColoredMessage(plugin.getMessagesManager().getNoPlayer()));
                        return;
                    }
                    InventoryPlayer inventoryPlayer = new InventoryPlayer(player.getName(), customInventory, uuid, new ModifyData());
                    userDataManager.fetchStoredName(inventoryPlayer.getTargetUuid()).thenAccept(name -> {
                        inventoryPlayer.setTargetName(name);
                        Bukkit.getScheduler().runTask(plugin, () -> manageOpenInventory(player, customInventory, inventoryPlayer));
                    });
                });
        return true;
    }

    private void manageOpenInventory(Player player, CustomInventory customInventory, InventoryPlayer inventoryPlayer){
        int limit = (customInventory.getRows() - 2) * 9;
        InventoryManager inventoryManager = plugin.getInventoryManager();
        if(plugin.getConfigManager().getMenuType().equals(GrantMenuType.LUCKPERMS)) {
            inventoryManager.setupGrantPagination(inventoryPlayer, limit);
        }else{
            inventoryManager.setupCustomPagination(inventoryPlayer);
        }
        inventoryManager.createInventory(customInventory, inventoryPlayer, inv -> {
            player.openInventory(inv);
            inventoryManager.setInventoryPlayer(inventoryPlayer, customInventory);
            player.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage(plugin.getMessagesManager().getGrantsGuiOpen()
                    .replaceAll("%player%", inventoryPlayer.getTargetName())));
            OtherUtils.playSound(player,10, 2, SoundType.OPEN_INV, plugin.getConfigManager());
        });
    }
}