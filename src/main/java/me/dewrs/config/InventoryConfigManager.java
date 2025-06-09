package me.dewrs.config;

import me.dewrs.GrantRank;
import me.dewrs.enums.CustomActionType;
import me.dewrs.logger.LogMessage;
import me.dewrs.logger.LogSender;
import me.dewrs.model.CustomInventory;
import me.dewrs.model.CustomItem;
import me.dewrs.utils.MessageUtils;
import me.dewrs.utils.OtherUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;

public class InventoryConfigManager {
    private GrantRank plugin;
    private ArrayList<CustomConfig> configFiles;
    private String folderName;
    private List<String> defaultInventories;

    public InventoryConfigManager(GrantRank plugin, String folderName) {
        this.plugin = plugin;
        this.folderName = folderName;
        this.configFiles = new ArrayList<>();
        this.defaultInventories = Arrays.asList("grants.yml", "grant-finish.yml", "select-time.yml", "contexts.yml", "nodes-logs.yml");
        setup();
        LogSender.sendLogMessage(LogMessage.INVENTORIES_LOADED.format(MessageUtils.getStringFromList(defaultInventories)));
    }

    private void setup(){
        createFolder();
        registerDefaultInventories();
        loadInventories();
    }

    private void registerDefaultInventories(){
        for(String f : defaultInventories){
            CustomConfig customConfig = new CustomConfig(f, "inventories", plugin);
            customConfig.registerConfig();
            configFiles.add(customConfig);
        }
    }

    public void reloadInventories() {
        this.configFiles = new ArrayList<>();
        setup();
    }

    public CustomConfig getConfigFile(String pathName) {
        for (CustomConfig configFile : configFiles) {
            if (configFile.getPath().equals(pathName)) {
                return configFile;
            }
        }
        return null;
    }

    private boolean hasValidRows(String path, int rows){
        if(path.equals("grants.yml") || path.equals("nodes-logs.yml")) {
            return rows > 2;
        }
        return true;
    }

    private void loadInventories(){
        ArrayList<CustomInventory> inventories = new ArrayList<>();
        for(CustomConfig customConfig : configFiles) {
            FileConfiguration config = customConfig.getConfig();
            if (config.contains("title") && config.contains("rows")) {
                String path = customConfig.getPath();
                String title = config.getString("title");
                int rows = config.getInt("rows");
                //Verificar si son validos los rows
                if(!hasValidRows(path, rows)){
                    LogSender.sendLogMessage(LogMessage.INVENTORY_INVALID_ROWS.format(path));
                    continue;
                }
                ArrayList<CustomItem> customItems = new ArrayList<>();
                //Cargar los items implementados por el usuario
                if (config.getConfigurationSection("items") != null) {
                    for (String key : config.getConfigurationSection("items").getKeys(false)) {
                        if (config.contains("items." + key + ".slot") && config.contains("items." + key + ".material")) {
                            int slot = config.getInt("items." + key + ".slot");
                            if(!OtherUtils.isValidSlot(slot, rows, customItems)) continue;

                            Material material;
                            try {
                                material = Material.valueOf(config.getString("items." + key + ".material"));
                            }catch (IllegalArgumentException ex){
                                continue;
                            }

                            String name = config.contains("items." + key + ".name") ? config.getString("items." + key + ".name") : "";
                            List<String> lore = config.contains("items." + key + ".lore") ? config.getStringList("items." + key + ".lore") : new ArrayList<>();

                            CustomActionType customActionType = CustomActionType.DECORATION;
                            if (config.contains("items." + key + ".action")) {
                                try {
                                    customActionType = CustomActionType.valueOf(Objects.requireNonNull(config.getString("items." + key + ".action")).toUpperCase());
                                }catch (IllegalArgumentException ignored){
                                }
                            }
                            if(!OtherUtils.isValidCustomAction(customActionType, path)) customActionType = CustomActionType.DECORATION;

                            CustomItem customItem = new CustomItem(name, 1, material, (ArrayList<String>) lore, slot, customActionType);

                            if(config.contains("items."+key+".inventory") && customActionType.equals(CustomActionType.OPEN_INVENTORY)){
                                String openInv = config.getString("items." + key + ".inventory");
                                if(OtherUtils.isValidOpenInventory(Objects.requireNonNull(openInv), defaultInventories)) {
                                    customItem.setInventoryToOpen(openInv);
                                }else{
                                    customItem.setCustomActionType(CustomActionType.DECORATION);
                                }
                            }
                            boolean containsValue = (customActionType.equals(CustomActionType.SET_TIME));
                            if(containsValue){
                                if(config.contains("items."+key+".value")){
                                    customItem.setCacheFormat(config.getString("items." + key + ".value"));
                                }
                            }

                            customItems.add(customItem);
                        }
                    }
                }
                CustomInventory inventory = new CustomInventory(path, title, rows, customItems);
                inventories.add(inventory);
            }
        }
        plugin.getInventoryManager().setInventories(inventories);
    }

    private void createFolder() {
        File folder;
        try {
            folder = new File(plugin.getDataFolder() + File.separator + folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }
        } catch (SecurityException e) {
            folder = null;
        }
    }

    public boolean inventoryAlreadyRegistered(String pathName) {
        for (CustomConfig configFile : configFiles) {
            if (configFile.getPath().equals(pathName)) {
                return true;
            }
        }
        return false;
    }
}