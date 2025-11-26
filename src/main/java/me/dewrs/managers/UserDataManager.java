package me.dewrs.managers;

import me.dewrs.GrantRank;
import me.dewrs.enums.NodeType;
import me.dewrs.enums.ParentRankType;
import me.dewrs.enums.SoundType;
import me.dewrs.logger.LogSender;
import me.dewrs.model.ModifyData;
import me.dewrs.model.NodeLog;
import me.dewrs.model.internal.InventoryPlayer;
import me.dewrs.utils.MessageUtils;
import me.dewrs.utils.OtherUtils;
import me.dewrs.utils.PermissionUtils;
import me.dewrs.utils.TimeUtils;
import net.luckperms.api.context.Context;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.context.MutableContextSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class UserDataManager {
    private GrantRank plugin;

    public UserDataManager(GrantRank plugin) {
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

    public void getInheritanceGroupsFromUUID(UUID uuid, Consumer<List<InheritanceNode>> callBack) {
        CompletableFuture<User> userFuture = plugin.getLuckPermsApi().getUserManager().loadUser(uuid);
        userFuture.thenAccept(user -> {
            Collection<Node> nodes = user.getNodes();
            List<InheritanceNode> groupNodes = new ArrayList<>();
            for (Node n : nodes) {
                if (n.getType() == net.luckperms.api.node.NodeType.INHERITANCE) {
                    groupNodes.add((InheritanceNode) n);
                }
            }
            callBack.accept(groupNodes);
        });
    }

    public void getInheritancePermissionFromUUID(UUID uuid, Consumer<List<PermissionNode>> callBack) {
        CompletableFuture<User> userFuture = plugin.getLuckPermsApi().getUserManager().loadUser(uuid);
        userFuture.thenAccept(user -> {
            Collection<Node> nodes = user.getNodes();
            List<PermissionNode> groupNodes = new ArrayList<>();
            for (Node n : nodes) {
                if (n.getType() == net.luckperms.api.node.NodeType.PERMISSION) {
                    groupNodes.add((PermissionNode) n);
                }
            }
            callBack.accept(groupNodes);
        });
    }

    public void setNodeData(NodeLog nodeLog, Consumer<Boolean> callback) {
        if (nodeLog.isRevoked()) {
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String node = nodeLog.getNode();
            NodeType nodeType = OtherUtils.getNodeType(node);
            if (nodeType == NodeType.RANK) {
                processRankNode(nodeLog, isSuccess -> {
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(isSuccess));
                });
            } else {
                processPermissionNode(nodeLog, isSuccess -> {
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(isSuccess));
                });
            }
        });
    }

    private void processRankNode(NodeLog nodeLog, Consumer<Boolean> callback) {
        getInheritanceGroupsFromUUID(nodeLog.getUuid_user(), groups -> {
            for (InheritanceNode n : groups) {
                if (nodeLogEqualsToNode(nodeLog, n)) {
                    nodeLog.setNodeData(n);
                    callback.accept(true);
                    return;
                }
            }
            nodeLog.setRevoked(true);
            plugin.getStorageManager().updateRevokeNodeLog(nodeLog.getId(), true, () -> {
                callback.accept(false);
            });
        });
    }

    private void processPermissionNode(NodeLog nodeLog, Consumer<Boolean> callback) {
        getInheritancePermissionFromUUID(nodeLog.getUuid_user(), permissions -> {
            for (PermissionNode n : permissions) {
                if (nodeLogEqualsToNode(nodeLog, n)) {
                    nodeLog.setNodeData(n);
                    callback.accept(true);
                    return;
                }
            }
            nodeLog.setRevoked(true);
            plugin.getStorageManager().updateRevokeNodeLog(nodeLog.getId(), true, () -> {
                callback.accept(false);
            });
        });
    }

    public void removeNodeToPlayer(Player player, NodeLog nodeLog) {
        String node = nodeLog.getNode();
        NodeType nodeType = OtherUtils.getNodeType(node);
        UUID uuid = nodeLog.getUuid_user();
        plugin.getLuckPermsApi().getUserManager().modifyUser(uuid, user -> {
            Node nodeData = nodeLog.getNodeData();
            if (nodeData == null) {
                return;
            }
            user.data().remove(nodeData);
            plugin.getStorageManager().updateRevokeNodeLog(nodeLog.getId(), true, () ->
                    manageChatRevokeMessages(player, nodeLog, nodeType));
        });
    }

    private boolean nodeLogEqualsToNode(NodeLog nodeLog, Node n) {
        String node = nodeLog.getNode();
        boolean isTemp = nodeLog.getExpiry() != -1;
        ImmutableContextSet immutableContextSet = nodeLog.getContextSet().immutableCopy();
        return n.getKey().equals(node) && n.getValue() && n.getContexts().equals(immutableContextSet) && n.hasExpiry() == isTemp;
    }

    public void setNodeToPlayer(UUID uuid, String permission, long expiry, MutableContextSet contexts, Consumer<Node> callback){
        Node node;
        if (expiry == -1) {
            node = PermissionNode.builder(permission)
                    .context(contexts)
                    .build();
        } else {
            node = PermissionNode.builder(permission)
                    .expiry(Duration.ofMillis(expiry))
                    .context(contexts)
                    .build();
        }
        plugin.getLuckPermsApi().getUserManager().modifyUser(uuid, user -> {
            user.data().add(node);
            callback.accept(node);
        });
    }

    public void setRankToPlayer(UUID uuid, Group group, long expiry, MutableContextSet contexts, Consumer<Node> callback){
        Node node;
        if (expiry == -1) {
            node = InheritanceNode.builder(group)
                    .context(contexts)
                    .build();
        } else {
            node = InheritanceNode.builder(group)
                    .expiry(Duration.ofMillis(expiry))
                    .context(contexts)
                    .build();
        }
        plugin.getLuckPermsApi().getUserManager().modifyUser(uuid, user -> {
            if(plugin.getConfigManager().getParentRankType() == ParentRankType.SET) user.data().clear(net.luckperms.api.node.NodeType.INHERITANCE::matches);
            user.data().add(node);
            callback.accept(node);
        });
    }

    public void giveNodeToPlayer(InventoryPlayer inventoryPlayer, Player player) {
        ModifyData modifyData = inventoryPlayer.getModifyData();
        NodeType nodeType = OtherUtils.getNodeType(modifyData);

        String nameUser = inventoryPlayer.getTargetName();
        String nameOperator = inventoryPlayer.getName();

        long expiry = modifyData.getTime();
        String reason = modifyData.getReason();
        ArrayList<String> contexts = modifyData.getContexts();

        if (contexts == null) {
            contexts = new ArrayList<>();
        }
        MutableContextSet contextSet = MutableContextSet.create();
        for (String s : contexts) {
            String[] split = s.split("=");
            contextSet.add(split[0], split[1]);
        }
        UUID uuid = inventoryPlayer.getTargetUuid();
        if (nodeType == NodeType.RANK) {
            setRankToPlayer(uuid, modifyData.getRank(), expiry, contextSet, node -> {
                long creation_time = System.currentTimeMillis();
                NodeLog nodeLog = new NodeLog(uuid, nameUser, nameOperator, node.getKey(), expiry, reason, contextSet, creation_time, false);
                plugin.getStorageManager().createNodeLog(nodeLog, () -> {
                    manageChatSuccessMessages(player, nodeLog, nodeType);
                });
            });
        }else{
            setNodeToPlayer(uuid, modifyData.getPermission(), expiry, contextSet, node -> {
                long creation_time = System.currentTimeMillis();
                NodeLog nodeLog = new NodeLog(uuid, nameUser, nameOperator, node.getKey(), expiry, reason, contextSet, creation_time, false);
                plugin.getStorageManager().createNodeLog(nodeLog, () -> {
                    manageChatSuccessMessages(player, nodeLog, nodeType);
                });
            });
        }
    }

    /*
    Util Methods To Manage Chat Messages And Replaces
    */
    private void manageChatSuccessMessages(Player player, NodeLog nodeLog, NodeType nodeType) {
        if (player == null) {
            return;
        }

        String nameUser = nodeLog.getName_user();

        ArrayList<String> contexts = new ArrayList<>();
        for(Context c : nodeLog.getContextSet().toSet()){
            contexts.add(c.getKey()+"="+c.getValue());
        }
        String contextsMessage = OtherUtils.getContextsStringFromList(contexts);

        boolean isRank = nodeType == NodeType.RANK;
        String timeString = TimeUtils.getTimeFromMilis(nodeLog.getExpiry());
        String time = timeString.equalsIgnoreCase("permanent") ? "" : timeString;
        String value = isRank ? nodeLog.getNode().split("\\.")[1] : nodeLog.getNode();

        String message = replaceGiveMessageOperator(nameUser, contextsMessage, value, time, isRank);
        player.sendMessage(GrantRank.PREFIX + MessageUtils.getColoredMessage(message));
        OtherUtils.playSound(player, 10, 2, SoundType.FINISH_GRANT, plugin.getConfigManager());

        notifyBroadcastSuccessMessage(nameUser, nodeLog.getName_operator(), nodeLog.getNode(), timeString, contexts, nodeType);
    }

    public void notifyBroadcastSuccessMessage(String nameUser, String nameOperator, String node, String timeString, ArrayList<String> contextsList, NodeType nodeType){
        boolean isRank = nodeType == NodeType.RANK;
        String value = isRank ? node.split("\\.")[1] : node;

        String contextsMessage = OtherUtils.getContextsStringFromList(contextsList);
        String messageNotify = replaceGiveMessageNotify(nameUser, contextsMessage, value, timeString, nameOperator, isRank);

        LogSender.sendLogMessage(messageNotify);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (PermissionUtils.canReceiveNotifies(p)) {
                p.sendMessage(GrantRank.PREFIX + MessageUtils.getColoredMessage(messageNotify));
                OtherUtils.playSound(p, 10, 2, SoundType.FINISH_GRANT, plugin.getConfigManager());
            }
        }
    }

    private void manageChatRevokeMessages(Player player, NodeLog nodeLog, NodeType nodeType) {
        if (player == null) {
            return;
        }

        String nameUser = nodeLog.getName_user();
        String nameOperator = nodeLog.getName_operator();

        boolean isRank = nodeType == NodeType.RANK;
        String value = isRank ? nodeLog.getNode().split("\\.")[1] : nodeLog.getNode();
        String nodeId = String.valueOf(nodeLog.getId());

        String message = replaceRevokeMessageOperator(nameUser, value, nodeId, isRank);
        player.sendMessage(GrantRank.PREFIX + MessageUtils.getColoredMessage(message));
        OtherUtils.playSound(player, 10, 2, SoundType.FINISH_GRANT, plugin.getConfigManager());

        notifyBroadcastRevokeMessage(nameUser, nameOperator, nodeLog.getNode(), nodeId, nodeType);
    }

    public void notifyBroadcastRevokeMessage(String nameUser, String nameOperator, String node, String nodeId, NodeType nodeType){
        boolean isRank = nodeType == NodeType.RANK;
        String value = isRank ? node.split("\\.")[1] : node;
        String messageNotify = replaceRevokeMessageNotify(nameUser, value, nameOperator, nodeId, isRank);
        LogSender.sendLogMessage(messageNotify);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (PermissionUtils.canReceiveNotifies(p)) {
                p.sendMessage(GrantRank.PREFIX + MessageUtils.getColoredMessage(messageNotify));
                OtherUtils.playSound(p, 10, 2, SoundType.FINISH_GRANT, plugin.getConfigManager());
            }
        }
    }

    private String replaceGiveMessageNotify(String nameUser, String contexts, String nodeValue, String time, String operator, boolean isRank){
        String nodeVariable = isRank ? "%rank%" : "%permission%";
        String mainMessage = isRank ? plugin.getMessagesManager().getGrantSuccessNotify() : plugin.getMessagesManager().getPermissionSuccessNotify();
        return mainMessage
                .replace("%player%", nameUser)
                .replace("%contexts%", contexts)
                .replace(nodeVariable, nodeValue)
                .replace("%time%", time)
                .replace("%operator%", operator);
    }

    private String replaceGiveMessageOperator(String nameUser, String contexts, String nodeValue, String time, boolean isRank){
        String mainMessage;
        boolean hasTime = !Objects.equals(time, "");
        if(isRank){
            mainMessage = hasTime ? plugin.getMessagesManager().getGrantSuccessTemp() : plugin.getMessagesManager().getGrantSuccessPerm();
        }else{
            mainMessage = hasTime ? plugin.getMessagesManager().getPermissionSuccessTemp() : plugin.getMessagesManager().getPermissionSuccessPerm();
        }
        String nodeVariable = isRank ? "%rank%" : "%permission%";
        String message = mainMessage
                .replace("%player%", nameUser)
                .replace("%contexts%", contexts)
                .replace(nodeVariable, nodeValue);
        String finalMessage = message;
        if(hasTime) finalMessage = message.replace("%time%", time);
        return finalMessage;
    }

    private String replaceRevokeMessageNotify(String nameUser, String nodeValue, String operator, String nodeId, boolean isRank){
        String nodeVariable = isRank ? "%rank%" : "%permission%";
        String mainMessage = isRank ? plugin.getMessagesManager().getGrantRevokeNotify() : plugin.getMessagesManager().getPermissionRevokeNotify();
        return mainMessage
                .replace("%player%", nameUser)
                .replace(nodeVariable, nodeValue)
                .replace("%operator%", operator)
                .replace("%id%", nodeId);
    }

    private String replaceRevokeMessageOperator(String nameUser, String nodeValue, String nodeId, boolean isRank){
        String nodeVariable = isRank ? "%rank%" : "%permission%";
        String mainMessage = isRank ? plugin.getMessagesManager().getGrantRevoke() : plugin.getMessagesManager().getPermissionRevoke();
        return mainMessage
                .replace("%player%", nameUser)
                .replace(nodeVariable, nodeValue)
                .replace("%id%", nodeId);
    }
}