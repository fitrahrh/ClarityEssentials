package com.harukiyu.clarityessentials;

import com.harukiyu.clarityessentials.chat.ChatListener;
import com.harukiyu.clarityessentials.chat.ChatManager;
import com.harukiyu.clarityessentials.commands.*;
import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.data.DataManager;
import com.harukiyu.clarityessentials.dialog.*;
import com.harukiyu.clarityessentials.home.HomeManager;
import com.harukiyu.clarityessentials.afk.AfkManager;
import com.harukiyu.clarityessentials.fly.FlyManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.hook.LuckPermsHook;
import com.harukiyu.clarityessentials.hook.PlaceholderAPIHook;
import com.harukiyu.clarityessentials.hook.VaultHook;
import com.harukiyu.clarityessentials.hook.WorldGuardHook;
import com.harukiyu.clarityessentials.leaderboard.LeaderboardManager;
import com.harukiyu.clarityessentials.listeners.PlayerListener;
import com.harukiyu.clarityessentials.nickname.NicknameManager;
import com.harukiyu.clarityessentials.settings.UserSettingsManager;
import com.harukiyu.clarityessentials.teleport.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class ClarityEssentials extends JavaPlugin {

    private static ClarityEssentials instance;

    private ConfigManager configManager;
    private DataManager dataManager;
    private UserSettingsManager userSettingsManager;
    private LeaderboardManager leaderboardManager;
    private NicknameManager nicknameManager;
    private TeleportManager teleportManager;
    private HomeManager homeManager;
    private HomeDialogManager homeDialogManager;
    private PayDialogManager payDialogManager;
    private TeleportDialogManager teleportDialogManager;
    private LeaderboardDialogManager leaderboardDialogManager;
    private SettingsDialogManager settingsDialogManager;
    private FriendsDialogManager friendsDialogManager;
    private MenuDialogManager menuDialogManager;
    private ChatManager chatManager;
    private com.harukiyu.clarityessentials.message.PrivateMessageManager privateMessageManager;
    private FlyManager flyManager;
    private AfkManager afkManager;
    private com.harukiyu.clarityessentials.spawn.SpawnManager spawnManager;
    private com.harukiyu.clarityessentials.mail.MailManager mailManager;
    private com.harukiyu.clarityessentials.dialog.UpdateDialogManager updateDialogManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Initialize Hooks
        LanguageHook.init(this);
        LuckPermsHook.init();
        PlaceholderAPIHook.init(this);
        VaultHook.init();
        WorldGuardHook.init();

        // 2. Initialize Managers
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);
        this.dataManager.load();
        this.userSettingsManager = new UserSettingsManager(this);
        this.leaderboardManager = new LeaderboardManager(this);

        this.nicknameManager = new NicknameManager(configManager, dataManager);
        this.teleportManager = new TeleportManager(this, configManager, dataManager, userSettingsManager);
        this.homeManager = new HomeManager(configManager, dataManager, teleportManager);
        this.homeDialogManager = new HomeDialogManager(configManager, homeManager);

        this.payDialogManager = new PayDialogManager(this, configManager, userSettingsManager);
        this.teleportDialogManager = new TeleportDialogManager(configManager, userSettingsManager, teleportManager);
        this.leaderboardDialogManager = new LeaderboardDialogManager(leaderboardManager);
        this.settingsDialogManager = new SettingsDialogManager(configManager, userSettingsManager, teleportManager);
        this.friendsDialogManager = new FriendsDialogManager(userSettingsManager, teleportManager, payDialogManager);

        this.menuDialogManager = new MenuDialogManager(
                configManager,
                homeDialogManager,
                payDialogManager,
                teleportDialogManager,
                leaderboardDialogManager,
                settingsDialogManager,
                friendsDialogManager,
                nicknameManager
        );
        this.menuDialogManager.wireSubManagers();

        this.chatManager = new ChatManager(configManager, nicknameManager);
        this.privateMessageManager = new com.harukiyu.clarityessentials.message.PrivateMessageManager(this, userSettingsManager, nicknameManager);
        this.flyManager = new FlyManager(this, configManager);
        this.afkManager = new AfkManager(this, configManager, nicknameManager);
        this.spawnManager = new com.harukiyu.clarityessentials.spawn.SpawnManager(this);
        this.mailManager = new com.harukiyu.clarityessentials.mail.MailManager(this, configManager);
        this.updateDialogManager = new com.harukiyu.clarityessentials.dialog.UpdateDialogManager(this, configManager, userSettingsManager);

        // 3. Register Events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(configManager, nicknameManager, teleportManager, userSettingsManager, spawnManager, mailManager, updateDialogManager), this);
        pm.registerEvents(new ChatListener(chatManager, userSettingsManager), this);
        pm.registerEvents(flyManager, this);
        pm.registerEvents(afkManager, this);

        // 4. Clean up legacy pause menu datapack if present
        cleanupLegacyDatapack();

        // 5. Register Paper Native Commands (BasicCommand)
        registerCommand("nick", "Change or reset your nickname", List.of("nickname"), new NicknameCommand(configManager, nicknameManager, menuDialogManager));
        registerCommand("realname", "Lookup a player's real username from nickname", List.of(), new RealnameCommand(nicknameManager));
        registerCommand("tpa", "Request to teleport to a player", List.of(), new TpaCommand(teleportManager));
        registerCommand("tpahere", "Request a player to teleport to your location", List.of(), new TpaHereCommand(teleportManager));
        registerCommand("tpaccept", "Accept a teleport request", List.of("tpyes"), new TpAcceptCommand(teleportManager));
        registerCommand("tpdeny", "Decline a teleport request", List.of("tpno"), new TpDenyCommand(teleportManager));
        registerCommand("tptoggle", "Toggle incoming teleport requests", List.of(), new TpToggleCommand(teleportManager));
        registerCommand("home", "Open Home Dialog or teleport to a home", List.of("homes"), new HomeCommand(homeManager, homeDialogManager));
        registerCommand("sethome", "Set a home at your current location", List.of(), new SetHomeCommand(homeManager, homeDialogManager));
        registerCommand("delhome", "Delete a saved home", List.of("deletehome", "remhome"), new DelHomeCommand(homeManager, homeDialogManager));
        registerCommand("homelimit", "Check or manage player home limit quota", List.of("hl"), new HomeLimitCommand(homeManager));
        registerCommand("msg", "Send a private message to a player", List.of("tell", "w", "whisper", "message"), new MsgCommand(privateMessageManager));
        registerCommand("reply", "Reply to the last messaged player", List.of("r"), new ReplyCommand(privateMessageManager));
        registerCommand("socialspy", "Toggle admin social spy for private messages", List.of("spy"), new SocialSpyCommand(privateMessageManager));
        registerCommand("menu", "Open Centralized Player Dashboard", List.of("dashboard", "cp"), new MenuCommand(menuDialogManager));
        registerCommand("clarityessentials", "ClarityEssentials main command", List.of("ce", "essentials"), new ClarityEssentialsCommand(this));

        // Essentials Commands
        registerCommand("feed", "Replenish hunger bar", List.of("eat"), new FeedCommand(configManager));
        registerCommand("heal", "Heal health and status effects", List.of(), new HealCommand(configManager));
        registerCommand("fly", "Toggle or set flight duration", List.of("flight"), new FlyCommand(flyManager, configManager));
        registerCommand("broadcast", "Broadcast a global announcement", List.of("bc", "bcast"), new BroadcastCommand(configManager));
        registerCommand("afk", "Toggle away-from-keyboard status", List.of("away"), new AfkCommand(afkManager));
        registerCommand("gamemode", "Change player game mode", List.of("gm"), new GamemodeCommand(configManager));
        registerCommand("gmc", "Switch game mode to creative", List.of(), new GamemodeAliasCommand(GameMode.CREATIVE, "clarityessentials.command.gamemode.creative"));
        registerCommand("gms", "Switch game mode to survival", List.of(), new GamemodeAliasCommand(GameMode.SURVIVAL, "clarityessentials.command.gamemode.survival"));
        registerCommand("gmsp", "Switch game mode to spectator", List.of(), new GamemodeAliasCommand(GameMode.SPECTATOR, "clarityessentials.command.gamemode.spectator"));
        registerCommand("gma", "Switch game mode to adventure", List.of(), new GamemodeAliasCommand(GameMode.ADVENTURE, "clarityessentials.command.gamemode.adventure"));
        registerCommand("speed", "Change walking or flight speed", List.of(), new SpeedCommand(configManager));

        // Spawn, Mail, and Update Commands
        registerCommand("setspawn", "Set server spawn location", List.of(), new SetSpawnCommand(spawnManager, configManager));
        registerCommand("spawn", "Teleport to server spawn", List.of(), new SpawnCommand(spawnManager, teleportManager, configManager));
        registerCommand("mail", "Send and read offline player mails", List.of("email"), new MailCommand(mailManager, configManager));
        registerCommand("update", "View server updates and changelog", List.of("changelog", "news"), new UpdateCommand(updateDialogManager, configManager));

        getLogger().info("==================================================");
        getLogger().info(() -> " 🌟 ClarityEssentials v" + getPluginMeta().getVersion() + " has been enabled!");
        getLogger().info(() -> " Paper Native Dialog API: Active");
        getLogger().info(() -> " ClarityLangCore Integration: " + (LanguageHook.isCoreAvailable() ? "Connected" : "Fallback Standalone"));
        getLogger().info(() -> " LuckPerms Integration: " + (LuckPermsHook.isAvailable() ? "Active" : "Disabled"));
        getLogger().info(() -> " PlaceholderAPI Integration: " + (PlaceholderAPIHook.isAvailable() ? "Active" : "Disabled"));
        getLogger().info(() -> " WorldGuard Integration: " + (WorldGuardHook.isAvailable() ? "Active" : "Disabled"));
        getLogger().info("==================================================");
    }

    @Override
    public void onDisable() {
        if (flyManager != null) {
            flyManager.stop();
        }
        if (mailManager != null) {
            mailManager.saveSync();
        }
        if (spawnManager != null) {
            spawnManager.save();
        }
        if (dataManager != null) {
            dataManager.saveSync();
        }
        if (userSettingsManager != null) {
            userSettingsManager.saveAll();
        }
        getLogger().info(() -> getName() + " has been safely disabled!");
    }

    public void reloadPlugin() {
        configManager.load();
        dataManager.load();
        if (spawnManager != null) {
            spawnManager.load();
        }
        if (mailManager != null) {
            mailManager.load();
        }
        LanguageHook.init(this);
        LuckPermsHook.init();
        PlaceholderAPIHook.init(this);
        VaultHook.init();
        WorldGuardHook.init();
    }

    private void cleanupLegacyDatapack() {
        try {
            File worldDir = Bukkit.getWorldContainer();
            File defaultWorld = new File(worldDir, "world");
            if (!defaultWorld.exists() && !getServer().getWorlds().isEmpty()) {
                defaultWorld = getServer().getWorlds().getFirst().getWorldFolder();
            }
            File datapacksDir = new File(defaultWorld, "datapacks/ClarityEssentials");
            if (datapacksDir.exists()) {
                deleteDirectoryRecursively(datapacksDir);
                getLogger().info(() -> "Cleaned up legacy pause menu datapack from: " + datapacksDir.getAbsolutePath());
            }
        } catch (Throwable e) {
            getLogger().warning(() -> "Failed to clean up legacy datapack: " + e.getMessage());
        }
    }

    private void deleteDirectoryRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectoryRecursively(child);
                }
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    public static ClarityEssentials getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public UserSettingsManager getUserSettingsManager() {
        return userSettingsManager;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public NicknameManager getNicknameManager() {
        return nicknameManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public HomeDialogManager getHomeDialogManager() {
        return homeDialogManager;
    }

    public PayDialogManager getPayDialogManager() {
        return payDialogManager;
    }

    public TeleportDialogManager getTeleportDialogManager() {
        return teleportDialogManager;
    }

    public LeaderboardDialogManager getLeaderboardDialogManager() {
        return leaderboardDialogManager;
    }

    public SettingsDialogManager getSettingsDialogManager() {
        return settingsDialogManager;
    }

    public FriendsDialogManager getFriendsDialogManager() {
        return friendsDialogManager;
    }

    public MenuDialogManager getMenuDialogManager() {
        return menuDialogManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public com.harukiyu.clarityessentials.message.PrivateMessageManager getPrivateMessageManager() {
        return privateMessageManager;
    }

    public FlyManager getFlyManager() {
        return flyManager;
    }

    public AfkManager getAfkManager() {
        return afkManager;
    }

    public com.harukiyu.clarityessentials.spawn.SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public com.harukiyu.clarityessentials.mail.MailManager getMailManager() {
        return mailManager;
    }

    public com.harukiyu.clarityessentials.dialog.UpdateDialogManager getUpdateDialogManager() {
        return updateDialogManager;
    }
}
