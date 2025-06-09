package me.dewrs.model.internal;

import me.dewrs.model.CustomInventory;
import me.dewrs.model.CustomItem;
import me.dewrs.model.ModifyData;
import me.dewrs.model.NodeLog;
import net.luckperms.api.model.group.Group;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InventoryPlayer {
    private String name;
    private CustomInventory customInventory;
    private UUID targetUuid;
    private String targetName;
    private ModifyData modifyData;
    private int actualPage;
    private List<List<Group>> paginatedGroups;
    private List<List<NodeLog>> paginatedNodeLogs;
    private List<List<Map.Entry<Group, CustomItem>>> paginatedCustomGroups;
    private boolean navigating;

    public InventoryPlayer(String name, CustomInventory customInventory, ModifyData modifyData) {
        this.name = name;
        this.customInventory = customInventory;
        this.modifyData = modifyData;
        this.actualPage = 0;
    }

    public InventoryPlayer(String name, CustomInventory customInventory, UUID targetUuid, ModifyData modifyData) {
        this.name = name;
        this.customInventory = customInventory;
        this.targetUuid = targetUuid;
        this.modifyData = modifyData;
        this.actualPage = 0;
    }

    public void setPaginatedGroups(List<List<Group>> paginatedGroups) {
        this.paginatedGroups = paginatedGroups;
    }

    public List<Group> getGroupsForPage(int page) {
        if (paginatedGroups == null || page < 0 || page >= paginatedGroups.size()) return Collections.emptyList();
        return paginatedGroups.get(page);
    }

    public List<Map.Entry<Group, CustomItem>> getCustomGroupsForPage(int page) {
        if (paginatedCustomGroups == null || page < 0 || page >= paginatedCustomGroups.size()) {
            return Collections.emptyList();
        }
        return paginatedCustomGroups.get(page);
    }

    public int getTotalPagesCustom() {
        return (paginatedCustomGroups != null) ? paginatedCustomGroups.size() : 0;
    }

    public List<NodeLog> getNodesForPage(int page) {
        if (paginatedNodeLogs == null || page < 0 || page >= paginatedNodeLogs.size()) return Collections.emptyList();
        return paginatedNodeLogs.get(page);
    }

    public int getTotalPages() {
        return (paginatedGroups != null) ? paginatedGroups.size() : 0;
    }


    public int getTotalNodeLogPages() {
        return (paginatedNodeLogs != null) ? paginatedNodeLogs.size() : 0;
    }

    public void setPaginatedNodeLogs(List<List<NodeLog>> paginatedNodeLogs) {
        this.paginatedNodeLogs = paginatedNodeLogs;
    }

    public void resetPagination() {
        paginatedGroups = null;
        paginatedNodeLogs = null;
        paginatedCustomGroups = null;
        this.actualPage = 0;
    }

    public boolean isNavigating() {
        return navigating;
    }

    public void setPaginatedCustomGroups(List<List<Map.Entry<Group, CustomItem>>> paginatedCustomGroups) {
        this.paginatedCustomGroups = paginatedCustomGroups;
    }

    public void setNavigating(boolean navigating) {
        this.navigating = navigating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CustomInventory getCustomInventory() {
        return customInventory;
    }

    public void setCustomInventory(CustomInventory customInventory) {
        this.customInventory = customInventory;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public void setTargetUuid(UUID targetUuid) {
        this.targetUuid = targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public ModifyData getModifyData() {
        return modifyData;
    }

    public void setModifyData(ModifyData modifyData) {
        this.modifyData = modifyData;
    }

    public int getActualPage() {
        return actualPage;
    }

    public void setActualPage(int actualPage) {
        this.actualPage = actualPage;
    }
}
