package me.dewrs.managers;

import me.dewrs.logger.LogSender;
import me.dewrs.model.internal.UpdateCheckerResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateCheckerManager {
    private String version;
    private String latestVersion;

    public UpdateCheckerManager(String version) {
        this.version = version;
        manageUpdateChecker();
    }

    public UpdateCheckerResult check(){
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=125879").openConnection();
            int timed_out = 1250;
            con.setConnectTimeout(timed_out);
            con.setReadTimeout(timed_out);
            latestVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            if (latestVersion.length() <= 7) {
                if(!version.equals(latestVersion)){
                    return UpdateCheckerResult.noErrors(latestVersion);
                }
            }
            return UpdateCheckerResult.noErrors(null);
        } catch (Exception ex) {
            return UpdateCheckerResult.error();
        }
    }

    public void manageUpdateChecker(){
        if (!this.check().isError()) {
            String latestVersion = this.check().getLatestVersion();
            if (latestVersion != null) {
                LogSender.sendLogMessage("*********************************************************************");
                LogSender.sendLogMessage("&cGrantRank is outdated!");
                LogSender.sendLogMessage("&cNewest version: &e"+latestVersion);
                LogSender.sendLogMessage("&cYour version: &e"+version);
                LogSender.sendLogMessage("&cPlease Update Here: &ehttps://spigotmc.org/resources/125879");
                LogSender.sendLogMessage("*********************************************************************");
            }
        } else {
            LogSender.sendLogMessage("&cError while checking update.");
        }
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}