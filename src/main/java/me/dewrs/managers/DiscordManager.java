package me.dewrs.managers;

import me.dewrs.GrantRank;
import me.dewrs.config.DiscordConfigManager;
import me.dewrs.enums.NodeType;
import me.dewrs.libs.DiscordWebhook;
import me.dewrs.logger.LogMessage;
import me.dewrs.logger.LogSender;
import me.dewrs.model.NodeLog;
import me.dewrs.utils.MessageUtils;
import me.dewrs.utils.OtherUtils;
import me.dewrs.utils.TimeUtils;
import net.luckperms.api.context.Context;
import org.bukkit.Bukkit;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Map;

public class DiscordManager{
    private GrantRank plugin;
    public DiscordManager(GrantRank plugin){
        this.plugin = plugin;
    }

    public void sendEmbedLog(NodeLog nodeLog, int actionType) {
        DiscordConfigManager discordConfigManager = plugin.getDiscordConfigManager();
        DiscordWebhook webhook = new DiscordWebhook(discordConfigManager.getUrlWebhook());
        DiscordWebhook.EmbedObject embedObject = getEmbedObjectByLog(nodeLog, actionType);
        webhook.addEmbed(embedObject);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                webhook.execute();
            } catch (MalformedURLException e) {
                LogSender.sendLogMessage(MessageUtils.getColoredMessage(LogMessage.INVALID_WEBHOOK.format()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private DiscordWebhook.EmbedObject getEmbedObjectByLog(NodeLog nodeLog, int actionType){
        DiscordConfigManager discordConfigManager = plugin.getDiscordConfigManager();
        NodeType nodeType = OtherUtils.getNodeType(nodeLog.getNode());
        ArrayList<String> contexts = new ArrayList<>();
        for(Context c : nodeLog.getContextSet().toSet()){
            contexts.add(c.getKey()+"="+c.getValue());
        }
        String contextsString = contexts.isEmpty() ? "None" : OtherUtils.getContextsStringFromList(contexts);
        DiscordWebhook.EmbedObject embedObject;
        Map<String, Boolean> fieldMap;
        String fieldNodeName;
        if(nodeType == NodeType.RANK) {
            if (actionType == 1) {
                embedObject = new DiscordWebhook.EmbedObject()
                        .setFooter(discordConfigManager.getFooterEmbed(), null)
                        .setTitle(discordConfigManager.getRankGiveTitle())
                        .setColor(Color.decode(discordConfigManager.getRankGiveColor()))
                        .setThumbnail(discordConfigManager.getRankGiveThumbnail());
                fieldMap = discordConfigManager.getFieldsRankGive();
            }else{
                embedObject = new DiscordWebhook.EmbedObject()
                        .setFooter(discordConfigManager.getFooterEmbed(), null)
                        .setTitle(discordConfigManager.getRankRevokeTitle())
                        .setColor(Color.decode(discordConfigManager.getRankRevokeColor()))
                        .setThumbnail(discordConfigManager.getRankRevokeThumbnail());
                fieldMap = discordConfigManager.getFieldsRankRevoke();
            }
            fieldNodeName = discordConfigManager.getRankName();
        }else{
            if (actionType == 1) {
                embedObject = new DiscordWebhook.EmbedObject()
                        .setFooter(discordConfigManager.getFooterEmbed(), null)
                        .setTitle(discordConfigManager.getPermGiveTitle())
                        .setColor(Color.decode(discordConfigManager.getPermGiveColor()))
                        .setThumbnail(discordConfigManager.getPermGiveThumbnail());
                fieldMap = discordConfigManager.getFieldsPermGive();
            }else{
                embedObject = new DiscordWebhook.EmbedObject()
                        .setFooter(discordConfigManager.getFooterEmbed(), null)
                        .setTitle(discordConfigManager.getPermRevokeTitle())
                        .setColor(Color.decode(discordConfigManager.getPermRevokeColor()))
                        .setThumbnail(discordConfigManager.getPermRevokeThumbnail());
                fieldMap = discordConfigManager.getFieldsPermRevoke();
            }
            fieldNodeName = discordConfigManager.getPermissionName();
        }
        if(fieldMap.get("id")) embedObject.addField(discordConfigManager.getIdName(), nodeLog.getId()+"", true);
        if(fieldMap.get("user")) embedObject.addField(discordConfigManager.getUserName(), nodeLog.getName_user(), true);
        if(fieldMap.get("staff")) embedObject.addField(discordConfigManager.getStaffName(), nodeLog.getName_operator(), true);
        if(fieldMap.get("node")) embedObject.addField(fieldNodeName, OtherUtils.getGroupNameByNode(nodeLog.getNode()), false);
        if(fieldMap.get("time")) embedObject.addField(discordConfigManager.getTimeName(), TimeUtils.getTimeFromMilis(nodeLog.getExpiry()), false);
        if(fieldMap.get("contexts")) embedObject.addField(discordConfigManager.getContextsName(), contextsString, false);
        if(fieldMap.get("reason")) embedObject.addField(discordConfigManager.getReasonName(), nodeLog.getReason(), false);
        if(fieldMap.get("date")) embedObject.addField(discordConfigManager.getDateName(), TimeUtils.getDateFromMillis(nodeLog.getCreation_time()), false);
        return embedObject;
    }
}