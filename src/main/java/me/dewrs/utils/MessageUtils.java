package me.dewrs.utils;

import me.dewrs.GrantRank;
import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {

    public static String getColoredMessage(String message) {
        if(!GrantRank.isLegacyServer()) {
            Pattern pattern = Pattern.compile("(&?#[a-fA-F0-9]{6})");
            Matcher match = pattern.matcher(message);
            while(match.find()) {
                String fullMatch = match.group(1);
                String hexColor;
                if(fullMatch.startsWith("&#")) {
                    hexColor = fullMatch.substring(1);
                } else {
                    hexColor = fullMatch;
                }
                message = message.replace(fullMatch, ChatColor.of(hexColor) + "");
                match = pattern.matcher(message);
            }
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    public static String getStringFromList(List<String> strings){
        return String.join(", ", strings);
    }
}
