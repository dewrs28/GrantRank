package me.dewrs.model;

import me.dewrs.enums.ChatModuleType;
import net.luckperms.api.model.group.Group;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

public class ModifyData {
    private Group rank;
    private ArrayList<String> contexts;
    private long time;
    private String reason;
    private String permission;
    private BukkitTask bukkitTask;
    private boolean isChatMode;
    private ChatModuleType chatSection;
    private NodeLog nodeLog;

    public ModifyData(){
        this.contexts = new ArrayList<>();
    }

    public Group getRank() {
        return rank;
    }

    public void setRank(Group rank) {
        this.rank = rank;
    }

    public ArrayList<String> getContexts() {
        return contexts;
    }

    public void setContexts(ArrayList<String> contexts) {
        this.contexts = contexts;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BukkitTask getBukkitTask() {
        return bukkitTask;
    }

    public void setBukkitTask(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean isChatMode() {
        return isChatMode;
    }

    public void setChatMode(boolean chatMode) {
        isChatMode = chatMode;
    }

    public ChatModuleType getChatSection() {
        return chatSection;
    }

    public void setChatSection(ChatModuleType chatSection) {
        this.chatSection = chatSection;
    }

    public NodeLog getNodeLog() {
        return nodeLog;
    }

    public void setNodeLog(NodeLog nodeLog) {
        this.nodeLog = nodeLog;
    }
}
