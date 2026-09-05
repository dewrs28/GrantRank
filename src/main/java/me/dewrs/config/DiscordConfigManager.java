package me.dewrs.config;

import me.dewrs.GrantRank;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class DiscordConfigManager {
    private CustomConfig customConfig;
    private boolean isWebhookEnabled;
    private String urlWebhook;
    private String footerEmbed;
    private String idName;
    private String userName;
    private String staffName;
    private String rankName;
    private String permissionName;
    private String timeName;
    private String contextsName;
    private String reasonName;
    private String dateName;
    private boolean isRankGiveEnabled;
    private String rankGiveTitle;
    private String rankGiveColor;
    private String rankGiveThumbnail;
    private boolean isRankRevokeEnabled;
    private String rankRevokeTitle;
    private String rankRevokeColor;
    private String rankRevokeThumbnail;
    private boolean isPermGiveEnabled;
    private String permGiveTitle;
    private String permGiveColor;
    private String permGiveThumbnail;
    private boolean isPermRevokeEnabled;
    private String permRevokeTitle;
    private String permRevokeColor;
    private String permRevokeThumbnail;

    private Map<String, Boolean> fieldsRankGive;
    private Map<String, Boolean> fieldsRankRevoke;
    private Map<String, Boolean> fieldsPermGive;
    private Map<String, Boolean> fieldsPermRevoke;

    public DiscordConfigManager(GrantRank plugin){
        customConfig = new CustomConfig("discord.yml", null, plugin);
        customConfig.registerConfig();
        customConfig.updateConfig();
        reload();
    }

    public void reload(){
        customConfig.reloadConfig();
        loadConfig();
    }

    private Map<String, Boolean> getFieldsMap(String embedType){
        FileConfiguration config = customConfig.getConfig();
        String path = "webhook.embeds."+embedType+".fields";
        Map<String, Boolean> fieldMap = new HashMap<>();
        boolean id = config.contains(path+".id") && config.getBoolean(path+".id");
        boolean user = config.contains(path+".user") && config.getBoolean(path+".user");
        boolean staff = config.contains(path+".staff") && config.getBoolean(path+".staff");
        boolean time = config.contains(path+".time") && config.getBoolean(path+".time");
        boolean contexts = config.contains(path+".contexts") && config.getBoolean(path+".contexts");
        boolean reason = config.contains(path+".reason") && config.getBoolean(path+".reason");
        boolean date = config.contains(path+".date") && config.getBoolean(path+".date");
        fieldMap.put("id", id);
        fieldMap.put("user", user);
        fieldMap.put("staff", staff);
        fieldMap.put("time", time);
        fieldMap.put("contexts", contexts);
        fieldMap.put("reason", reason);
        fieldMap.put("date", date);
        boolean node;
        if(embedType.startsWith("rank")){
            node = config.contains(path+".rank") && config.getBoolean(path+".rank");
            fieldMap.put("node", node);
        }else{
            node = config.contains(path+".permission") && config.getBoolean(path+".permission");
            fieldMap.put("node", node);
        }
        return fieldMap;
    }

    private void loadConfig(){
        FileConfiguration config = customConfig.getConfig();
        isWebhookEnabled = config.getBoolean("webhook.enabled");
        urlWebhook = config.getString("webhook.url");
        footerEmbed = config.getString("webhook.footer");
        //Field Names
        idName = config.getString("webhook.field_names.id");
        userName = config.getString("webhook.field_names.user");
        staffName = config.getString("webhook.field_names.staff");
        rankName = config.getString("webhook.field_names.rank");
        permissionName = config.getString("webhook.field_names.permission");
        timeName = config.getString("webhook.field_names.time");
        contextsName = config.getString("webhook.field_names.contexts");
        reasonName = config.getString("webhook.field_names.reason");
        dateName = config.getString("webhook.field_names.date");
        //Embeds
        isRankGiveEnabled = config.getBoolean("webhook.embeds.rank_give.enabled");
        rankGiveTitle = config.getString("webhook.embeds.rank_give.title");
        rankGiveColor = config.getString("webhook.embeds.rank_give.color");
        rankGiveThumbnail = config.getString("webhook.embeds.rank_give.thumbnail");
        fieldsRankGive = getFieldsMap("rank_give");

        isRankRevokeEnabled = config.getBoolean("webhook.embeds.rank_revoke.enabled");
        rankRevokeTitle = config.getString("webhook.embeds.rank_revoke.title");
        rankRevokeColor = config.getString("webhook.embeds.rank_revoke.color");
        rankRevokeThumbnail = config.getString("webhook.embeds.rank_revoke.thumbnail");
        fieldsRankRevoke = getFieldsMap("rank_revoke");

        isPermGiveEnabled = config.getBoolean("webhook.embeds.permission_give.enabled");
        permGiveTitle = config.getString("webhook.embeds.permission_give.title");
        permGiveColor = config.getString("webhook.embeds.permission_give.color");
        permGiveThumbnail = config.getString("webhook.embeds.permission_give.thumbnail");
        fieldsPermGive = getFieldsMap("permission_give");

        isPermRevokeEnabled = config.getBoolean("webhook.embeds.permission_revoke.enabled");
        permRevokeTitle = config.getString("webhook.embeds.permission_revoke.title");
        permRevokeColor = config.getString("webhook.embeds.permission_revoke.color");
        permRevokeThumbnail = config.getString("webhook.embeds.permission_revoke.thumbnail");
        fieldsPermRevoke = getFieldsMap("permission_revoke");
    }

    public boolean isWebhookEnabled() {
        return isWebhookEnabled;
    }

    public String getUrlWebhook() {
        return urlWebhook;
    }

    public String getFooterEmbed() {
        return footerEmbed;
    }

    public String getIdName() {
        return idName;
    }

    public String getUserName() {
        return userName;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getRankName() {
        return rankName;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public String getTimeName() {
        return timeName;
    }

    public String getContextsName() {
        return contextsName;
    }

    public String getReasonName() {
        return reasonName;
    }

    public String getDateName() {
        return dateName;
    }

    public boolean isRankGiveEnabled() {
        return isRankGiveEnabled;
    }

    public String getRankGiveTitle() {
        return rankGiveTitle;
    }

    public String getRankGiveColor() {
        return rankGiveColor;
    }

    public String getRankGiveThumbnail() {
        return rankGiveThumbnail;
    }

    public boolean isRankRevokeEnabled() {
        return isRankRevokeEnabled;
    }

    public String getRankRevokeTitle() {
        return rankRevokeTitle;
    }

    public String getRankRevokeColor() {
        return rankRevokeColor;
    }

    public String getRankRevokeThumbnail() {
        return rankRevokeThumbnail;
    }

    public boolean isPermGiveEnabled() {
        return isPermGiveEnabled;
    }

    public String getPermGiveTitle() {
        return permGiveTitle;
    }

    public String getPermGiveColor() {
        return permGiveColor;
    }

    public String getPermGiveThumbnail() {
        return permGiveThumbnail;
    }

    public boolean isPermRevokeEnabled() {
        return isPermRevokeEnabled;
    }

    public String getPermRevokeTitle() {
        return permRevokeTitle;
    }

    public String getPermRevokeColor() {
        return permRevokeColor;
    }

    public String getPermRevokeThumbnail() {
        return permRevokeThumbnail;
    }

    public Map<String, Boolean> getFieldsRankGive() {
        return fieldsRankGive;
    }

    public Map<String, Boolean> getFieldsRankRevoke() {
        return fieldsRankRevoke;
    }

    public Map<String, Boolean> getFieldsPermGive() {
        return fieldsPermGive;
    }

    public Map<String, Boolean> getFieldsPermRevoke() {
        return fieldsPermRevoke;
    }
}