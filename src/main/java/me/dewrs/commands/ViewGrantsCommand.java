package me.dewrs.commands;

import me.dewrs.GrantRank;
import me.dewrs.enums.SoundType;
import me.dewrs.managers.InventoryManager;
import me.dewrs.managers.UserDataManager;
import me.dewrs.model.CustomInventory;
import me.dewrs.model.ModifyData;
import me.dewrs.model.internal.InventoryPlayer;
import me.dewrs.utils.MessageUtils;
import me.dewrs.utils.OtherUtils;
import me.dewrs.utils.PermissionUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ViewGrantsCommand implements CommandExecutor {
    private GrantRank plugin;
    public ViewGrantsCommand(GrantRank plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(GrantRank.prefix+ MessageUtils.getColoredMessage("&cOnly players can do this!"));
            return true;
        }
        Player player = (Player) sender;
        if(!PermissionUtils.canViewUserLogs(player)){
            player.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage(plugin.getMessagesManager().getNoPermission()));
            OtherUtils.playSound(player, 10, 2, SoundType.NO_PERM, plugin.getConfigManager());
            return true;
        }

        if(args.length == 0){
            player.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage(plugin.getMessagesManager().getNoArguments()
                    .replaceAll("%c%", "/grants (player)")));
            return true;
        }

        String nameTarget = args[0];
        if(nameTarget.length() > 16){
            player.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage(plugin.getMessagesManager().getNicknameTooLong()));
            return true;
        }
        InventoryManager inventoryManager = plugin.getInventoryManager();
        UserDataManager userDataManager = plugin.getUserDataManager();
        CustomInventory customInventory = plugin.getInventoryManager().getCustomInventory("nodes-logs.yml");
        userDataManager.fetchStoredUUID(nameTarget)
                .thenAccept(uuid -> {
                    if (uuid == null) {
                        player.sendMessage(GrantRank.prefix + MessageUtils.getColoredMessage(plugin.getMessagesManager().getNoPlayer()));
                        return;
                    }
                    InventoryPlayer inventoryPlayer = new InventoryPlayer(player.getName(), customInventory, uuid, new ModifyData());
                    userDataManager.fetchStoredName(inventoryPlayer.getTargetUuid()).thenAccept(name -> {
                        inventoryPlayer.setTargetName(name);
                        inventoryManager.setupUserNodeLogPagination(inventoryPlayer, (customInventory.getRows() - 2) * 9, () -> {
                            player.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage(plugin.getMessagesManager().getLoadingUserLogs()
                                    .replaceAll("%player%", name)));
                            inventoryManager.createInventory(customInventory, inventoryPlayer, inv -> {
                                player.sendMessage(GrantRank.prefix+ MessageUtils.getColoredMessage(plugin.getMessagesManager().getGlobalLogsGuiOpen()));
                                player.openInventory(inv);
                                inventoryManager.setInventoryPlayer(inventoryPlayer, customInventory);
                                OtherUtils.playSound(player,10, 2, SoundType.OPEN_INV, plugin.getConfigManager());
                            });
                        });
                    });
                });
        return true;
    }
}
