package me.dewrs.config;

import me.dewrs.GrantRank;
import org.bukkit.configuration.file.FileConfiguration;

public class DiscordConfigManager {
    private CustomConfig customConfig;
    private boolean isEnabledDiscordBot;
    private boolean isEnabledDiscordWebhook;
    private String discordBotToken;
    private String discordWebhookUrl;

    public DiscordConfigManager(GrantRank plugin){
        customConfig = new CustomConfig("discord.yml", null, plugin);
        customConfig.registerConfig();
    }

    public void reload(){
        customConfig.reloadConfig();
    }

    private void loadConfig(){
        FileConfiguration config = customConfig.getConfig();
        isEnabledDiscordBot = config.getBoolean("discord.bot.enabled");
        isEnabledDiscordWebhook = config.getBoolean("discord.webhook.enabled");
        discordBotToken = config.getString("discord.bot.bot_token");
        discordWebhookUrl = config.getString("discord.webhook.webhook_url");
    }

    public boolean isEnabledDiscordBot() {
        return isEnabledDiscordBot;
    }

    public boolean isEnabledDiscordWebhook() {
        return isEnabledDiscordWebhook;
    }

    public String getDiscordBotToken() {
        return discordBotToken;
    }

    public String getDiscordWebhookUrl() {
        return discordWebhookUrl;
    }
}
