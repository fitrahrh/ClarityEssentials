package com.harukiyu.clarityessentials.afk;

import com.harukiyu.clarityessentials.ClarityEssentials;
import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.nickname.NicknameManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AfkManager implements Listener {

    public record AfkData(long timestamp, @Nullable String reason, Location location) {}

    private final ClarityEssentials plugin;
    private final ConfigManager configManager;
    private final NicknameManager nicknameManager;

    private final Map<UUID, AfkData> afkPlayers = new ConcurrentHashMap<>();

    public AfkManager(@NotNull ClarityEssentials plugin,
                      @NotNull ConfigManager configManager,
                      @NotNull NicknameManager nicknameManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.nicknameManager = nicknameManager;
    }

    public boolean isAfk(@NotNull Player player) {
        return afkPlayers.containsKey(player.getUniqueId());
    }

    public boolean isAfk(@NotNull UUID uuid) {
        return afkPlayers.containsKey(uuid);
    }

    @Nullable
    public String getReason(@NotNull UUID uuid) {
        AfkData data = afkPlayers.get(uuid);
        return data != null ? data.reason() : null;
    }

    public void toggleAfk(@NotNull Player player, @Nullable String reason) {
        if (isAfk(player)) {
            setAfk(player, false, null);
        } else {
            setAfk(player, true, reason);
        }
    }

    public void setAfk(@NotNull Player player, boolean afk, @Nullable String reason) {
        UUID uuid = player.getUniqueId();
        if (afk) {
            afkPlayers.put(uuid, new AfkData(System.currentTimeMillis(), reason, player.getLocation().clone()));

            if (reason != null && !reason.isBlank()) {
                LanguageHook.broadcastMessage("afk.now_afk_reason",
                        Placeholder.component("player", nicknameManager.getDisplayName(player)),
                        Placeholder.unparsed("reason", reason));
            } else {
                LanguageHook.broadcastMessage("afk.now_afk",
                        Placeholder.component("player", nicknameManager.getDisplayName(player)));
            }

            if (configManager.isSoundsEnabled()) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f);
            }
        } else {
            if (afkPlayers.remove(uuid) != null) {
                LanguageHook.broadcastMessage("afk.no_longer_afk",
                        Placeholder.component("player", nicknameManager.getDisplayName(player)));

                if (configManager.isSoundsEnabled()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!configManager.isAfkAutoUnafkOnMove()) return;

        Player player = event.getPlayer();
        AfkData data = afkPlayers.get(player.getUniqueId());
        if (data == null) return;

        Location to = event.getTo();
        Location orig = data.location();
        if (orig == null || orig.getWorld() == null || !orig.getWorld().equals(to.getWorld())) {
            setAfk(player, false, null);
            return;
        }

        if (orig.distanceSquared(to) > 0.05) {
            setAfk(player, false, null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        if (!configManager.isAfkAutoUnafkOnChat()) return;

        Player player = event.getPlayer();
        if (isAfk(player)) {
            Bukkit.getScheduler().runTask(plugin, () -> setAfk(player, false, null));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (isAfk(player)) {
            String msg = event.getMessage().toLowerCase();
            if (!msg.startsWith("/afk")) {
                setAfk(player, false, null);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isAfk(player)) {
            setAfk(player, false, null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        afkPlayers.remove(event.getPlayer().getUniqueId());
    }
}
