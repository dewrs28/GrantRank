package me.dewrs.utils;

import me.dewrs.config.ConfigManager;
import me.dewrs.enums.CustomActionType;
import me.dewrs.enums.NodeType;
import me.dewrs.enums.SoundType;
import me.dewrs.logger.LogMessage;
import me.dewrs.logger.LogSender;
import me.dewrs.model.CustomInventory;
import me.dewrs.model.CustomItem;
import me.dewrs.model.ModifyData;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class OtherUtils {
    public static boolean hasSpaces(String string) {
        String[] splitSpaces = string.split(" ");
        return splitSpaces.length != 1;
    }

    public static boolean isValidContext(String context) {
        if (hasSpaces(context)) return false;
        String[] splitContext = context.split("=");
        return splitContext.length != 1;
    }

    public static boolean isValidPermission(String permission) {
        if (hasSpaces(permission)) return false;
        String[] splitPermission = permission.split("\\.");
        return splitPermission.length != 1;
    }

    public static NodeType getNodeType(ModifyData modifyData) {
        if (modifyData.getRank() == null) {
            return NodeType.PERMISSION;
        }
        return NodeType.RANK;
    }

    public static NodeType getNodeType(String node) {
        if(node.startsWith("group.")){
            return NodeType.RANK;
        }
        return NodeType.PERMISSION;
    }

    public static boolean isValidPageFormat(String input) {
        return input.matches("^page_\\d+$");
    }

    public static CustomInventory cloneCustomInventory(CustomInventory customInventory){
        return new CustomInventory(customInventory.getInv(), customInventory.getTitle(), customInventory.getRows(), customInventory.getCustomItems());
    }

    public static String replaceEndingNumbers(String input, int newNumber) {
        if (input.matches(".*\\d+$")) {
            if (newNumber == 0) {
                return input.replaceAll("\\d+$", "");
            } else {
                return input.replaceAll("\\d+$", String.valueOf(newNumber));
            }
        } else {
            if (newNumber == 0) {
                return input;
            } else {
                return input + newNumber;
            }
        }
    }

    public static boolean isValidSlot(int slot, int rows){
        return slot <= (rows * 9) - 1;
    }

    public static boolean isValidSlot(int slot, int rows, ArrayList<CustomItem> customItems){
        boolean validSlot = isValidSlot(slot, rows);
        for(CustomItem ci : customItems){
            if(ci.getSlot() == slot){
                validSlot = false;
                break;
            }
        }
        return validSlot;
    }

    public static boolean isValidCustomAction(CustomActionType customActionType, String inv){
        switch (customActionType){
            case CONFIRM_REVOKE:
            case CANCEL_REVOKE:
            case NODE_LOG:
            case GRANT: {
                return false;
            }
            case NEXT:
            case BACK: {
                return inv.equals("grants.yml") || inv.equals("nodes-logs.yml");
            }
            case VIEW_GRANTS:
            case ADD_PERMISSION: {
                return inv.equals("grants.yml");
            }
            case GRANT_GIVE:
            case OPEN_INVENTORY: {
                return inv.equals("grant-finish.yml");
            }
            case DECORATION: {
                return true;
            }
            case SET_TIME: {
                return inv.equals("select-time.yml");
            }
            case FINISH_GRANT:
            case SET_CONTEXT: {
                return inv.equals("contexts.yml");
            }
            default: return false;
        }
    }

    public static boolean isValidOpenInventory(String inv, List<String> validInv){
        for(String i : validInv){
            if(inv.equals(i) && (!inv.equals("grants.yml") && !inv.equals("grant-finish.yml") && !inv.equals("nodes-logs.yml"))) {
                return true;
            }
        }
        return false;
    }

    public static void playSound(Player player, int volume, float pitch, SoundType soundType, ConfigManager configManager){
        String soundName = "";
        switch (soundType){
            case OPEN_INV: {
                if(!configManager.isEnabledSoundOpenInv()) return;
                soundName = configManager.getSoundOpenInv();
                break;
            }
            case FINISH_GRANT: {
                if(!configManager.isEnabledSoundFinishGrant()) return;
                soundName = configManager.getSoundFinishGrant();
                break;
            }
            case NO_PERM: {
                if(!configManager.isEnabledSoundNoPerm()) return;
                soundName = configManager.getSoundNoPerm();
                break;
            }
        }
        Sound sound = null;
        try {
            sound = getSoundByName(soundName);
        }catch(Exception e ) {
            LogSender.sendLogMessage(LogMessage.INVALID_SOUND.format(soundName));
            return;
        }
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private static Sound getSoundByName(String name){
        try {
            Class<?> soundTypeClass = Class.forName("org.bukkit.Sound");
            Method valueOf = soundTypeClass.getMethod("valueOf", String.class);
            return (Sound) valueOf.invoke(null,name);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}