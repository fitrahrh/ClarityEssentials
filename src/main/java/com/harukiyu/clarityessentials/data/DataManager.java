package com.harukiyu.clarityessentials.data;

import com.harukiyu.clarityessentials.home.Home;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class DataManager {

    private final Plugin plugin;
    private final File dataFolder;
    private final File playersFile;
    private final File homesFile;

    private YamlConfiguration playersConfig;
    private YamlConfiguration homesConfig;

    // In-memory cache
    // UUID -> HomeName (lowercase) -> Home
    private final Map<UUID, Map<String, Home>> homesCache = new ConcurrentHashMap<>();
    
    // UUID -> Nickname (MiniMessage/color formatted)
    private final Map<UUID, String> nicknameCache = new ConcurrentHashMap<>();
    
    // UUID -> Raw Nickname (stripped plain text for regex and reverse lookup)
    private final Map<UUID, String> rawNicknameCache = new ConcurrentHashMap<>();
    
    // Raw Nickname (lowercase) -> UUID
    private final Map<String, UUID> nicknameToUuidMap = new ConcurrentHashMap<>();
    
    // UUID -> TPA enabled status (default true)
    private final Map<UUID, Boolean> tpaToggleCache = new ConcurrentHashMap<>();

    public DataManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        this.playersFile = new File(dataFolder, "players.yml");
        this.homesFile = new File(dataFolder, "homes.yml");
    }

    public void load() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        loadPlayers();
        loadHomes();
    }

    private void loadPlayers() {
        if (!playersFile.exists()) {
            try {
                playersFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create players.yml", e);
            }
        }
        playersConfig = YamlConfiguration.loadConfiguration(playersFile);

        nicknameCache.clear();
        rawNicknameCache.clear();
        nicknameToUuidMap.clear();
        tpaToggleCache.clear();

        ConfigurationSection section = playersConfig.getConfigurationSection("players");
        if (section != null) {
            for (String uuidStr : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String nick = section.getString(uuidStr + ".nickname");
                    String rawNick = section.getString(uuidStr + ".raw-nickname");
                    boolean tpaEnabled = section.getBoolean(uuidStr + ".tpa-enabled", true);

                    if (nick != null && !nick.isBlank()) {
                        nicknameCache.put(uuid, nick);
                    }
                    if (rawNick != null && !rawNick.isBlank()) {
                        rawNicknameCache.put(uuid, rawNick);
                        nicknameToUuidMap.put(rawNick.toLowerCase(Locale.ROOT), uuid);
                    }
                    tpaToggleCache.put(uuid, tpaEnabled);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    private void loadHomes() {
        if (!homesFile.exists()) {
            try {
                homesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create homes.yml", e);
            }
        }
        homesConfig = YamlConfiguration.loadConfiguration(homesFile);
        homesCache.clear();

        ConfigurationSection section = homesConfig.getConfigurationSection("homes");
        if (section != null) {
            for (String uuidStr : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    ConfigurationSection playerHomes = section.getConfigurationSection(uuidStr);
                    if (playerHomes != null) {
                        Map<String, Home> map = new ConcurrentHashMap<>();
                        for (String homeName : playerHomes.getKeys(false)) {
                            ConfigurationSection homeSection = playerHomes.getConfigurationSection(homeName);
                            if (homeSection != null) {
                                Home home = Home.fromMap(homeName, homeSection.getValues(false));
                                map.put(homeName.toLowerCase(Locale.ROOT), home);
                            }
                        }
                        homesCache.put(uuid, map);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public synchronized void saveSync() {
        // Save Players
        if (playersConfig == null) playersConfig = new YamlConfiguration();
        playersConfig.set("players", null);

        Set<UUID> allUuids = new HashSet<>();
        allUuids.addAll(nicknameCache.keySet());
        allUuids.addAll(tpaToggleCache.keySet());

        for (UUID uuid : allUuids) {
            String path = "players." + uuid.toString();
            String nick = nicknameCache.get(uuid);
            String rawNick = rawNicknameCache.get(uuid);
            boolean tpaEnabled = tpaToggleCache.getOrDefault(uuid, true);

            if (nick != null) playersConfig.set(path + ".nickname", nick);
            if (rawNick != null) playersConfig.set(path + ".raw-nickname", rawNick);
            playersConfig.set(path + ".tpa-enabled", tpaEnabled);
        }

        try {
            playersConfig.save(playersFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save players.yml", e);
        }

        // Save Homes
        if (homesConfig == null) homesConfig = new YamlConfiguration();
        homesConfig.set("homes", null);

        for (Map.Entry<UUID, Map<String, Home>> entry : homesCache.entrySet()) {
            String uuidPath = "homes." + entry.getKey().toString();
            for (Home home : entry.getValue().values()) {
                homesConfig.set(uuidPath + "." + home.getName(), home.toMap());
            }
        }

        try {
            homesConfig.save(homesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save homes.yml", e);
        }
    }

    public void saveAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveSync);
    }

    // --- Nickname Cache Access ---
    @Nullable
    public String getNickname(@NotNull UUID uuid) {
        return nicknameCache.get(uuid);
    }

    @Nullable
    public String getRawNickname(@NotNull UUID uuid) {
        return rawNicknameCache.get(uuid);
    }

    public void setNickname(@NotNull UUID uuid, @Nullable String nickname, @Nullable String rawNickname) {
        // Remove previous reverse mapping
        String oldRaw = rawNicknameCache.remove(uuid);
        if (oldRaw != null) {
            nicknameToUuidMap.remove(oldRaw.toLowerCase(Locale.ROOT));
        }

        if (nickname == null || rawNickname == null || nickname.isBlank()) {
            nicknameCache.remove(uuid);
        } else {
            nicknameCache.put(uuid, nickname);
            rawNicknameCache.put(uuid, rawNickname);
            nicknameToUuidMap.put(rawNickname.toLowerCase(Locale.ROOT), uuid);
        }
        saveAsync();
    }

    @Nullable
    public UUID getUuidByNickname(@NotNull String rawNick) {
        return nicknameToUuidMap.get(rawNick.toLowerCase(Locale.ROOT));
    }

    // --- TPA Toggle Cache Access ---
    public boolean isTpaEnabled(@NotNull UUID uuid, boolean defaultValue) {
        return tpaToggleCache.getOrDefault(uuid, defaultValue);
    }

    public void setTpaEnabled(@NotNull UUID uuid, boolean enabled) {
        tpaToggleCache.put(uuid, enabled);
        saveAsync();
    }

    // --- Homes Cache Access ---
    @NotNull
    public Map<String, Home> getPlayerHomes(@NotNull UUID uuid) {
        return homesCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
    }

    @Nullable
    public Home getHome(@NotNull UUID uuid, @NotNull String homeName) {
        Map<String, Home> map = homesCache.get(uuid);
        if (map == null) return null;
        return map.get(homeName.toLowerCase(Locale.ROOT));
    }

    public void setHome(@NotNull UUID uuid, @NotNull Home home) {
        Map<String, Home> map = homesCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        map.put(home.getName().toLowerCase(Locale.ROOT), home);
        saveAsync();
    }

    public boolean deleteHome(@NotNull UUID uuid, @NotNull String homeName) {
        Map<String, Home> map = homesCache.get(uuid);
        if (map == null) return false;
        Home removed = map.remove(homeName.toLowerCase(Locale.ROOT));
        if (removed != null) {
            saveAsync();
            return true;
        }
        return false;
    }
}
