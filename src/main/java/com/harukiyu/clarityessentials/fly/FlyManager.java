package com.harukiyu.clarityessentials.fly;

import com.harukiyu.clarityessentials.ClarityEssentials;
import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.hook.WorldGuardHook;
import com.harukiyu.clarityessentials.utils.Utils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FlyManager implements Listener {

    private final ClarityEssentials plugin;
    private final ConfigManager configManager;

    private final Map<UUID, Long> activeSessions = new ConcurrentHashMap<>();
    private final Set<UUID> unlimitedFlyers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> fallProtection = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> fallProtectionExpiry = new ConcurrentHashMap<>();

    private BukkitTask timerTask;

    public FlyManager(@NotNull ClarityEssentials plugin, @NotNull ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        startTimer();
    }

    private void startTimer() {
        this.timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();

            // 1. Clean expired fall protection
            fallProtectionExpiry.entrySet().removeIf(entry -> {
                if (now > entry.getValue()) {
                    fallProtection.remove(entry.getKey());
                    return true;
                }
                return false;
            });

            // 2. Check active flight sessions
            for (Map.Entry<UUID, Long> entry : activeSessions.entrySet()) {
                UUID uuid = entry.getKey();
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) {
                    continue;
                }

                long expiry = entry.getValue();
                long remainingSec = (expiry - now + 999L) / 1000L;

                if (remainingSec <= 0) {
                    activeSessions.remove(uuid);
                    revokeFlight(player, "fly.expired");
                } else if (remainingSec == 60 || remainingSec == 30 || remainingSec == 15
                        || remainingSec == 10 || remainingSec == 5 || remainingSec == 3
                        || remainingSec == 2 || remainingSec == 1) {
                    LanguageHook.sendMessage(player, "fly.expiring_soon", Placeholder.unparsed("time", String.valueOf(remainingSec)));
                    if (configManager.isSoundsEnabled()) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.5f);
                    }
                }
            }
        }, 20L, 20L);
    }

    public void stop() {
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel();
        }
    }

    public boolean isFlying(@NotNull Player player) {
        return player.getAllowFlight();
    }

    public boolean isWorldAllowed(@Nullable World world) {
        if (world == null) return true;
        String mode = configManager.getFlyWorldMode();
        if ("DISABLED".equalsIgnoreCase(mode)) return true;

        List<String> worlds = configManager.getFlyWorlds();
        String worldName = world.getName().toLowerCase();

        if ("WHITELIST".equalsIgnoreCase(mode)) {
            return worlds.contains(worldName);
        } else {
            // Default: BLACKLIST
            return !worlds.contains(worldName);
        }
    }

    public boolean isRegionAllowed(@NotNull Location location) {
        String mode = configManager.getFlyRegionMode();
        if ("DISABLED".equalsIgnoreCase(mode) || !WorldGuardHook.isAvailable()) {
            return true;
        }

        List<String> regions = configManager.getFlyRegions();
        Set<String> currentRegions = WorldGuardHook.getRegionsAt(location);

        if ("WHITELIST".equalsIgnoreCase(mode)) {
            if (currentRegions.isEmpty()) return false;
            return !Collections.disjoint(currentRegions, regions);
        } else {
            // Default: BLACKLIST
            for (String r : currentRegions) {
                if (regions.contains(r)) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean canFlyAt(@NotNull Player player, @NotNull Location location) {
        if (player.hasPermission("clarityessentials.fly.bypass")) {
            return true;
        }
        if (!isWorldAllowed(location.getWorld())) {
            return false;
        }
        return isRegionAllowed(location);
    }

    public boolean setFly(@NotNull Player target, boolean enable, long durationSeconds, @Nullable CommandSender initiator) {
        if (enable) {
            if (!canFlyAt(target, target.getLocation())) {
                if (!isWorldAllowed(target.getWorld())) {
                    LanguageHook.sendMessage(target, "fly.restricted_world");
                    if (initiator != null && !initiator.equals(target)) {
                        LanguageHook.sendMessage(initiator, "fly.restricted_world_target", Placeholder.unparsed("target", target.getName()));
                    }
                } else {
                    LanguageHook.sendMessage(target, "fly.restricted_region");
                    if (initiator != null && !initiator.equals(target)) {
                        LanguageHook.sendMessage(initiator, "fly.restricted_region_target", Placeholder.unparsed("target", target.getName()));
                    }
                }
                return false;
            }

            target.setAllowFlight(true);
            target.setFlying(true);

            if (durationSeconds > 0) {
                activeSessions.put(target.getUniqueId(), System.currentTimeMillis() + (durationSeconds * 1000L));
                unlimitedFlyers.remove(target.getUniqueId());
                String formatted = Utils.formatDuration(durationSeconds);
                if (initiator == null || initiator.equals(target)) {
                    LanguageHook.sendMessage(target, "fly.enabled_timed", Placeholder.unparsed("duration", formatted));
                } else {
                    LanguageHook.sendMessage(target, "fly.enabled_timed_by",
                            Placeholder.unparsed("sender", initiator.getName()),
                            Placeholder.unparsed("duration", formatted));
                    LanguageHook.sendMessage(initiator, "fly.enabled_timed_target",
                            Placeholder.unparsed("target", target.getName()),
                            Placeholder.unparsed("duration", formatted));
                }
            } else {
                activeSessions.remove(target.getUniqueId());
                unlimitedFlyers.add(target.getUniqueId());
                if (initiator == null || initiator.equals(target)) {
                    LanguageHook.sendMessage(target, "fly.enabled");
                } else {
                    LanguageHook.sendMessage(target, "fly.enabled_by", Placeholder.unparsed("sender", initiator.getName()));
                    LanguageHook.sendMessage(initiator, "fly.enabled_target", Placeholder.unparsed("target", target.getName()));
                }
            }

            if (configManager.isSoundsEnabled()) {
                target.playSound(target.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
            }
            return true;
        } else {
            target.setFlying(false);
            target.setAllowFlight(false);
            activeSessions.remove(target.getUniqueId());
            unlimitedFlyers.remove(target.getUniqueId());
            grantFallProtection(target);

            if (initiator == null || initiator.equals(target)) {
                LanguageHook.sendMessage(target, "fly.disabled");
            } else {
                LanguageHook.sendMessage(target, "fly.disabled_by", Placeholder.unparsed("sender", initiator.getName()));
                LanguageHook.sendMessage(initiator, "fly.disabled_target", Placeholder.unparsed("target", target.getName()));
            }

            if (configManager.isSoundsEnabled()) {
                target.playSound(target.getLocation(), Sound.ENTITY_BAT_DEATH, 0.8f, 1.0f);
            }
            return true;
        }
    }

    public void revokeFlight(@NotNull Player player, @NotNull String reasonLanguageKey) {
        player.setFlying(false);
        player.setAllowFlight(false);
        activeSessions.remove(player.getUniqueId());
        unlimitedFlyers.remove(player.getUniqueId());
        grantFallProtection(player);

        LanguageHook.sendMessage(player, reasonLanguageKey);
        if (configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
        }
    }

    public void grantFallProtection(@NotNull Player player) {
        fallProtection.add(player.getUniqueId());
        long expiry = System.currentTimeMillis() + (configManager.getFlyFallProtectionSeconds() * 1000L);
        fallProtectionExpiry.put(player.getUniqueId(), expiry);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.isFlying() || player.hasPermission("clarityessentials.fly.bypass")) {
            return;
        }

        if (!isWorldAllowed(to.getWorld())) {
            revokeFlight(player, "fly.restricted_world_entered");
        } else if (!isRegionAllowed(to)) {
            revokeFlight(player, "fly.restricted_region_entered");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();
        Player player = event.getPlayer();
        if (!player.isFlying() || player.hasPermission("clarityessentials.fly.bypass")) {
            return;
        }

        if (!isWorldAllowed(to.getWorld())) {
            revokeFlight(player, "fly.restricted_world_entered");
        } else if (!isRegionAllowed(to)) {
            revokeFlight(player, "fly.restricted_region_entered");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!player.isFlying() || player.hasPermission("clarityessentials.fly.bypass")) {
            return;
        }

        if (!isWorldAllowed(player.getWorld())) {
            revokeFlight(player, "fly.restricted_world_entered");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (fallProtection.contains(player.getUniqueId())) {
            event.setCancelled(true);
            fallProtection.remove(player.getUniqueId());
            fallProtectionExpiry.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        fallProtection.remove(uuid);
        fallProtectionExpiry.remove(uuid);
    }
}
