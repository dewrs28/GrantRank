package me.dewrs;

import me.dewrs.bstats.Metrics;
import me.dewrs.commands.GrantAdminCommand;
import me.dewrs.commands.GrantCommand;
import me.dewrs.commands.ViewGrantsCommand;
import me.dewrs.config.*;
import me.dewrs.listeners.ChatListener;
import me.dewrs.listeners.InventoryListener;
import me.dewrs.listeners.PlayerListener;
import me.dewrs.logger.LogSender;
import me.dewrs.managers.*;
import me.dewrs.storage.ConnectionFactory;
import me.dewrs.storage.StorageManager;
import me.dewrs.utils.MessageUtils;
import me.dewrs.utils.ServerVersion;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GrantRank extends JavaPlugin {
    public static final String PREFIX = MessageUtils.getColoredMessage("&8[&dGrants&8]&r ");
    PluginDescriptionFile pluginDescriptionFile = getDescription();
    public final String version = pluginDescriptionFile.getVersion();
    private static ServerVersion serverVersion;
    private static String bukkitVersion;
    private static boolean legacyServer;
    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private InventoryManager inventoryManager;
    private InventoryConfigManager inventoryConfigManager;
    private LuckPerms luckPermsApi;
    private UserDataManager userDataManager;
    private HookManager hookManager;
    private ActionInventoryManager actionInventoryManager;
    private StorageManager storageManager;
    private RanksConfigManager ranksConfigManager;
    private ConnectionFactory connectionFactory;
    private UpdateCheckerManager updateCheckerManager;
    private DiscordConfigManager discordConfigManager;

    @Override
    public void onEnable() {
        bukkitVersion = ServerVersion.getBukkitVersion();
        serverVersion = ServerVersion.getServerVersion();
        legacyServer = ServerVersion.isLegacyServer();
        configManager = new ConfigManager(this);
        messagesManager = new MessagesManager(this);
        inventoryManager = new InventoryManager(this);
        inventoryConfigManager = new InventoryConfigManager(this, "inventories");
        ranksConfigManager = new RanksConfigManager(this);
        userDataManager = new UserDataManager(this);
        hookManager = new HookManager(this);
        actionInventoryManager = new ActionInventoryManager(this);
        storageManager = new StorageManager(this);
        discordConfigManager = new DiscordConfigManager(this);
        regCommands();
        regEvents();
        updateCheckerManager = new UpdateCheckerManager(version);
        new Metrics(this, 26124);
        LogSender.sendLogMessage("&ahas been enabled");
    }

    @Override
    public void onDisable(){
        Bukkit.getScheduler().cancelTasks(this);
        if(connectionFactory != null) connectionFactory.closeConnection();
        LogSender.sendLogMessage("&chas been disabled");
    }

    private void regCommands(){
        this.getCommand("grant").setExecutor(new GrantCommand(this));
        this.getCommand("grants").setExecutor(new ViewGrantsCommand(this));
        this.getCommand("grantadmin").setExecutor(new GrantAdminCommand(this));
    }

    private void regEvents(){
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new InventoryListener(this), this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);
    }

    public void reloadPlugin(){
        configManager.reload();
        messagesManager.reload();
        ranksConfigManager.reload();
        inventoryConfigManager.reloadInventories();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public LuckPerms getLuckPermsApi() {
        return luckPermsApi;
    }

    public UserDataManager getUserDataManager() {
        return userDataManager;
    }

    public void setLuckPermsApi(LuckPerms luckPermsApi) {
        this.luckPermsApi = luckPermsApi;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public ActionInventoryManager getActionInventoryManager() {
        return actionInventoryManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public RanksConfigManager getRanksConfigManager() {
        return ranksConfigManager;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public UpdateCheckerManager getUpdateCheckerManager() {
        return updateCheckerManager;
    }

    public static ServerVersion getServerVersion() {
        return serverVersion;
    }

    public static boolean isLegacyServer() {
        return legacyServer;
    }

    public static String getBukkitVersion() {
        return bukkitVersion;
    }
}