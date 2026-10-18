package com.harukiyu.clarityessentials.mail;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Mail {

    private final String id;
    private final String senderName;
    private final UUID senderUuid;
    private final long timestamp;
    private final long expireTimestamp; // 0 for permanent
    private final String message;
    private boolean read;
    private final boolean global;

    public Mail(@NotNull String id,
                @NotNull String senderName,
                @Nullable UUID senderUuid,
                long timestamp,
                long expireTimestamp,
                @NotNull String message,
                boolean read,
                boolean global) {
        this.id = id;
        this.senderName = senderName;
        this.senderUuid = senderUuid;
        this.timestamp = timestamp;
        this.expireTimestamp = expireTimestamp;
        this.message = message;
        this.read = read;
        this.global = global;
    }

    public String getId() {
        return id;
    }

    public String getSenderName() {
        return senderName;
    }

    @Nullable
    public UUID getSenderUuid() {
        return senderUuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getExpireTimestamp() {
        return expireTimestamp;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isGlobal() {
        return global;
    }

    public boolean isExpired() {
        return expireTimestamp > 0 && System.currentTimeMillis() > expireTimestamp;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("sender", senderName);
        if (senderUuid != null) {
            map.put("senderUuid", senderUuid.toString());
        }
        map.put("timestamp", timestamp);
        map.put("expire", expireTimestamp);
        map.put("message", message);
        map.put("read", read);
        return map;
    }

    public static Mail fromMap(@NotNull String id, @NotNull Map<String, Object> map, boolean global) {
        String sender = (String) map.getOrDefault("sender", "Server");
        String uuidStr = (String) map.get("senderUuid");
        UUID senderUuid = null;
        if (uuidStr != null) {
            try {
                senderUuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {}
        }
        long timestamp = ((Number) map.getOrDefault("timestamp", System.currentTimeMillis())).longValue();
        long expire = ((Number) map.getOrDefault("expire", 0L)).longValue();
        String message = (String) map.getOrDefault("message", "");
        boolean read = (Boolean) map.getOrDefault("read", false);
        return new Mail(id, sender, senderUuid, timestamp, expire, message, read, global);
    }
}
