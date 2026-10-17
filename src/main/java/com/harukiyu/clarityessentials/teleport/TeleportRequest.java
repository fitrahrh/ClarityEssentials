package com.harukiyu.clarityessentials.teleport;

import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TeleportRequest {

    public enum Type {
        NORMAL, // Sender teleports to Target (/tpa)
        HERE    // Target teleports to Sender (/tpahere)
    }

    private final UUID senderId;
    private final UUID targetId;
    private final Type type;
    private final long createdAt;
    private BukkitTask expireTask;

    public TeleportRequest(@NotNull UUID senderId, @NotNull UUID targetId, @NotNull Type type) {
        this.senderId = senderId;
        this.targetId = targetId;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
    }

    @NotNull
    public UUID getSenderId() { return senderId; }

    @NotNull
    public UUID getTargetId() { return targetId; }

    @NotNull
    public Type getType() { return type; }

    public long getCreatedAt() { return createdAt; }

    @Nullable
    public BukkitTask getExpireTask() { return expireTask; }

    public void setExpireTask(@Nullable BukkitTask expireTask) {
        this.expireTask = expireTask;
    }
}
