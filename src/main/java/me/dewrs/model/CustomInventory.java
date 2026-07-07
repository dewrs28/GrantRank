package me.dewrs.model;

import java.util.ArrayList;

public class CustomInventory {
    private String inv;
    private String title;
    private int rows;
    private ArrayList<CustomItem> customItems;

    public CustomInventory(String inv, String title, int rows, ArrayList<CustomItem> customItems) {
        this.inv = inv;
        this.title = title;
        this.rows = rows;
        this.customItems = customItems;
    }

    public String getInv() {
        return inv;
    }

    public void setInv(String inv) {
        this.inv = inv;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public ArrayList<CustomItem> getCustomItems() {
        return customItems;
    }

    public void setCustomItems(ArrayList<CustomItem> customItems) {
        this.customItems = customItems;
    }
}
