package com.harukiyu.clarityessentials.home;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Home {

    private final String name;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final long createdAt;

    public Home(@NotNull String name,
                @NotNull String worldName,
                double x,
                double y,
                double z,
                float yaw,
                float pitch,
                long createdAt) {
        this.name = name;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.createdAt = createdAt;
    }

    public static Home fromLocation(@NotNull String name, @NotNull Location loc) {
        String worldName = loc.getWorld() != null ? loc.getWorld().getName() : "world";
        return new Home(
                name,
                worldName,
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch(),
                System.currentTimeMillis()
        );
    }

    public static Home fromMap(@NotNull String name, @NotNull Map<String, Object> map) {
        String world = (String) map.getOrDefault("world", "world");
        double x = ((Number) map.getOrDefault("x", 0.0)).doubleValue();
        double y = ((Number) map.getOrDefault("y", 64.0)).doubleValue();
        double z = ((Number) map.getOrDefault("z", 0.0)).doubleValue();
        float yaw = ((Number) map.getOrDefault("yaw", 0.0f)).floatValue();
        float pitch = ((Number) map.getOrDefault("pitch", 0.0f)).floatValue();
        long created = ((Number) map.getOrDefault("created", System.currentTimeMillis())).longValue();
        return new Home(name, world, x, y, z, yaw, pitch, created);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("world", worldName);
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        map.put("yaw", yaw);
        map.put("pitch", pitch);
        map.put("created", createdAt);
        return map;
    }

    @Nullable
    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getWorldName() {
        return worldName;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public long getCreatedAt() { return createdAt; }

    @NotNull
    public String getFormattedCoordinates() {
        return String.format("%.1f, %.1f, %.1f", x, y, z);
    }
}
