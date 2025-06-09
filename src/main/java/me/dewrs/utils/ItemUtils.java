package me.dewrs.utils;

import me.dewrs.enums.CustomActionType;
import me.dewrs.model.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemUtils {

    public static ItemStack getItemStackFromCustomItem(CustomItem customItem){
        ItemStack itemStack = new ItemStack(customItem.getMaterial());
        itemStack.setAmount(customItem.getAmount());
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta != null){
            itemMeta.setDisplayName(MessageUtils.getColoredMessage(customItem.getName()));
            List<String> lore = new ArrayList<>();
            for(String s : customItem.getLore()){
                lore.add(MessageUtils.getColoredMessage(s));
            }
            itemMeta.setLore(lore);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static CustomItem cloneCustomItem(CustomItem customItem){
        return new CustomItem(customItem.getName(), customItem.getAmount(), customItem.getMaterial(), customItem.getLore(), customItem.getSlot(), customItem.getCustomActionType());
    }

    public static ItemStack getItemStackReplaceVariables(ItemStack itemStack, Map<String, String> variables) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }

        String name = itemMeta.getDisplayName();
        List<String> lore = itemMeta.getLore();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            name = name.replace(entry.getKey(), entry.getValue());
        }
        itemMeta.setDisplayName(MessageUtils.getColoredMessage(name));

        if (lore != null) {
            List<String> loreReplace = lore.stream().map(line -> {
                        for (Map.Entry<String, String> entry : variables.entrySet()) {
                            line = line.replace(entry.getKey(), entry.getValue());
                        }
                        return MessageUtils.getColoredMessage(line);
                    }).collect(Collectors.toList());
            itemMeta.setLore(loreReplace);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack getItemStackReplaceMultiLine(ItemStack itemStack, String variable, ArrayList<String> values){
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }
        List<String> lore = itemMeta.getLore();
        List<String> replace = new ArrayList<>();
        if (lore != null) {
            for(String s : lore){
                if(s.contains(variable)){
                    if(values == null || values.isEmpty()){
                        replace.add(MessageUtils.getColoredMessage("&7Empty"));
                        continue;
                    }
                    for(String v : values) {
                        replace.add(MessageUtils.getColoredMessage("&e"+v));
                    }
                }else{
                    replace.add(s);
                }
            }
            itemMeta.setLore(replace);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static CustomItem getCustomItemFromConfigPath(String path, FileConfiguration config){
        if(!config.contains(path+".slot") || !config.contains(path+".material")) {
            return null;
        }
        int slot = config.getInt(path+".slot");
        Material material = Material.valueOf(config.getString(path+".material"));
        String name = config.contains(path+".name") ? config.getString(path+".name") : "";
        List<String> lore = config.contains(path+".lore") ? config.getStringList(path+".lore") : new ArrayList<>();
        return new CustomItem(name, 1, material, (ArrayList<String>) lore, slot, CustomActionType.DECORATION);
    }
}