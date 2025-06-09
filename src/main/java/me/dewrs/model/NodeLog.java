package me.dewrs.model;

import net.luckperms.api.context.MutableContextSet;

import java.util.UUID;

public class NodeLog {
    private int id;
    private UUID uuid_user;
    private String name_user;
    private String name_operator;
    private String node;
    private long expiry;
    private String reason;
    private MutableContextSet contextSet;
    private long creation_time;

    public NodeLog(UUID uuid_user, String name_user, String name_operator, String node, long expiry, String reason, MutableContextSet contextSet, long creation_time) {
        this.uuid_user = uuid_user;
        this.name_user = name_user;
        this.name_operator = name_operator;
        this.node = node;
        this.expiry = expiry;
        this.reason = reason;
        this.contextSet = contextSet;
        this.creation_time = creation_time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getUuid_user() {
        return uuid_user;
    }

    public void setUuid_user(UUID uuid_user) {
        this.uuid_user = uuid_user;
    }

    public String getName_user() {
        return name_user;
    }

    public void setName_user(String name_user) {
        this.name_user = name_user;
    }

    public String getName_operator() {
        return name_operator;
    }

    public void setName_operator(String name_operator) {
        this.name_operator = name_operator;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public MutableContextSet getContextSet() {
        return contextSet;
    }

    public void setContextSet(MutableContextSet contextSet) {
        this.contextSet = contextSet;
    }

    public long getCreation_time() {
        return creation_time;
    }

    public void setCreation_time(long creation_time) {
        this.creation_time = creation_time;
    }
}
