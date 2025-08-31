package me.dewrs.utils;

import me.dewrs.GrantRank;
import org.bukkit.Bukkit;

public enum ServerVersion {
    v1_8_R1,
    v1_8_R2,
    v1_8_R3,
    v1_9_R1,
    v1_9_R2,
    v1_10_R1,
    v1_11_R1,
    v1_12_R1,
    v1_13_R1,
    v1_13_R2,
    v1_14_R1,
    v1_15_R1,
    v1_16_R1,
    v1_16_R2,
    v1_16_R3,
    v1_17_R1,
    v1_18_R1,
    v1_18_R2,
    v1_19_R1,
    v1_19_R2,
    v1_19_R3,
    v1_20_R1,
    v1_20_R2,
    v1_20_R3,
    v1_20_R4,
    v1_21_R1,
    v1_21_R2,
    v1_21_R3,
    v1_21_R4,
    v1_21_R5;

    private static boolean isNewerThanOrEqualTo(ServerVersion version1, ServerVersion version2) {
        return version1.ordinal() >= version2.ordinal();
    }

    public static boolean isLegacyServer(){
        return !isNewerThanOrEqualTo(GrantRank.getServerVersion(), v1_16_R1);
    }

    public static String getBukkitVersion(){
        return Bukkit.getServer().getBukkitVersion().split("-")[0];
    }

    public static ServerVersion getServerVersion(){
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String bukkitVersion = GrantRank.getBukkitVersion();
        switch (bukkitVersion) {
            case "1.20.5":
            case "1.20.6": {
                return ServerVersion.v1_20_R4;
            }
            case "1.21":
            case "1.21.1": {
                return ServerVersion.v1_21_R1;
            }
            case "1.21.2":
            case "1.21.3": {
                return ServerVersion.v1_21_R2;
            }
            case "1.21.4": return ServerVersion.v1_21_R3;
            case "1.21.5": return ServerVersion.v1_21_R4;
            case "1.21.6":
            case "1.21.7":
            case "1.21.8": {
                return ServerVersion.v1_21_R5;
            }
            default: {
                try{
                    return ServerVersion.valueOf(packageName.replace("org.bukkit.craftbukkit.", ""));
                }catch (Exception e){
                    return ServerVersion.v1_21_R5;
                }
            }
        }
    }
}
