package me.dewrs.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {

    public static String getColoredMessage(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    public static String getStringFromList(List<String> strings){
        return String.join(", ", strings);
    }
}
