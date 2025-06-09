package me.dewrs.logger;

public enum LogMessage {
    ONLY_PLAYERS("&cThis action is only for players."),
    INVENTORY_INVALID_ROWS("&cInvalid number of rows from the inventory &e%s&c. The inventory could not be loaded."),
    INVENTORIES_LOADED("&aThe following inventories have been successfully loaded &e%s&a."),
    STORAGE_CORRECT("&aThe storage was successfully started on type: &e%s&a."),
    STORAGE_CONNECTION_ERROR("&cThe connection to &e%s &ccould not be made. Please check the connection information and storage type correctly."),
    INVALID_SOUND("&cThe sound &e%s &cis not valid. Please choose the correct one.");

    private final String message;

    LogMessage(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return String.format(this.message, args);
    }

    public String getRawMessage() {
        return this.message;
    }
}