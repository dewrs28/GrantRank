package me.dewrs.utils;

import org.bukkit.ChatColor;

import java.util.List;

public class MessageUtils {

    public static String getColoredMessage(String message){
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String getStringFromList(List<String> strings){
        return String.join(", ", strings);
    }
}
