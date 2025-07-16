package me.dewrs.config;

import me.dewrs.GrantRank;
import me.dewrs.enums.CustomActionType;
import me.dewrs.enums.GrantMenuType;
import me.dewrs.model.CustomItem;
import me.dewrs.storage.StorageType;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigManager {
    private CustomConfig customConfig;
    private GrantMenuType menuType;
    private CustomItem itemLpGrantsMenu;
    private StorageType storageType;
    private String storageHost;
    private int storagePort;
    private String storageDatabase;
    private String storageUser;
    private String storagePassword;
    private CustomItem itemNodeLog;
    private List<String> excludeLpGroups;
    private String soundOpenInv;
    private String soundFinishGrant;
    private String soundNoPerm;
    private boolean isEnabledSoundOpenInv;
    private boolean isEnabledSoundNoPerm;
    private boolean isEnabledSoundFinishGrant;

    public ConfigManager(GrantRank plugin){
        customConfig = new CustomConfig("config.yml", null, plugin);
        customConfig.registerConfig();
        customConfig.updateConfig();
        reload();
    }

    public void reload(){
        customConfig.reloadConfig();
        loadConfig();
    }

    private void loadConfig(){
        FileConfiguration config = customConfig.getConfig();
        try {
            menuType = GrantMenuType.valueOf(Objects.requireNonNull(config.getString("settings.grants_menu_type")).toUpperCase());
        }catch (IllegalArgumentException ex){
            menuType = GrantMenuType.LUCKPERMS;
        }
        try {
            storageType = StorageType.valueOf(Objects.requireNonNull(config.getString("settings.storage.type")).toUpperCase());
        }catch (IllegalArgumentException ex){
            storageType = StorageType.SQLITE;
        }
        itemLpGrantsMenu = loadItemLP(config);
        storageHost = config.getString("settings.storage.host");
        storagePort = config.getInt("settings.storage.port");
        storageDatabase = config.getString("settings.storage.database");
        storageUser = config.getString("settings.storage.user");
        storagePassword = config.getString("settings.storage.password");
        itemNodeLog = loadItemNodeLog(config);
        excludeLpGroups = config.getStringList("settings.exclude_luckperms_groups");
        soundFinishGrant = config.getString("settings.sounds.finish_grant.sound");
        soundNoPerm = config.getString("settings.sounds.no_permission.sound");
        soundOpenInv = config.getString("settings.sounds.open_inventory.sound");
        isEnabledSoundOpenInv = config.getBoolean("settings.sounds.open_inventory.enabled");
        isEnabledSoundFinishGrant = config.getBoolean("settings.sounds.finish_grant.enabled");
        isEnabledSoundNoPerm = config.getBoolean("settings.sounds.no_permission.enabled");
    }

    private CustomItem loadItemLP(FileConfiguration config) {
        if (config.contains("settings.item_lp_grants_menu.material")) {
            Material material = Material.valueOf(config.getString("settings.item_lp_grants_menu.material"));
            String name = config.contains("settings.item_lp_grants_menu.name") ? config.getString("settings.item_lp_grants_menu.name") : "";
            int slot = -1;
            List<String> lore = config.contains("settings.item_lp_grants_menu.lore") ? config.getStringList("settings.item_lp_grants_menu.lore") : new ArrayList<>();
            return new CustomItem(name, 1, material, (ArrayList<String>) lore, slot, CustomActionType.GRANT);
        }
        return new CustomItem("", 1, Material.STONE, new ArrayList<>(),-1, CustomActionType.GRANT);
    }

    private CustomItem loadItemNodeLog(FileConfiguration config) {
        if (config.contains("settings.item_node_log.material")) {
            Material material = Material.valueOf(config.getString("settings.item_node_log.material"));
            String name = config.contains("settings.item_node_log.name") ? config.getString("settings.item_node_log.name") : "";
            int slot = -1;
            List<String> lore = config.contains("settings.item_node_log.lore") ? config.getStringList("settings.item_node_log.lore") : new ArrayList<>();
            return new CustomItem(name, 1, material, (ArrayList<String>) lore, slot, CustomActionType.NODE_LOG);
        }
        return new CustomItem("", 1, Material.STONE, new ArrayList<>(),-1, CustomActionType.NODE_LOG);
    }

    public GrantMenuType getMenuType() {
        return menuType;
    }

    public CustomItem getItemLpGrantsMenu() {
        return itemLpGrantsMenu;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public String getStorageHost() {
        return storageHost;
    }

    public int getStoragePort() {
        return storagePort;
    }

    public String getStorageDatabase() {
        return storageDatabase;
    }

    public String getStorageUser() {
        return storageUser;
    }

    public String getStoragePassword() {
        return storagePassword;
    }

    public CustomItem getItemNodeLog() {
        return itemNodeLog;
    }

    public List<String> getExcludeLpGroups() {
        return excludeLpGroups;
    }

    public String getSoundOpenInv() {
        return soundOpenInv;
    }

    public String getSoundFinishGrant() {
        return soundFinishGrant;
    }

    public String getSoundNoPerm() {
        return soundNoPerm;
    }

    public boolean isEnabledSoundOpenInv() {
        return isEnabledSoundOpenInv;
    }

    public boolean isEnabledSoundNoPerm() {
        return isEnabledSoundNoPerm;
    }

    public boolean isEnabledSoundFinishGrant() {
        return isEnabledSoundFinishGrant;
    }
}