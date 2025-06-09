package me.dewrs.commands;

import me.dewrs.GrantRank;
import me.dewrs.enums.SoundType;
import me.dewrs.logger.LogMessage;
import me.dewrs.logger.LogSender;
import me.dewrs.managers.InventoryManager;
import me.dewrs.model.CustomInventory;
import me.dewrs.model.ModifyData;
import me.dewrs.model.internal.InventoryPlayer;
import me.dewrs.utils.MessageUtils;
import me.dewrs.utils.OtherUtils;
import me.dewrs.utils.PermissionUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GrantAdminCommand implements CommandExecutor, TabCompleter {
    private GrantRank plugin;
    public GrantAdminCommand(GrantRank plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(args.length == 0){
            help(sender);
            return true;
        }
        if(args[0].equalsIgnoreCase("reload")){
            reloadCommand(sender);
            return true;
        }

        if(!(sender instanceof Player)){
            LogSender.sendLogMessage(MessageUtils.getColoredMessage(LogMessage.ONLY_PLAYERS.format()));
            return true;
        }

        Player player = (Player) sender;
        if(args[0].equalsIgnoreCase("logs")){
            logsCommand(player);
            return true;
        }
        help(sender);
        return true;
    }

    private void reloadCommand(CommandSender sender){
        if(sender instanceof Player) {
            if (!PermissionUtils.isGrantRankAdmin((Player) sender)) {
                sender.sendMessage(GrantRank.prefix + MessageUtils.getColoredMessage(plugin.getMessagesManager().getNoPermission()));
                OtherUtils.playSound((Player) sender,10, 2, SoundType.NO_PERM, plugin.getConfigManager());
                return;
            }
        }
        plugin.reloadPlugin();
        sender.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage(plugin.getMessagesManager().getReload()));
    }

    private void logsCommand(Player player){
        if(!PermissionUtils.canViewGlobalLogs(player)){
            player.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage(plugin.getMessagesManager().getNoPermission()));
            OtherUtils.playSound(player,10, 2, SoundType.NO_PERM, plugin.getConfigManager());
            return;
        }
        InventoryManager inventoryManager = plugin.getInventoryManager();
        CustomInventory customInventory = plugin.getInventoryManager().getCustomInventory("nodes-logs.yml");
        InventoryPlayer inventoryPlayer = new InventoryPlayer(player.getName(), customInventory, new ModifyData());
        inventoryManager.setupNodeLogPagination(inventoryPlayer, customInventory, player, (customInventory.getRows() - 2) * 9);
        inventoryManager.setInventoryPlayer(inventoryPlayer, customInventory);
        OtherUtils.playSound(player,10, 2, SoundType.OPEN_INV, plugin.getConfigManager());
    }

    private void help(CommandSender sender){
        if(!PermissionUtils.isGrantRankAdmin((Player) sender)){
            sender.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage(plugin.getMessagesManager().getNoPermission()));
            return;
        }
        for(String m : plugin.getMessagesManager().getHelpAdmin()){
            sender.sendMessage(MessageUtils.getColoredMessage(m));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if(args.length == 1){
            List<String> arguments = new ArrayList<>();
            if(PermissionUtils.isGrantRankAdmin((Player) sender)){
                arguments.add("reload");
            }
            if(PermissionUtils.canViewGlobalLogs((Player) sender)){
                arguments.add("logs");
            }
            return arguments;
        }
        return null;
    }
}