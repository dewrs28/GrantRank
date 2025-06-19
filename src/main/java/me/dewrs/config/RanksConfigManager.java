package me.dewrs.config;

import me.dewrs.GrantRank;
import me.dewrs.enums.CustomActionType;
import me.dewrs.model.CustomInventory;
import me.dewrs.model.CustomItem;
import me.dewrs.utils.ItemUtils;
import me.dewrs.utils.OtherUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class RanksConfigManager {
    private GrantRank plugin;
    private CustomConfig customConfig;
    private Map<Integer, Map<String, CustomItem>> rankCustomItems;
    private CustomInventory parentInventory;

    public RanksConfigManager(GrantRank plugin){
        this.plugin = plugin;
        customConfig = new CustomConfig("ranks.yml", null, plugin);
        customConfig.registerConfig();
        parentInventory = plugin.getInventoryManager().getCustomInventory("grants.yml");
        loadRanks();
    }

    public void reload(){
        customConfig.reloadConfig();
        loadRanks();
    }

    public void loadRanks(){
        FileConfiguration config = customConfig.getConfig();
        Map<Integer, Map<String, CustomItem>> allPages = new HashMap<>();
        if(config.getConfigurationSection("ranks") != null) {
            int page = 1;
            for (String key : config.getConfigurationSection("ranks").getKeys(false)) {
                if (!OtherUtils.isValidPageFormat(key)) continue;
                Map<String, CustomItem> customItems = new HashMap<>();
                allPages.put(page, customItems);
                for (String i : config.getConfigurationSection("ranks." + key).getKeys(false)) {
                    CustomItem customItem = ItemUtils.getCustomItemFromConfigPath("ranks."+key+"."+i, config);
                    if (customItem == null || !OtherUtils.isValidSlot(customItem.getSlot(), parentInventory.getRows())) {
                        continue;
                    }
                    customItem.setCustomActionType(CustomActionType.GRANT);
                    customItems.put(i, customItem);
                }
                page++;
            }
        }
        rankCustomItems = allPages;
    }

    public Map<Integer, Map<String, CustomItem>> getRankCustomItems() {
        return rankCustomItems;
    }
}
