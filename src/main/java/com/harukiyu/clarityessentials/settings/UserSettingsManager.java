package com.harukiyu.clarityessentials.settings;

import com.harukiyu.clarityessentials.ClarityEssentials;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserSettingsManager {

    private final ClarityEssentials plugin;
    private final File settingsDir;
    private final Map<UUID, UserSettings> cache = new ConcurrentHashMap<>();

    public UserSettingsManager(@NotNull ClarityEssentials plugin) {
        this.plugin = plugin;
        this.settingsDir = new File(plugin.getDataFolder(), "userdata");
        if (!settingsDir.exists()) {
            settingsDir.mkdirs();
        }
    }

    @NotNull
    public UserSettings getSettings(@NotNull UUID uuid) {
        return cache.computeIfAbsent(uuid, this::loadSettings);
    }

    @NotNull
    public UserSettings getSettings(@NotNull Player player) {
        return getSettings(player.getUniqueId());
    }

    private UserSettings loadSettings(UUID uuid) {
        UserSettings settings = new UserSettings(uuid);
        File file = new File(settingsDir, uuid + ".yml");
        if (!file.exists()) {
            return settings;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        settings.setPublicChat(config.getBoolean("chat.publicChat", true));
        settings.setPrivateMessages(config.getString("chat.privateMessages", "Anyone"));
        settings.setServerChatMessages(config.getBoolean("chat.serverChatMessages", true));
        settings.setServerHotbarMessages(config.getBoolean("chat.serverHotbarMessages", true));
        settings.setDeathMessages(config.getString("chat.deathMessages", "Anyone"));
        settings.setAdvancementMessages(config.getString("chat.advancementMessages", "Friends"));
        settings.setJoinLeaveMessages(config.getString("chat.joinLeaveMessages", "Friends"));
        settings.setSocialSpy(config.getBoolean("chat.socialSpy", false));

        settings.setTeleportRequests(config.getString("privacy.teleportRequests", "Anyone"));
        settings.setTeleportHereRequests(config.getString("privacy.teleportHereRequests", "Anyone"));
        settings.setAllowPayments(config.getString("privacy.allowPayments", "Anyone"));
        settings.setRandomizedCoords(config.getBoolean("privacy.randomizedCoords", true));
        settings.setPrivateTransactions(config.getBoolean("privacy.privateTransactions", false));

        settings.setPayAlerts(config.getBoolean("notifications.payAlerts", true));
        settings.setTeleportAlerts(config.getBoolean("notifications.teleportAlerts", true));
        settings.setBountyAlerts(config.getBoolean("notifications.bountyAlerts", true));
        settings.setAuctionAlerts(config.getBoolean("notifications.auctionAlerts", true));
        settings.setOrderAlerts(config.getBoolean("notifications.orderAlerts", true));
        settings.setServerSounds(config.getBoolean("notifications.serverSounds", true));
        settings.setFollowAlerts(config.getBoolean("notifications.followAlerts", true));

        for (String p : config.getStringList("teleportList")) {
            settings.addTeleportPlayer(p);
        }
        for (String p : config.getStringList("payList")) {
            settings.addPayPlayer(p);
        }
        for (String f : config.getStringList("friendsList")) {
            settings.addFriend(f);
        }
        for (String f : config.getStringList("followingList")) {
            settings.addFollowing(f);
        }

        settings.setLastChangelogShownTime(config.getLong("misc.lastChangelogShownTime", 0L));
        settings.setLastDismissedUpdateId(config.getString("misc.lastDismissedUpdateId", ""));
        settings.setFirstJoinTipsShown(config.getBoolean("misc.firstJoinTipsShown", false));
        settings.setLanguage(config.getString("misc.language", ""));

        return settings;
    }

    public void saveSettingsAsync(@NotNull UUID uuid) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> saveSettings(uuid));
    }

    public void saveSettings(@NotNull UUID uuid) {
        UserSettings settings = cache.get(uuid);
        if (settings == null) return;

        File file = new File(settingsDir, uuid + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("chat.publicChat", settings.isPublicChat());
        config.set("chat.privateMessages", settings.getPrivateMessages());
        config.set("chat.serverChatMessages", settings.isServerChatMessages());
        config.set("chat.serverHotbarMessages", settings.isServerHotbarMessages());
        config.set("chat.deathMessages", settings.getDeathMessages());
        config.set("chat.advancementMessages", settings.getAdvancementMessages());
        config.set("chat.joinLeaveMessages", settings.getJoinLeaveMessages());
        config.set("chat.socialSpy", settings.isSocialSpy());

        config.set("privacy.teleportRequests", settings.getTeleportRequests());
        config.set("privacy.teleportHereRequests", settings.getTeleportHereRequests());
        config.set("privacy.allowPayments", settings.getAllowPayments());
        config.set("privacy.randomizedCoords", settings.isRandomizedCoords());
        config.set("privacy.privateTransactions", settings.isPrivateTransactions());

        config.set("notifications.payAlerts", settings.isPayAlerts());
        config.set("notifications.teleportAlerts", settings.isTeleportAlerts());
        config.set("notifications.bountyAlerts", settings.isBountyAlerts());
        config.set("notifications.auctionAlerts", settings.isAuctionAlerts());
        config.set("notifications.orderAlerts", settings.isOrderAlerts());
        config.set("notifications.serverSounds", settings.isServerSounds());
        config.set("notifications.followAlerts", settings.isFollowAlerts());

        config.set("teleportList", settings.getTeleportList());
        config.set("payList", settings.getPayList());
        config.set("friendsList", settings.getFriendsList());
        config.set("followingList", settings.getFollowingList());

        config.set("misc.lastChangelogShownTime", settings.getLastChangelogShownTime());
        config.set("misc.lastDismissedUpdateId", settings.getLastDismissedUpdateId());
        config.set("misc.firstJoinTipsShown", settings.isFirstJoinTipsShown());
        config.set("misc.language", settings.getLanguage());

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning(() -> "Failed to save user settings for " + uuid + ": " + e.getMessage());
        }
    }

    public void saveAll() {
        for (UUID uuid : cache.keySet()) {
            saveSettings(uuid);
        }
    }
}
