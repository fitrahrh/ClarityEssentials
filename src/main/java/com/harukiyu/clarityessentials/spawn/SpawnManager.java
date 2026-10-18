package com.harukiyu.clarityessentials.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class SpawnManager {

    private final Plugin plugin;
    private final File spawnFile;
    private YamlConfiguration spawnConfig;

    private Location defaultSpawn;
    private Location firstJoinSpawn;

    public SpawnManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.spawnFile = new File(new File(plugin.getDataFolder(), "data"), "spawns.yml");
        load();
    }

    public final synchronized void load() {
        if (!spawnFile.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            spawnFile.getParentFile().mkdirs();
        }
        if (!spawnFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                spawnFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create spawns.yml", e);
            }
        }
        this.spawnConfig = YamlConfiguration.loadConfiguration(spawnFile);
        this.defaultSpawn = parseLocation(spawnConfig.getConfigurationSection("default"));
        this.firstJoinSpawn = parseLocation(spawnConfig.getConfigurationSection("firstjoin"));
    }

    public synchronized void save() {
        if (spawnConfig == null) spawnConfig = new YamlConfiguration();

        if (defaultSpawn != null) {
            saveLocation("default", defaultSpawn);
        } else {
            spawnConfig.set("default", null);
        }

        if (firstJoinSpawn != null) {
            saveLocation("firstjoin", firstJoinSpawn);
        } else {
            spawnConfig.set("firstjoin", null);
        }

        try {
            spawnConfig.save(spawnFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save spawns.yml", e);
        }
    }

    private void saveLocation(String path, Location loc) {
        spawnConfig.set(path + ".world", loc.getWorld() != null ? loc.getWorld().getName() : "world");
        spawnConfig.set(path + ".x", loc.getX());
        spawnConfig.set(path + ".y", loc.getY());
        spawnConfig.set(path + ".z", loc.getZ());
        spawnConfig.set(path + ".yaw", (double) loc.getYaw());
        spawnConfig.set(path + ".pitch", (double) loc.getPitch());
    }

    @Nullable
    private Location parseLocation(@Nullable ConfigurationSection section) {
        if (section == null) return null;
        String worldName = section.getString("world");
        if (worldName == null) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw", 0.0);
        float pitch = (float) section.getDouble("pitch", 0.0);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setDefaultSpawn(@NotNull Location location) {
        this.defaultSpawn = location.clone();
        save();
    }

    public void setFirstJoinSpawn(@NotNull Location location) {
        this.firstJoinSpawn = location.clone();
        save();
    }

    @NotNull
    public Location getSpawn() {
        if (defaultSpawn != null && defaultSpawn.getWorld() != null) {
            return defaultSpawn.clone();
        }
        if (!Bukkit.getWorlds().isEmpty()) {
            return Bukkit.getWorlds().getFirst().getSpawnLocation();
        }
        return new Location(null, 0, 64, 0);
    }

    @NotNull
    public Location getFirstJoinSpawn() {
        if (firstJoinSpawn != null && firstJoinSpawn.getWorld() != null) {
            return firstJoinSpawn.clone();
        }
        return getSpawn();
    }

    public boolean hasCustomSpawn() {
        return defaultSpawn != null;
    }

    public boolean hasFirstJoinSpawn() {
        return firstJoinSpawn != null;
    }
}
