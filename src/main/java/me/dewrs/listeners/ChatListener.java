package me.dewrs.listeners;

import me.dewrs.GrantRank;
import me.dewrs.enums.ChatModuleType;
import me.dewrs.model.ModifyData;
import me.dewrs.model.internal.InventoryPlayer;
import me.dewrs.utils.OtherUtils;
import me.dewrs.utils.MessageUtils;
import me.dewrs.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Objects;

public class ChatListener implements Listener {
    private GrantRank plugin;

    public ChatListener(GrantRank plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        InventoryPlayer inventoryPlayer = plugin.getInventoryManager().getInventoryPlayer(player);
        if(inventoryPlayer == null){
            return;
        }
        ModifyData modifyData = inventoryPlayer.getModifyData();
        if(!modifyData.isChatMode()){
            return;
        }
        event.setCancelled(true);
        String input = event.getMessage();
        if(input.equals("#")){
            runSyncLeftChat(inventoryPlayer, player, false);
            return;
        }
        ChatModuleType chatModuleType = modifyData.getChatSection();
        switch (chatModuleType){
            case TIME: {
                if(!TimeUtils.isValidTime(input)){
                    player.sendMessage(GrantRank.PREFIX+ MessageUtils.getColoredMessage(plugin.getMessagesManager().getInvalidTime()));
                    runSyncLeftChat(inventoryPlayer, player, false);
                    return;
                }

                long time;
                if (input.equalsIgnoreCase("perma") || input.equalsIgnoreCase("permanent")) {
                    time = -1;
                } else {
                    time = Objects.requireNonNull(TimeUtils.getDurationFromTime(input)).toMillis();
                }
                modifyData.setTime(time);

                runSyncLeftChat(inventoryPlayer, player, true);
                break;
            }
            case CONTEXT: {
                if(!OtherUtils.isValidContext(input)){
                    player.sendMessage(GrantRank.PREFIX+MessageUtils.getColoredMessage(plugin.getMessagesManager().getInvalidContext()));
                    runSyncLeftChat(inventoryPlayer, player, false);
                    return;
                }

                ArrayList<String> contexts = modifyData.getContexts();
                if(contexts == null){
                    contexts = new ArrayList<>();
                    contexts.add(input);
                }else{
                    contexts.add(input);
                }

                modifyData.setContexts(contexts);

                runSyncLeftChat(inventoryPlayer, player, true);
                break;
            }
            case REASON: {
                modifyData.setReason(input);
                runSyncLeftChat(inventoryPlayer, player, true);
                break;
            }
            case PERMISSION: {
                if(!OtherUtils.isValidPermission(input)){
                    player.sendMessage(GrantRank.PREFIX+MessageUtils.getColoredMessage(plugin.getMessagesManager().getInvalidPermission()));
                    runSyncLeftChat(inventoryPlayer, player, false);
                    return;
                }
                modifyData.setPermission(input);

                runSyncLeftChat(inventoryPlayer, player, true);
                break;
            }
        }
    }

    private void runSyncLeftChat(InventoryPlayer inventoryPlayer, Player player, boolean successful){
        Bukkit.getScheduler().runTask(plugin, () -> plugin.getActionInventoryManager().leftChatMode(inventoryPlayer,player,successful));
    }
}