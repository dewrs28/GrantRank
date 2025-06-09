package me.dewrs.config;

import me.dewrs.GrantRank;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class MessagesManager {
    private GrantRank plugin;
    private CustomConfig customConfig;
    private String noArguments;
    private String noPlayer;
    private String grantsGuiOpen;
    private List<String> helpAdmin;
    private String reload;
    private String nicknameTooLong;
    private String chatTime;
    private String chatContext;
    private String chatReason;
    private String chatPermission;
    private String noPermission;
    private String grantSuccessPerm;
    private String grantSuccessTemp;
    private String invalidTime;
    private String invalidContext;
    private String invalidPermission;
    private String permissionSuccessPerm;
    private String permissionSuccessTemp;
    private String grantSuccessNotify;
    private String permissionSuccessNotify;

    public MessagesManager(GrantRank plugin){
        this.plugin = plugin;
        customConfig = new CustomConfig("messages.yml", null, plugin);
        customConfig.registerConfig();
        loadConfig();
    }

    public void reload(){
        customConfig.reloadConfig();
        loadConfig();
    }

    private void loadConfig(){
        FileConfiguration config = customConfig.getConfig();
        noArguments = config.getString("messages.no_arguments");
        noPlayer = config.getString("messages.no_player");
        grantsGuiOpen = config.getString("messages.grants_gui_open");
        reload = config.getString("messages.reload");
        helpAdmin = config.getStringList("help_admin_command");
        nicknameTooLong = config.getString("messages.nickname_too_long");
        chatTime = config.getString("messages.chat_time_input");
        chatContext = config.getString("messages.chat_context_input");
        chatReason = config.getString("messages.chat_reason_input");
        noPermission = config.getString("messages.no_permission");
        chatPermission = config.getString("messages.chat_permission_input");
        grantSuccessPerm = config.getString("messages.grant_perm_success");
        grantSuccessTemp = config.getString("messages.grant_temp_success");
        permissionSuccessPerm = config.getString("messages.permission_perm_success");
        permissionSuccessTemp = config.getString("messages.permission_temp_success");
        invalidTime = config.getString("messages.invalid_time");
        invalidContext = config.getString("messages.invalid_context");
        invalidPermission = config.getString("messages.invalid_permission");
        grantSuccessNotify = config.getString("messages.grant_success_notify");
        permissionSuccessNotify = config.getString("messages.permission_success_notify");
    }

    public String getNoArguments() {
        return noArguments;
    }

    public String getNoPlayer() {
        return noPlayer;
    }

    public String getGrantsGuiOpen() {
        return grantsGuiOpen;
    }

    public List<String> getHelpAdmin() {
        return helpAdmin;
    }

    public String getReload() {
        return reload;
    }

    public String getNicknameTooLong() {
        return nicknameTooLong;
    }

    public String getChatTime() {
        return chatTime;
    }

    public String getChatContext() {
        return chatContext;
    }

    public String getChatReason() {
        return chatReason;
    }

    public String getNoPermission() {
        return noPermission;
    }

    public String getGrantSuccessPerm() {
        return grantSuccessPerm;
    }

    public String getGrantSuccessTemp() {
        return grantSuccessTemp;
    }

    public String getInvalidTime() {
        return invalidTime;
    }

    public String getInvalidContext() {
        return invalidContext;
    }

    public String getChatPermission() {
        return chatPermission;
    }

    public String getInvalidPermission() {
        return invalidPermission;
    }

    public String getPermissionSuccessPerm() {
        return permissionSuccessPerm;
    }

    public String getPermissionSuccessTemp() {
        return permissionSuccessTemp;
    }

    public String getGrantSuccessNotify() {
        return grantSuccessNotify;
    }

    public String getPermissionSuccessNotify() {
        return permissionSuccessNotify;
    }
}
