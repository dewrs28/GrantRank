package me.dewrs.model;

import me.dewrs.enums.CustomActionType;
import net.luckperms.api.model.group.Group;
import org.bukkit.Material;

import java.util.ArrayList;

public class CustomItem {
    private Material material;
    private String name;
    private int amount;
    private ArrayList<String> lore;
    private int slot;
    private CustomActionType customActionType;
    private String inventoryToOpen;
    private String cacheFormat;
    private Group grantToGive;
    private NodeLog nodeLog;

    public CustomItem(String name, int amount, Material material, ArrayList<String> lore, int slot, CustomActionType customActionType) {
        this.name = name;
        this.amount = amount;
        this.material = material;
        this.lore = lore;
        this.slot = slot;
        this.customActionType = customActionType;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public ArrayList<String> getLore() {
        return lore;
    }

    public void setLore(ArrayList<String> lore) {
        this.lore = lore;
    }

    public CustomActionType getCustomActionType() {
        return customActionType;
    }

    public void setCustomActionType(CustomActionType customActionType) {
        this.customActionType = customActionType;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public String getInventoryToOpen() {
        return inventoryToOpen;
    }

    public void setInventoryToOpen(String inventoryToOpen) {
        this.inventoryToOpen = inventoryToOpen;
    }

    public String getCacheFormat() {
        return cacheFormat;
    }

    public void setCacheFormat(String cacheFormat) {
        this.cacheFormat = cacheFormat;
    }

    public Group getGrantToGive() {
        return grantToGive;
    }

    public void setGrantToGive(Group grantToGive) {
        this.grantToGive = grantToGive;
    }

    public NodeLog getNodeLog() {
        return nodeLog;
    }

    public void setNodeLog(NodeLog nodeLog) {
        this.nodeLog = nodeLog;
    }
}