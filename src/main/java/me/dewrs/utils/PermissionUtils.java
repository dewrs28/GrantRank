package me.dewrs.utils;

import org.bukkit.entity.Player;

public class PermissionUtils {
    private static final String MAIN_PREFIX = "grant.";
    private static final String GIVE_PREFIX = "give.";
    private static final String REVOKE_PREFIX = "revoke.";

    public static boolean canUseGrants(Player player){
        return player.hasPermission(MAIN_PREFIX+"use");
        // grant.use
    }

    public static boolean canGrantRank(Player player, String rank){
        return player.hasPermission(MAIN_PREFIX+GIVE_PREFIX+"rank."+rank);
        // grant.give.rank.(rank)
    }

    public static boolean canGivePermission(Player player){
        return player.hasPermission(MAIN_PREFIX+GIVE_PREFIX+"permission");
        // grant.give.permission
    }

    public static boolean canViewGlobalLogs(Player player){
        return player.hasPermission(MAIN_PREFIX+"logs.global");
        // grant.logs.global
    }

    public static boolean canViewUserLogs(Player player){
        return player.hasPermission(MAIN_PREFIX+"logs.player");
        // grant.logs.player
    }

    public static boolean isGrantRankAdmin(Player player){
        return player.hasPermission(MAIN_PREFIX+"admin");
        // grant.admin
    }

    public static boolean canReceiveNotifies(Player player){
        return player.hasPermission(MAIN_PREFIX+"notify");
        // grant.notify
    }

    public static boolean canRevokeRank(Player player, String rank){
        return player.hasPermission(MAIN_PREFIX+REVOKE_PREFIX+"rank."+rank);
        // grant.revoke.rank.(rank)
    }

    public static boolean canRevokePermission(Player player){
        return player.hasPermission(MAIN_PREFIX+REVOKE_PREFIX+"permission");
        // grant.revoke.permission
    }
}