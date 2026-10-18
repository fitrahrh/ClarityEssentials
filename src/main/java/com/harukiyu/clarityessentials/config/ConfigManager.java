package com.harukiyu.clarityessentials.config;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConfigManager {

    private final Plugin plugin;
    private FileConfiguration config;

    // Chat
    private boolean chatEnabled;
    private String defaultChatFormat;
    private final Map<String, String> groupChatFormats = new HashMap<>();
    private String chatColorPermission;

    // Nickname
    private boolean nickEnabled;
    private String allowedNicksRegex;
    private Pattern allowedNicksPattern;
    private int nickMinLength;
    private int nickMaxLength;
    private boolean nickChangeTabName;
    private String nickUnsafeBypassPermission;
    private String nickColorPermission;

    // Teleport
    private int tpaTimeoutSeconds;
    private int tpaWarmupSeconds;
    private boolean tpaCancelOnMove;
    private double tpaMoveToleranceSquared;
    private boolean tpaAllowToggleDefault;
    private String tpaBypassWarmupPermission;

    // Homes
    private boolean homesEnabled;
    private int homeDefaultLimit;
    private String homeUnlimitedPermission;
    private final Map<String, Integer> homeRankLimits = new HashMap<>();

    // Sounds
    private boolean soundsEnabled;
    private Sound soundTeleportWarmup;
    private Sound soundTeleportSuccess;
    private Sound soundTeleportCancel;
    private Sound soundDialogOpen;
    private Sound soundActionSuccess;
    private Sound soundActionFailure;

    public ConfigManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        load();
    }

    public final void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        // Chat
        this.chatEnabled = config.getBoolean("chat.enabled", true);
        this.defaultChatFormat = config.getString("chat.default-format", "<gray>{prefix}</gray><white>{nickname}</white><gray>{suffix}</gray><dark_gray> » </dark_gray><white>{message}</white>");
        this.chatColorPermission = config.getString("chat.color-permission", "clarityessentials.chat.color");
        this.groupChatFormats.clear();
        ConfigurationSection groupSection = config.getConfigurationSection("chat.group-formats");
        if (groupSection != null) {
            for (String group : groupSection.getKeys(false)) {
                groupChatFormats.put(group.toLowerCase(), groupSection.getString(group, defaultChatFormat));
            }
        }

        // Nickname
        this.nickEnabled = config.getBoolean("nickname.enabled", true);
        this.allowedNicksRegex = config.getString("nickname.allowed-nicks-regex", "^[a-zA-Z0-9_]{3,16}$");
        try {
            this.allowedNicksPattern = Pattern.compile(allowedNicksRegex);
        } catch (PatternSyntaxException e) {
            plugin.getLogger().warning("Invalid allowed-nicks-regex in config.yml! Falling back to default.");
            this.allowedNicksPattern = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
        }
        this.nickMinLength = config.getInt("nickname.min-length", 3);
        this.nickMaxLength = config.getInt("nickname.max-length", 16);
        this.nickChangeTabName = config.getBoolean("nickname.change-tab-name", true);
        this.nickUnsafeBypassPermission = config.getString("nickname.unsafe-bypass-permission", "clarityessentials.nick.allowunsafe");
        this.nickColorPermission = config.getString("nickname.color-permission", "clarityessentials.nick.color");

        // Teleport
        this.tpaTimeoutSeconds = config.getInt("teleport.timeout-seconds", 60);
        this.tpaWarmupSeconds = config.getInt("teleport.warmup-seconds", 3);
        this.tpaCancelOnMove = config.getBoolean("teleport.cancel-on-move", true);
        this.tpaMoveToleranceSquared = config.getDouble("teleport.move-tolerance-squared", 0.05);
        this.tpaAllowToggleDefault = config.getBoolean("teleport.allow-toggle-default", true);
        this.tpaBypassWarmupPermission = config.getString("teleport.bypass-warmup-permission", "clarityessentials.tpa.bypasswarmup");

        // Homes
        this.homesEnabled = config.getBoolean("homes.enabled", true);
        this.homeDefaultLimit = config.getInt("homes.default-limit", 3);
        this.homeUnlimitedPermission = config.getString("homes.unlimited-permission", "clarityessentials.homes.unlimited");
        this.homeRankLimits.clear();
        ConfigurationSection limitsSection = config.getConfigurationSection("homes.limits");
        if (limitsSection != null) {
            for (String rank : limitsSection.getKeys(false)) {
                homeRankLimits.put(rank.toLowerCase(), limitsSection.getInt(rank, homeDefaultLimit));
            }
        }

        // Sounds
        this.soundsEnabled = config.getBoolean("sounds.enabled", true);
        this.soundTeleportWarmup = parseSound(config.getString("sounds.teleport-warmup"), Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        this.soundTeleportSuccess = parseSound(config.getString("sounds.teleport-success"), Sound.ENTITY_ENDERMAN_TELEPORT);
        this.soundTeleportCancel = parseSound(config.getString("sounds.teleport-cancel"), Sound.BLOCK_NOTE_BLOCK_BASS);
        this.soundDialogOpen = parseSound(config.getString("sounds.dialog-open"), Sound.UI_BUTTON_CLICK);
        this.soundActionSuccess = parseSound(config.getString("sounds.action-success"), Sound.ENTITY_PLAYER_LEVELUP);
        this.soundActionFailure = parseSound(config.getString("sounds.action-failure"), Sound.BLOCK_NOTE_BLOCK_BASS);

        // ESC Menu & Shortcuts
        this.escMenuEnabled = config.getBoolean("esc-menu.enabled", true);
        this.escMenuButtonText = config.getString("esc-menu.button-text", "Clarity");
        this.escMenuDialogTitle = config.getString("esc-menu.dialog-title", "Clarity");
        this.cmdRtp = config.getString("esc-menu.commands.rtp", "rtp");
        this.cmdRtpQueue = config.getString("esc-menu.commands.rtp-queue", "rtp queue");
        this.cmdAuction = config.getString("esc-menu.commands.auction", "ah");
        this.cmdQuickBuy = config.getString("esc-menu.commands.quick-buy", "buy");
        this.cmdOrders = config.getString("esc-menu.commands.orders", "orders");
        this.cmdShardShop = config.getString("esc-menu.commands.shard-shop", "shards");
        this.cmdSell = config.getString("esc-menu.commands.sell", "sell");
        this.cmdStats = config.getString("esc-menu.commands.stats", "stats");
        this.cmdSkills = config.getString("esc-menu.commands.skills", "skills");
        this.webstoreUrl = config.getString("esc-menu.webstore-url", "https://store.clarity-id.net");
        this.sellUrl = config.getString("esc-menu.sell-url", "https://store.clarity-id.net");

        // Fly System
        this.flyEnabled = config.getBoolean("fly.enabled", true);
        this.flyDefaultDuration = config.getString("fly.default-duration", "unlimited");
        this.flyWorldMode = config.getString("fly.world-mode", "BLACKLIST");
        this.flyWorlds = config.getStringList("fly.worlds").stream().map(String::toLowerCase).toList();
        this.flyRegionMode = config.getString("fly.region-mode", "BLACKLIST");
        this.flyRegions = config.getStringList("fly.regions").stream().map(String::toLowerCase).toList();
        this.flyFallProtectionSeconds = Math.max(1, config.getInt("fly.fall-protection-seconds", 15));

        // AFK System
        this.afkEnabled = config.getBoolean("afk.enabled", true);
        this.afkAutoUnafkOnMove = config.getBoolean("afk.auto-unafk-on-move", true);
        this.afkAutoUnafkOnChat = config.getBoolean("afk.auto-unafk-on-chat", true);

        // Join & Leave Messages
        this.joinLeaveEnabled = config.getBoolean("join-leave.enabled", true);
        this.defaultJoinMessage = config.getString("join-leave.default-join", "<dark_gray>[<green>+</green>]</dark_gray> <white><player></white> <gray>joined the game.</gray>");
        this.defaultLeaveMessage = config.getString("join-leave.default-leave", "<dark_gray>[<red>-</red>]</dark_gray> <white><player></white> <gray>left the game.</gray>");
        this.vipJoinRules.clear();
        ConfigurationSection vipSection = config.getConfigurationSection("join-leave.vip-join");
        if (vipSection != null) {
            for (String key : vipSection.getKeys(false)) {
                String perm = vipSection.getString(key + ".permission", "clarityessentials.join." + key);
                String msg = vipSection.getString(key + ".message", defaultJoinMessage);
                vipJoinRules.add(new VipJoinRule(key, perm, msg));
            }
        }

        // Settings commands
        this.cmdPvp = config.getString("settings.commands.pvp", "pvp");
        this.cmdScoreboard = config.getString("settings.commands.scoreboard", "sb toggle");

        // Spawn Settings
        this.spawnEnabled = config.getBoolean("spawn.enabled", true);
        this.spawnRespawnAtSpawn = config.getBoolean("spawn.respawn-at-spawn", true);
        this.spawnRespawnOverrideBed = config.getBoolean("spawn.respawn-override-bed", false);
        this.spawnTeleportOnFirstJoin = config.getBoolean("spawn.teleport-on-first-join", true);
        this.spawnTeleportOnJoin = config.getBoolean("spawn.teleport-on-join", false);

        // Mail Settings
        this.mailEnabled = config.getBoolean("mail.enabled", true);
        this.mailNotifyOnJoin = config.getBoolean("mail.notify-on-join", true);

        // JoinDialog & Changelog Settings
        this.joinDialogEnabled = config.getBoolean("join-dialog.enabled", true);
        this.joinDialogUpdateId = config.getString("join-dialog.current-update-id", "v1.0.0");
        this.joinDialogDailyReminder = config.getBoolean("join-dialog.daily-reminder", true);
        this.firstJoinTipsEnabled = config.getBoolean("join-dialog.first-join-tips.enabled", true);
        this.firstJoinTipsTitle = config.getString("join-dialog.first-join-tips.title", "<gradient:#00fbff:#0077ff><bold>Welcome to Clarity Network!</bold></gradient>");
        this.firstJoinTipsBody = config.getStringList("join-dialog.first-join-tips.body");
        if (this.firstJoinTipsBody.isEmpty()) {
            this.firstJoinTipsBody = java.util.List.of(
                    "<yellow>★ <bold>Beginner Tips & Guidelines:</bold></yellow>",
                    "<gray>1. Use <white>/menu</white> or press <white>ESC</white> to open the Central Dashboard.</gray>",
                    "<gray>2. Teleport into the wilderness using <white>/rtp</white> to build your base.</gray>",
                    "<gray>3. Set a home using <white>/sethome <name></white> so you can easily return.</gray>",
                    "<gray>4. Protect your land and trade with others using <white>/ah</white> and <white>/pay</white>.</gray>",
                    "<gray>5. Need help? Contact our staff or join our community Discord!</gray>"
            );
        }
        this.changelogTitle = config.getString("join-dialog.changelog.title", "<gradient:#ffaa00:#ff5555><bold>Server Updates & Changelog</bold></gradient>");
        this.changelogBody = config.getStringList("join-dialog.changelog.body");
        if (this.changelogBody.isEmpty()) {
            this.changelogBody = java.util.List.of(
                    "<gold><bold>What's New in Clarity Network:</bold></gold>",
                    "<gray>• Added <white>/spawn</white> with dedicated new player first-join spawn point.</gray>",
                    "<gray>• Added <white>/mail</white> system for persistent inboxes & announcements.</gray>",
                    "<gray>• Added <white>/speed</white> command to adjust walking and flight speed.</gray>",
                    "<gray>• Added <white>/fly</white>, <white>/feed</white>, <white>/heal</white>, and <white>/afk</white> with custom reasons.</gray>",
                    "<gray>• Added fast gamemode shortcuts: <white>/gmc</white>, <white>/gms</white>, <white>/gmsp</white>, <white>/gma</white>.</gray>",
                    "<gray>• Dashboard updated with <white>Skills</white> and official <white>Webstore</white>!</gray>"
            );
        }
    }

    @SuppressWarnings("removal")
    private Sound parseSound(@Nullable String name, @NotNull Sound fallback) {
        if (name == null || name.isBlank()) return fallback;
        try {
            // First check by direct Bukkit enum name (e.g. ENTITY_ENDERMAN_TELEPORT)
            String enumFormatted = name.toUpperCase().replace('.', '_');
            return Sound.valueOf(enumFormatted);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    // Getters
    public boolean isChatEnabled() { return chatEnabled; }
    public String getDefaultChatFormat() { return defaultChatFormat; }
    public Map<String, String> getGroupChatFormats() { return groupChatFormats; }
    public String getChatColorPermission() { return chatColorPermission; }

    public boolean isNickEnabled() { return nickEnabled; }
    public Pattern getAllowedNicksPattern() { return allowedNicksPattern; }
    public String getAllowedNicksRegex() { return allowedNicksRegex; }
    public int getNickMinLength() { return nickMinLength; }
    public int getNickMaxLength() { return nickMaxLength; }
    public boolean isNickChangeTabName() { return nickChangeTabName; }
    public String getNickUnsafeBypassPermission() { return nickUnsafeBypassPermission; }
    public String getNickColorPermission() { return nickColorPermission; }

    public int getTpaTimeoutSeconds() { return tpaTimeoutSeconds; }
    public int getTpaWarmupSeconds() { return tpaWarmupSeconds; }
    public boolean isTpaCancelOnMove() { return tpaCancelOnMove; }
    public double getTpaMoveToleranceSquared() { return tpaMoveToleranceSquared; }
    public boolean isTpaAllowToggleDefault() { return tpaAllowToggleDefault; }
    public String getTpaBypassWarmupPermission() { return tpaBypassWarmupPermission; }

    public boolean isHomesEnabled() { return homesEnabled; }
    public int getHomeDefaultLimit() { return homeDefaultLimit; }
    public String getHomeUnlimitedPermission() { return homeUnlimitedPermission; }
    public Map<String, Integer> getHomeRankLimits() { return homeRankLimits; }

    // ESC Menu & Dashboard Shortcuts
    private boolean escMenuEnabled = true;
    private String escMenuButtonText = "Clarity";
    private String escMenuDialogTitle = "Clarity";
    private String cmdRtp = "rtp";
    private String cmdRtpQueue = "rtp queue";
    private String cmdAuction = "ah";
    private String cmdQuickBuy = "buy";
    private String cmdOrders = "orders";
    private String cmdShardShop = "shards";
    private String cmdSell = "sell";
    private String cmdStats = "stats";
    private String cmdSkills = "skills";
    private String webstoreUrl = "https://store.clarity-id.net";
    private String sellUrl = "https://store.clarity-id.net";

    public boolean isSoundsEnabled() { return soundsEnabled; }
    public Sound getSoundTeleportWarmup() { return soundTeleportWarmup; }
    public Sound getSoundTeleportSuccess() { return soundTeleportSuccess; }
    public Sound getSoundTeleportCancel() { return soundTeleportCancel; }
    public Sound getSoundDialogOpen() { return soundDialogOpen; }
    public Sound getSoundActionSuccess() { return soundActionSuccess; }
    public Sound getSoundActionFailure() { return soundActionFailure; }

    public boolean isEscMenuEnabled() { return escMenuEnabled; }
    public String getEscMenuButtonText() { return escMenuButtonText; }
    public String getEscMenuDialogTitle() { return escMenuDialogTitle; }
    public String getCmdRtp() { return cmdRtp; }
    public String getCmdRtpQueue() { return cmdRtpQueue; }
    public String getCmdAuction() { return cmdAuction; }
    public String getCmdQuickBuy() { return cmdQuickBuy; }
    public String getCmdOrders() { return cmdOrders; }
    public String getCmdShardShop() { return cmdShardShop; }
    public String getCmdSell() { return cmdSell; }
    public String getCmdStats() { return cmdStats; }
    public String getCmdSkills() { return cmdSkills; }
    public String getWebstoreUrl() { return webstoreUrl; }
    public String getSellUrl() { return sellUrl; }

    // Fly Settings
    private boolean flyEnabled = true;
    private String flyDefaultDuration = "unlimited";
    private String flyWorldMode = "BLACKLIST";
    private java.util.List<String> flyWorlds = new java.util.ArrayList<>();
    private String flyRegionMode = "BLACKLIST";
    private java.util.List<String> flyRegions = new java.util.ArrayList<>();
    private int flyFallProtectionSeconds = 15;

    public boolean isFlyEnabled() { return flyEnabled; }
    public String getFlyDefaultDuration() { return flyDefaultDuration; }
    public String getFlyWorldMode() { return flyWorldMode; }
    public java.util.List<String> getFlyWorlds() { return flyWorlds; }
    public String getFlyRegionMode() { return flyRegionMode; }
    public java.util.List<String> getFlyRegions() { return flyRegions; }
    public int getFlyFallProtectionSeconds() { return flyFallProtectionSeconds; }

    // AFK Settings
    private boolean afkEnabled = true;
    private boolean afkAutoUnafkOnMove = true;
    private boolean afkAutoUnafkOnChat = true;

    public boolean isAfkEnabled() { return afkEnabled; }
    public boolean isAfkAutoUnafkOnMove() { return afkAutoUnafkOnMove; }
    public boolean isAfkAutoUnafkOnChat() { return afkAutoUnafkOnChat; }

    // Join & Leave Messages
    private boolean joinLeaveEnabled = true;
    private String defaultJoinMessage = "<dark_gray>[<green>+</green>]</dark_gray> <white><player></white> <gray>joined the game.</gray>";
    private String defaultLeaveMessage = "<dark_gray>[<red>-</red>]</dark_gray> <white><player></white> <gray>left the game.</gray>";
    private final java.util.List<VipJoinRule> vipJoinRules = new java.util.ArrayList<>();

    public record VipJoinRule(String key, String permission, String message) {}

    public boolean isJoinLeaveEnabled() { return joinLeaveEnabled; }
    public String getDefaultJoinMessage() { return defaultJoinMessage; }
    public String getDefaultLeaveMessage() { return defaultLeaveMessage; }
    public java.util.List<VipJoinRule> getVipJoinRules() { return vipJoinRules; }

    // Settings Commands
    private String cmdPvp = "pvp";
    private String cmdScoreboard = "sb toggle";

    public String getCmdPvp() { return cmdPvp; }
    public String getCmdScoreboard() { return cmdScoreboard; }

    // Spawn Settings
    private boolean spawnEnabled = true;
    private boolean spawnRespawnAtSpawn = true;
    private boolean spawnRespawnOverrideBed = false;
    private boolean spawnTeleportOnFirstJoin = true;
    private boolean spawnTeleportOnJoin = false;

    public boolean isSpawnEnabled() { return spawnEnabled; }
    public boolean isSpawnRespawnAtSpawn() { return spawnRespawnAtSpawn; }
    public boolean isSpawnRespawnOverrideBed() { return spawnRespawnOverrideBed; }
    public boolean isSpawnTeleportOnFirstJoin() { return spawnTeleportOnFirstJoin; }
    public boolean isSpawnTeleportOnJoin() { return spawnTeleportOnJoin; }

    // Mail Settings
    private boolean mailEnabled = true;
    private boolean mailNotifyOnJoin = true;

    public boolean isMailEnabled() { return mailEnabled; }
    public boolean isMailNotifyOnJoin() { return mailNotifyOnJoin; }

    // JoinDialog & Changelog Settings
    private boolean joinDialogEnabled = true;
    private String joinDialogUpdateId = "v1.0.0";
    private boolean joinDialogDailyReminder = true;
    private boolean firstJoinTipsEnabled = true;
    private String firstJoinTipsTitle = "<gradient:#00fbff:#0077ff><bold>Welcome to Clarity Network!</bold></gradient>";
    private java.util.List<String> firstJoinTipsBody = new java.util.ArrayList<>();
    private String changelogTitle = "<gradient:#ffaa00:#ff5555><bold>Server Updates & Changelog</bold></gradient>";
    private java.util.List<String> changelogBody = new java.util.ArrayList<>();

    public boolean isJoinDialogEnabled() { return joinDialogEnabled; }
    public String getJoinDialogUpdateId() { return joinDialogUpdateId; }
    public boolean isJoinDialogDailyReminder() { return joinDialogDailyReminder; }
    public boolean isFirstJoinTipsEnabled() { return firstJoinTipsEnabled; }
    public String getFirstJoinTipsTitle() { return firstJoinTipsTitle; }
    public java.util.List<String> getFirstJoinTipsBody() { return firstJoinTipsBody; }
    public String getChangelogTitle() { return changelogTitle; }
    public java.util.List<String> getChangelogBody() { return changelogBody; }
}
