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
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.context.MutableContextSet;
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

    public void removeNodeToPlayer(InventoryPlayer inventoryPlayer, Player player, NodeLog nodeLog) {
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
                    manageChatRevokeMessages(inventoryPlayer, player, nodeLog, nodeType));
        });
    }

    private boolean nodeLogEqualsToNode(NodeLog nodeLog, Node n) {
        String node = nodeLog.getNode();
        boolean isTemp = nodeLog.getExpiry() != -1;
        ImmutableContextSet immutableContextSet = nodeLog.getContextSet().immutableCopy();
        return n.getKey().equals(node) && n.getValue() && n.getContexts().equals(immutableContextSet) && n.hasExpiry() == isTemp;
    }

    public void giveNodeToPlayer(InventoryPlayer inventoryPlayer, Player player) {
        ModifyData modifyData = inventoryPlayer.getModifyData();
        NodeType nodeType = OtherUtils.getNodeType(modifyData);

        String nameUser = inventoryPlayer.getTargetName();
        String nameOperator = inventoryPlayer.getName();
        String timeString = TimeUtils.getTimeFromMilis(modifyData.getTime());
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

        Node node;
        if (expiry == -1) {
            if (nodeType == NodeType.RANK) {
                node = InheritanceNode.builder(modifyData.getRank())
                        .context(contextSet)
                        .build();
            } else {
                node = PermissionNode.builder(modifyData.getPermission())
                        .context(contextSet)
                        .build();
            }
        } else {
            if (nodeType == NodeType.RANK) {
                node = InheritanceNode.builder(modifyData.getRank())
                        .expiry(Duration.ofMillis(expiry))
                        .context(contextSet)
                        .build();
            } else {
                node = PermissionNode.builder(modifyData.getPermission())
                        .expiry(Duration.ofMillis(expiry))
                        .context(contextSet)
                        .build();
            }
        }

        UUID uuid = inventoryPlayer.getTargetUuid();
        plugin.getLuckPermsApi().getUserManager().modifyUser(uuid, user -> {
            if(plugin.getConfigManager().getParentRankType() == ParentRankType.SET && nodeType == NodeType.RANK) user.data().clear();
            user.data().add(node);
            long creation_time = System.currentTimeMillis();
            NodeLog nodeLog = new NodeLog(uuid, nameUser, nameOperator, node.getKey(), expiry, reason, contextSet, creation_time, false);
            plugin.getStorageManager().createNodeLog(nodeLog, () -> {
                manageChatSuccessMessages(inventoryPlayer, player, modifyData, timeString, nodeType);
            });
        });
    }

    /*
    Util Methods To Manage Chat Messages And Replaces
    */
    private void manageChatSuccessMessages(InventoryPlayer inventoryPlayer, Player player, ModifyData modifyData, String timeString, NodeType nodeType) {
        String nameUser = inventoryPlayer.getTargetName();
        String nameOperator = inventoryPlayer.getName();

        if (player == null) {
            return;
        }
        StringBuilder contextsMessage = new StringBuilder();
        int i = 1;
        for (String s : modifyData.getContexts()) {
            if (modifyData.getContexts().size() != i) {
                contextsMessage.append(s).append(", ");
            } else {
                contextsMessage.append(s);
            }
            i++;
        }

        boolean isRank = nodeType == NodeType.RANK;
        String time = timeString.equalsIgnoreCase("permanent") ? "" : timeString;
        String value = isRank ? modifyData.getRank().getName() : modifyData.getPermission();
        String messageNotify = replaceGiveMessageNotify(nameUser, contextsMessage.toString(), value, timeString, nameOperator, isRank);
        String message = replaceGiveMessageOperator(nameUser, contextsMessage.toString(), value, time, isRank);

        player.sendMessage(GrantRank.PREFIX + MessageUtils.getColoredMessage(message));
        OtherUtils.playSound(player, 10, 2, SoundType.FINISH_GRANT, plugin.getConfigManager());

        LogSender.sendLogMessage(messageNotify);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (PermissionUtils.canReceiveNotifies(p)) {
                p.sendMessage(GrantRank.PREFIX + MessageUtils.getColoredMessage(messageNotify));
                OtherUtils.playSound(p, 10, 2, SoundType.FINISH_GRANT, plugin.getConfigManager());
            }
        }
    }

    private void manageChatRevokeMessages(InventoryPlayer inventoryPlayer, Player player, NodeLog nodeLog, NodeType nodeType) {
        String nameUser = nodeLog.getName_user();
        String nameOperator = inventoryPlayer.getName();

        if (player == null) {
            return;
        }

        boolean isRank = nodeType == NodeType.RANK;
        String nodeValue = isRank ? nodeLog.getNode().split("\\.")[1] : nodeLog.getNode();
        String nodeId = String.valueOf(nodeLog.getId());

        String message = replaceRevokeMessageOperator(nameUser, nodeValue, nodeId, isRank);
        String messageNotify = replaceRevokeMessageNotify(nameUser, nodeValue, nameOperator, nodeId, isRank);

        player.sendMessage(GrantRank.PREFIX + MessageUtils.getColoredMessage(message));
        OtherUtils.playSound(player, 10, 2, SoundType.FINISH_GRANT, plugin.getConfigManager());

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
        String mainMessage = isRank ? plugin.getMessagesManager().getGrantRevoke() : plugin.getMessagesManager().getPermissionRevokeNotify();
        return mainMessage
                .replace("%player%", nameUser)
                .replace(nodeVariable, nodeValue)
                .replace("%id%", nodeId);
    }
}