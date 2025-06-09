package me.dewrs.managers;

import me.dewrs.GrantRank;
import me.dewrs.enums.HookType;
import me.dewrs.utils.MessageUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.*;

public class HookManager {
    private GrantRank plugin;
    private ArrayList<HookType> hooksEnabled;

    public HookManager(GrantRank plugin) {
        this.plugin = plugin;
        hooksEnabled = new ArrayList<>();
        loadHooks();
    }

    public void loadHooks(){
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if(provider == null){
            Bukkit.getConsoleSender().sendMessage(GrantRank.prefix + MessageUtils.getColoredMessage("&aLuckPerms has produced a error with GrantRank!"));
            return;
        }
        plugin.setLuckPermsApi(provider.getProvider());
        hooksEnabled.add(HookType.LUCKPERMS);
        Bukkit.getConsoleSender().sendMessage(GrantRank.prefix + MessageUtils.getColoredMessage("&aLuckPerms has been connected successful!"));
    }

    public Map<Group, Integer> sortWeightGroupsLuckPerms(){
        Map<Group, Integer> mapGroups = new HashMap<>();
        Set<Group> groups = plugin.getLuckPermsApi().getGroupManager().getLoadedGroups();
        for (Group group : groups) {
            int weight;
            OptionalInt weightObject = group.getWeight();
            if(weightObject.isPresent()){
                weight = weightObject.getAsInt();
            }else{
                weight = 0;
            }
            mapGroups.put(group, weight);
        }
        List<Map.Entry<Group, Integer>> listMapGroups = new ArrayList<>(mapGroups.entrySet());
        listMapGroups.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        Map<Group, Integer> sortWeightGroups = new LinkedHashMap<>();
        for (Map.Entry<Group, Integer> entry : listMapGroups) {
            sortWeightGroups.put(entry.getKey(), entry.getValue());
        }
        return sortWeightGroups;
    }

    public String getGroupPrefixLuckPerms(Group group){
        CachedMetaData metaData = group.getCachedData().getMetaData();
        if(metaData.getPrefix() == null){
            return "&cNo Prefix";
        }
        return metaData.getPrefix();
    }

    public boolean isHookEnabled(HookType hookType){
        return hooksEnabled.contains(hookType);
    }

    private boolean isPluginInstalled(String namePlugin){
        return Bukkit.getPluginManager().getPlugin(namePlugin) != null;
    }
}
