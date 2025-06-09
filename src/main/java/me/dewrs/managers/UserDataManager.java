package me.dewrs.managers;

import me.dewrs.GrantRank;
import me.dewrs.enums.NodeType;
import me.dewrs.enums.SoundType;
import me.dewrs.logger.LogSender;
import me.dewrs.model.ModifyData;
import me.dewrs.model.NodeLog;
import me.dewrs.model.internal.InventoryPlayer;
import me.dewrs.utils.MessageUtils;
import me.dewrs.utils.OtherUtils;
import me.dewrs.utils.PermissionUtils;
import me.dewrs.utils.TimeUtils;
import net.luckperms.api.context.MutableContextSet;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserDataManager {
    private GrantRank plugin;
    public UserDataManager(GrantRank plugin){
        this.plugin = plugin;
    }

    public CompletableFuture<UUID> fetchStoredUUID(String username) {
        return plugin.getLuckPermsApi().getUserManager()
                .lookupUniqueId(username)
                .thenCompose(CompletableFuture::completedFuture);
    }

    public CompletableFuture<String> fetchStoredName(UUID uuid) {
        return plugin.getLuckPermsApi().getUserManager()
                .lookupUsername(uuid)
                .thenCompose(CompletableFuture::completedFuture);
    }

    public void giveNodeToPlayer(InventoryPlayer inventoryPlayer, Player player){
        ModifyData modifyData = inventoryPlayer.getModifyData();
        NodeType nodeType = OtherUtils.getNodeType(modifyData);

        String nameUser = inventoryPlayer.getTargetName();
        String nameOperator = inventoryPlayer.getName();
        String timeString = TimeUtils.getTimeFromMilis(modifyData.getTime());
        long expiry = modifyData.getTime();
        String reason = modifyData.getReason();
        ArrayList<String> contexts = modifyData.getContexts();

        if(contexts == null){
            contexts = new ArrayList<>();
        }
        MutableContextSet contextSet = MutableContextSet.create();
        for(String s : contexts){
            String[] split = s.split("=");
            contextSet.add(split[0], split[1]);
        }

        Node node;
        if(expiry == -1) {
            if(nodeType == NodeType.RANK) {
                node = InheritanceNode.builder(modifyData.getRank())
                        .context(contextSet)
                        .build();
            }else{
                node = PermissionNode.builder(modifyData.getPermission())
                        .context(contextSet)
                        .build();
            }
        }else{
            if(nodeType == NodeType.RANK) {
                node = InheritanceNode.builder(modifyData.getRank())
                        .expiry(Duration.ofMillis(expiry))
                        .context(contextSet)
                        .build();
            }else{
                node = PermissionNode.builder(modifyData.getPermission())
                        .expiry(Duration.ofMillis(expiry))
                        .context(contextSet)
                        .build();
            }
        }

        UUID uuid = inventoryPlayer.getTargetUuid();
        plugin.getLuckPermsApi().getUserManager().modifyUser(uuid, user -> {
            user.data().add(node);
            long creation_time = System.currentTimeMillis();
            NodeLog nodeLog = new NodeLog(uuid, nameUser, nameOperator, node.getKey(), expiry, reason, contextSet, creation_time);
            plugin.getStorageManager().createNodeLog(nodeLog, () -> {
                manageChatSuccessMessages(inventoryPlayer, player, modifyData, timeString, nodeType);
            });
        });
    }

    private void manageChatSuccessMessages(InventoryPlayer inventoryPlayer, Player player, ModifyData modifyData, String timeString, NodeType nodeType){
        String nameUser = inventoryPlayer.getTargetName();
        String nameOperator = inventoryPlayer.getName();

        if(player == null){
            return;
        }
        StringBuilder contextsMessage = new StringBuilder();
        int i = 1;
        for(String s : modifyData.getContexts()){
            if(modifyData.getContexts().size() != i) {
                contextsMessage.append(s).append(", ");
            }else{
                contextsMessage.append(s);
            }
            i++;
        }

        String messageNotify;
        if(nodeType == NodeType.RANK){
            messageNotify = plugin.getMessagesManager().getGrantSuccessNotify()
                    .replaceAll("%player%", nameUser)
                    .replaceAll("%contexts%", contextsMessage.toString())
                    .replaceAll("%rank%", modifyData.getRank().getName())
                    .replaceAll("%time%", timeString)
                    .replaceAll("%operator%", nameOperator);
        }else{
            messageNotify = plugin.getMessagesManager().getPermissionSuccessNotify()
                    .replaceAll("%player%", nameUser)
                    .replaceAll("%contexts%", contextsMessage.toString())
                    .replaceAll("%permission%", modifyData.getPermission())
                    .replaceAll("%time%", timeString)
                    .replaceAll("%operator%", nameOperator);
        }
        String message;
        if(timeString.equalsIgnoreCase("permanent")) {
            //Perm
            if(nodeType == NodeType.RANK) {
                message = plugin.getMessagesManager().getGrantSuccessPerm()
                        .replaceAll("%player%", nameUser)
                        .replaceAll("%contexts%", contextsMessage.toString())
                        .replaceAll("%rank%", modifyData.getRank().getName());
            }else{
                message = plugin.getMessagesManager().getPermissionSuccessPerm()
                        .replaceAll("%player%", nameUser)
                        .replaceAll("%contexts%", contextsMessage.toString())
                        .replaceAll("%permission%", modifyData.getPermission());
            }
        }else{
            //Temp
            if(nodeType == NodeType.RANK) {
                message = plugin.getMessagesManager().getGrantSuccessTemp()
                        .replaceAll("%player%", nameUser).replaceAll("%contexts%", contextsMessage.toString())
                        .replaceAll("%time%", timeString)
                        .replaceAll("%rank%", modifyData.getRank().getName());
            }else{
                message = plugin.getMessagesManager().getPermissionSuccessTemp()
                        .replaceAll("%player%", nameUser).replaceAll("%contexts%", contextsMessage.toString())
                        .replaceAll("%time%", timeString)
                        .replaceAll("%permission%", modifyData.getPermission());
            }
        }
        player.sendMessage(GrantRank.prefix+ MessageUtils.getColoredMessage(message));
        OtherUtils.playSound(player,10, 2, SoundType.FINISH_GRANT, plugin.getConfigManager());

        LogSender.sendLogMessage(messageNotify);
        for(Player p : Bukkit.getOnlinePlayers()){
            if(PermissionUtils.canReceiveNotifies(p)){
                p.sendMessage(GrantRank.prefix+MessageUtils.getColoredMessage(messageNotify));
                OtherUtils.playSound(p,10, 2, SoundType.FINISH_GRANT, plugin.getConfigManager());
            }
        }
    }
}