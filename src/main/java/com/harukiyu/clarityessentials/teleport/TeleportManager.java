package com.harukiyu.clarityessentials.teleport;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.data.DataManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.settings.UserSettings;
import com.harukiyu.clarityessentials.settings.UserSettingsManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager {

    private final Plugin plugin;
    private final ConfigManager configManager;
    private final DataManager dataManager;
    private final UserSettingsManager userSettingsManager;

    // Target UUID -> TeleportRequest
    private final Map<UUID, TeleportRequest> pendingRequests = new ConcurrentHashMap<>();

    // Player UUID -> Warmup Task
    private final Map<UUID, BukkitTask> activeWarmups = new ConcurrentHashMap<>();

    public TeleportManager(@NotNull Plugin plugin,
                           @NotNull ConfigManager configManager,
                           @NotNull DataManager dataManager,
                           @NotNull UserSettingsManager userSettingsManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.dataManager = dataManager;
        this.userSettingsManager = userSettingsManager;
    }

    /**
     * Sends a teleport request from sender to target.
     */
    public void sendRequest(@NotNull Player sender, @NotNull Player target, @NotNull TeleportRequest.Type type) {
        if (sender.getUniqueId().equals(target.getUniqueId())) {
            LanguageHook.sendMessage(sender, "teleport.cannot_tpa_self");
            playSound(sender, configManager.getSoundActionFailure());
            return;
        }

        // Check if target has disabled incoming TPA requests
        if (!dataManager.isTpaEnabled(target.getUniqueId(), configManager.isTpaAllowToggleDefault())) {
            LanguageHook.sendMessage(sender, "teleport.target_disabled_tpa",
                    Placeholder.unparsed("target", target.getName())
            );
            playSound(sender, configManager.getSoundActionFailure());
            return;
        }

        // Check target's Privacy Settings
        UserSettings targetSettings = userSettingsManager.getSettings(target.getUniqueId());
        if (type == TeleportRequest.Type.NORMAL) {
            String pref = targetSettings.getTeleportRequests();
            if ("Off".equalsIgnoreCase(pref)) {
                LanguageHook.sendMessage(sender, "teleport.target_disabled_tpa",
                        Placeholder.unparsed("target", target.getName())
                );
                playSound(sender, configManager.getSoundActionFailure());
                return;
            }
            if ("Friends".equalsIgnoreCase(pref) && !targetSettings.isFriend(sender.getName())) {
                LanguageHook.sendMessage(sender, "teleport.target_friends_only_tpa",
                        Placeholder.unparsed("target", target.getName())
                );
                playSound(sender, configManager.getSoundActionFailure());
                return;
            }
        } else {
            String pref = targetSettings.getTeleportHereRequests();
            if ("Off".equalsIgnoreCase(pref)) {
                LanguageHook.sendMessage(sender, "teleport.target_disabled_tpahere",
                        Placeholder.unparsed("target", target.getName())
                );
                playSound(sender, configManager.getSoundActionFailure());
                return;
            }
            if ("Friends".equalsIgnoreCase(pref) && !targetSettings.isFriend(sender.getName())) {
                LanguageHook.sendMessage(sender, "teleport.target_friends_only_tpahere",
                        Placeholder.unparsed("target", target.getName())
                );
                playSound(sender, configManager.getSoundActionFailure());
                return;
            }
        }

        // Check if a request is already pending to this target from this sender
        TeleportRequest existing = pendingRequests.get(target.getUniqueId());
        if (existing != null && existing.getSenderId().equals(sender.getUniqueId())) {
            LanguageHook.sendMessage(sender, "teleport.already_sent");
            playSound(sender, configManager.getSoundActionFailure());
            return;
        }

        // Create new request
        TeleportRequest request = new TeleportRequest(sender.getUniqueId(), target.getUniqueId(), type);

        // Schedule expiration
        int timeoutSeconds = configManager.getTpaTimeoutSeconds();
        BukkitTask expireTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            TeleportRequest req = pendingRequests.remove(target.getUniqueId());
            if (req != null && req.equals(request)) {
                Player s = Bukkit.getPlayer(req.getSenderId());
                Player t = Bukkit.getPlayer(req.getTargetId());
                if (s != null && s.isOnline()) {
                    LanguageHook.sendMessage(s, "teleport.tpa_expired_sender", Placeholder.unparsed("target", target.getName()));
                }
                if (t != null && t.isOnline()) {
                    LanguageHook.sendMessage(t, "teleport.tpa_expired_target", Placeholder.unparsed("sender", sender.getName()));
                }
            }
        }, timeoutSeconds * 20L);

        request.setExpireTask(expireTask);

        // Store request
        TeleportRequest oldReq = pendingRequests.put(target.getUniqueId(), request);
        if (oldReq != null) {
            BukkitTask oldTask = oldReq.getExpireTask();
            if (oldTask != null) {
                oldTask.cancel();
            }
        }

        // Notify sender & target
        if (type == TeleportRequest.Type.NORMAL) {
            LanguageHook.sendMessage(sender, "teleport.tpa_sent", Placeholder.unparsed("target", target.getName()));
            LanguageHook.sendMessage(target, "teleport.tpa_received", Placeholder.unparsed("sender", sender.getName()));
        } else {
            LanguageHook.sendMessage(sender, "teleport.tpahere_sent", Placeholder.unparsed("target", target.getName()));
            LanguageHook.sendMessage(target, "teleport.tpahere_received", Placeholder.unparsed("sender", sender.getName()));
        }

        // Send interactive clickable buttons to target
        LanguageHook.sendMessage(target, "teleport.tpa_click_accept");
        LanguageHook.sendMessage(target, "teleport.tpa_click_deny");

        playSound(sender, configManager.getSoundDialogOpen());
        if (targetSettings.isTeleportAlerts()) {
            playSound(target, configManager.getSoundDialogOpen());
        }
    }

    /**
     * Accepts a pending teleport request.
     */
    public void acceptRequest(@NotNull Player target, @Nullable Player specificSender) {
        TeleportRequest request = pendingRequests.get(target.getUniqueId());
        if (request == null) {
            LanguageHook.sendMessage(target, "teleport.no_pending");
            playSound(target, configManager.getSoundActionFailure());
            return;
        }

        if (specificSender != null && !request.getSenderId().equals(specificSender.getUniqueId())) {
            LanguageHook.sendMessage(target, "teleport.no_pending");
            playSound(target, configManager.getSoundActionFailure());
            return;
        }

        Player sender = Bukkit.getPlayer(request.getSenderId());
        if (sender == null || !sender.isOnline()) {
            pendingRequests.remove(target.getUniqueId());
            BukkitTask task = request.getExpireTask();
            if (task != null) {
                task.cancel();
            }
            LanguageHook.sendMessage(target, "general.player_not_found", Placeholder.unparsed("player", "Sender"));
            playSound(target, configManager.getSoundActionFailure());
            return;
        }

        // Cancel expiration task and remove
        BukkitTask task = request.getExpireTask();
        if (task != null) {
            task.cancel();
        }
        pendingRequests.remove(target.getUniqueId());

        LanguageHook.sendMessage(target, "teleport.tpa_accepted_target", Placeholder.unparsed("sender", sender.getName()));
        LanguageHook.sendMessage(sender, "teleport.tpa_accepted_sender", Placeholder.unparsed("target", target.getName()));

        playSound(target, configManager.getSoundActionSuccess());
        playSound(sender, configManager.getSoundActionSuccess());

        // Perform teleportation based on request type
        if (request.getType() == TeleportRequest.Type.NORMAL) {
            // Sender teleports to Target
            teleportWithWarmup(sender, target.getLocation());
        } else {
            // Target teleports to Sender
            teleportWithWarmup(target, sender.getLocation());
        }
    }

    /**
     * Denies a pending teleport request.
     */
    public void denyRequest(@NotNull Player target, @Nullable Player specificSender) {
        TeleportRequest request = pendingRequests.get(target.getUniqueId());
        if (request == null) {
            LanguageHook.sendMessage(target, "teleport.no_pending");
            playSound(target, configManager.getSoundActionFailure());
            return;
        }

        if (specificSender != null && !request.getSenderId().equals(specificSender.getUniqueId())) {
            LanguageHook.sendMessage(target, "teleport.no_pending");
            playSound(target, configManager.getSoundActionFailure());
            return;
        }

        Player sender = Bukkit.getPlayer(request.getSenderId());

        // Cancel expiration task and remove
        BukkitTask task = request.getExpireTask();
        if (task != null) {
            task.cancel();
        }
        pendingRequests.remove(target.getUniqueId());

        LanguageHook.sendMessage(target, "teleport.tpa_denied_target", Placeholder.unparsed("sender", sender != null ? sender.getName() : "Player"));
        if (sender != null && sender.isOnline()) {
            LanguageHook.sendMessage(sender, "teleport.tpa_denied_sender", Placeholder.unparsed("target", target.getName()));
            playSound(sender, configManager.getSoundTeleportCancel());
        }
        playSound(target, configManager.getSoundDialogOpen());
    }

    /**
     * Toggles incoming teleport requests for a player.
     * Returns the new status (true = enabled, false = disabled).
     */
    public boolean toggleTpa(@NotNull Player player) {
        boolean current = dataManager.isTpaEnabled(player.getUniqueId(), configManager.isTpaAllowToggleDefault());
        boolean newState = !current;
        dataManager.setTpaEnabled(player.getUniqueId(), newState);

        if (newState) {
            LanguageHook.sendMessage(player, "teleport.tptoggle_on");
            playSound(player, configManager.getSoundActionSuccess());
        } else {
            LanguageHook.sendMessage(player, "teleport.tptoggle_off");
            playSound(player, configManager.getSoundActionFailure());
        }
        return newState;
    }

    public boolean isTpaEnabled(@NotNull Player player) {
        return dataManager.isTpaEnabled(player.getUniqueId(), configManager.isTpaAllowToggleDefault());
    }

    /**
     * Teleports a player to a destination with warmup and movement cancellation check.
     */
    public void teleportWithWarmup(@NotNull Player player, @NotNull Location destination) {
        int warmup = configManager.getTpaWarmupSeconds();
        boolean bypass = player.hasPermission(configManager.getTpaBypassWarmupPermission());

        if (warmup <= 0 || bypass) {
            // Instant teleport
            player.teleportAsync(destination).thenAccept(success -> {
                if (success) {
                    LanguageHook.sendMessage(player, "teleport.teleported_success");
                    playSound(player, configManager.getSoundTeleportSuccess());
                }
            });
            return;
        }

        // Cancel existing warmup if active
        cancelWarmup(player);

        Location startLoc = player.getLocation().clone();
        LanguageHook.sendMessage(player, "teleport.warmup_countdown", Placeholder.unparsed("seconds", String.valueOf(warmup)));
        playSound(player, configManager.getSoundTeleportWarmup());

        BukkitTask warmupTask = new BukkitRunnable() {
            int remaining = warmup;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelWarmup(player);
                    cancel();
                    return;
                }

                // Movement check
                if (configManager.isTpaCancelOnMove()) {
                    Location currentLoc = player.getLocation();
                    if (!startLoc.getWorld().equals(currentLoc.getWorld()) ||
                            startLoc.distanceSquared(currentLoc) > configManager.getTpaMoveToleranceSquared()) {
                        LanguageHook.sendMessage(player, "teleport.warmup_cancelled");
                        playSound(player, configManager.getSoundTeleportCancel());
                        cancelWarmup(player);
                        cancel();
                        return;
                    }
                }

                remaining--;
                if (remaining > 0) {
                    playSound(player, configManager.getSoundTeleportWarmup());
                } else {
                    // Teleport execution
                    player.teleportAsync(destination).thenAccept(success -> {
                        if (success) {
                            LanguageHook.sendMessage(player, "teleport.teleported_success");
                            playSound(player, configManager.getSoundTeleportSuccess());
                        }
                    });
                    cancelWarmup(player);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);

        activeWarmups.put(player.getUniqueId(), warmupTask);
    }

    public void cancelWarmup(@NotNull Player player) {
        BukkitTask task = activeWarmups.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    public void handleQuit(@NotNull Player player) {
        cancelWarmup(player);
        TeleportRequest request = pendingRequests.remove(player.getUniqueId());
        if (request != null) {
            BukkitTask task = request.getExpireTask();
            if (task != null) {
                task.cancel();
            }
        }
    }

    @Nullable
    public Player getPendingSender(@NotNull UUID targetId) {
        TeleportRequest req = pendingRequests.get(targetId);
        if (req != null) {
            return Bukkit.getPlayer(req.getSenderId());
        }
        return null;
    }

    public void sendTpa(@NotNull Player sender, @NotNull Player target) {
        sendRequest(sender, target, TeleportRequest.Type.NORMAL);
    }

    public void sendTpaHere(@NotNull Player sender, @NotNull Player target) {
        sendRequest(sender, target, TeleportRequest.Type.HERE);
    }

    public void setTpaEnabled(@NotNull Player player, boolean enabled) {
        dataManager.setTpaEnabled(player.getUniqueId(), enabled);
    }

    private void playSound(@NotNull Player player, @Nullable org.bukkit.Sound sound) {
        if (configManager.isSoundsEnabled() && sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }
}
