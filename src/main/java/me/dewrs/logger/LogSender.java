package me.dewrs.logger;

import me.dewrs.GrantRank;
import me.dewrs.utils.MessageUtils;
import org.bukkit.Bukkit;

public class LogSender {
    public static void sendLogMessage(String message){
        Bukkit.getConsoleSender().sendMessage(GrantRank.PREFIX+MessageUtils.getColoredMessage(message));
    }
}
