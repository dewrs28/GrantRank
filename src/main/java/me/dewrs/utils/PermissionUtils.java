package me.dewrs.utils;

import org.bukkit.entity.Player;

public class PermissionUtils {
    private static final String mainPrefix = "grant.";
    private static final String givePrefix = "give.";
    private static final String revokePrefix = "revoke.";

    public static boolean canUseGrants(Player player){
        return player.hasPermission(mainPrefix+"use");
        // grant.use
    }

    public static boolean canGrantRank(Player player, String rank){
        return player.hasPermission(mainPrefix+givePrefix+"rank."+rank);
        // grant.give.rank.(rank)
    }

    public static boolean canGivePermission(Player player){
        return player.hasPermission(mainPrefix+givePrefix+"permission");
        // grant.give.permission
    }

    public static boolean canViewGlobalLogs(Player player){
        return player.hasPermission(mainPrefix+"logs.global");
        // grant.logs.global
    }

    public static boolean isGrantRankAdmin(Player player){
        return player.hasPermission(mainPrefix+"admin");
        // grant.admin
    }

    public static boolean canReceiveNotifies(Player player){
        return player.hasPermission(mainPrefix+"notify");
        // grant.notify
    }

    public static boolean canRevokeRank(Player player, String rank){
        return player.hasPermission(mainPrefix+revokePrefix+"rank."+rank);
        // grant.revoke.rank.(rank)
    }

    public static boolean canRevokePermission(Player player){
        return player.hasPermission(mainPrefix+revokePrefix+"permission");
        // grant.revoke.permission
    }
}