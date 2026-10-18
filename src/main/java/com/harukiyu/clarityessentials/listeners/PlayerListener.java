package com.harukiyu.clarityessentials.listeners;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.nickname.NicknameManager;
import com.harukiyu.clarityessentials.settings.UserSettings;
import com.harukiyu.clarityessentials.settings.UserSettingsManager;
import com.harukiyu.clarityessentials.teleport.TeleportManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.ServerLinks;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

public class PlayerListener implements Listener {

    private final ConfigManager configManager;
    private final NicknameManager nicknameManager;
    private final TeleportManager teleportManager;
    private final UserSettingsManager userSettingsManager;
    private final com.harukiyu.clarityessentials.spawn.SpawnManager spawnManager;
    private final com.harukiyu.clarityessentials.mail.MailManager mailManager;
    private final com.harukiyu.clarityessentials.dialog.UpdateDialogManager updateDialogManager;

    public PlayerListener(@NotNull ConfigManager configManager,
                          @NotNull NicknameManager nicknameManager,
                          @NotNull TeleportManager teleportManager,
                          @NotNull UserSettingsManager userSettingsManager,
                          @NotNull com.harukiyu.clarityessentials.spawn.SpawnManager spawnManager,
                          @NotNull com.harukiyu.clarityessentials.mail.MailManager mailManager,
                          @NotNull com.harukiyu.clarityessentials.dialog.UpdateDialogManager updateDialogManager) {
        this.configManager = configManager;
        this.nicknameManager = nicknameManager;
        this.teleportManager = teleportManager;
        this.userSettingsManager = userSettingsManager;
        this.spawnManager = spawnManager;
        this.mailManager = mailManager;
        this.updateDialogManager = updateDialogManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        if (configManager.isSpawnEnabled() && configManager.isSpawnRespawnAtSpawn()) {
            if (configManager.isSpawnRespawnOverrideBed() || (!event.isBedSpawn() && !event.isAnchorSpawn())) {
                event.setRespawnLocation(spawnManager.getSpawn());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        nicknameManager.applyNickname(player);

        // Spawn Teleportation Check
        if (!player.hasPlayedBefore() && configManager.isSpawnEnabled() && configManager.isSpawnTeleportOnFirstJoin()) {
            player.teleportAsync(spawnManager.getFirstJoinSpawn());
        } else if (configManager.isSpawnEnabled() && configManager.isSpawnTeleportOnJoin()) {
            player.teleportAsync(spawnManager.getSpawn());
        }

        if (configManager.isEscMenuEnabled()) {
            try {
                ServerLinks serverLinks = player.getServer().getServerLinks().copy();
                serverLinks.addLink(Component.text(configManager.getEscMenuButtonText()), URI.create("clarity://menu"));
                player.sendLinks(serverLinks);
            } catch (Throwable ignored) {
            }
        }

        if (configManager.isJoinLeaveEnabled()) {
            event.joinMessage(null);
            String template = configManager.getDefaultJoinMessage();
            for (ConfigManager.VipJoinRule rule : configManager.getVipJoinRules()) {
                if (player.hasPermission(rule.permission())) {
                    template = rule.message();
                    break;
                }
            }
            Component joinMsg = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(template,
                    Placeholder.component("player", nicknameManager.getDisplayName(player))
            );
            for (Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
                UserSettings s = userSettingsManager.getSettings(online.getUniqueId());
                String pref = s.getJoinLeaveMessages();
                if ("Off".equalsIgnoreCase(pref)) continue;
                if ("Friends".equalsIgnoreCase(pref) && !online.equals(player) && !s.isFriend(player.getName())) continue;
                online.sendMessage(joinMsg);
            }
            org.bukkit.Bukkit.getConsoleSender().sendMessage(joinMsg);
        }

        // Unread Mail Notification
        if (configManager.isMailEnabled() && configManager.isMailNotifyOnJoin()) {
            int unread = mailManager.getUnreadCount(player.getUniqueId());
            if (unread > 0) {
                LanguageHook.sendMessage(player, "mail.unread_join_notice",
                        Placeholder.unparsed("count", String.valueOf(unread))
                );
            }
        }

        // Join Dialog & Changelog check
        updateDialogManager.checkAndShowJoinDialog(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        teleportManager.handleQuit(player);

        if (configManager.isJoinLeaveEnabled()) {
            event.quitMessage(null);
            Component quitMsg = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(configManager.getDefaultLeaveMessage(),
                    Placeholder.component("player", nicknameManager.getDisplayName(player))
            );
            for (Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
                if (online.equals(player)) continue;
                UserSettings s = userSettingsManager.getSettings(online.getUniqueId());
                String pref = s.getJoinLeaveMessages();
                if ("Off".equalsIgnoreCase(pref)) continue;
                if ("Friends".equalsIgnoreCase(pref) && !s.isFriend(player.getName())) continue;
                online.sendMessage(quitMsg);
            }
            org.bukkit.Bukkit.getConsoleSender().sendMessage(quitMsg);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = null;
        if (event.getDamager() instanceof Player directAttacker) {
            attacker = directAttacker;
        } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }

        if (attacker == null || attacker.equals(victim)) {
            return;
        }

        UserSettings attackerSettings = userSettingsManager.getSettings(attacker);
        UserSettings victimSettings = userSettingsManager.getSettings(victim);

        if (attackerSettings.isFriend(victim.getName()) || victimSettings.isFriend(attacker.getName())) {
            event.setCancelled(true);
            LanguageHook.sendMessage(attacker, "friends.friendly_fire_blocked", Placeholder.unparsed("target", victim.getName()));
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Component deathMsg = event.deathMessage();
        if (deathMsg == null) {
            return;
        }

        Player victim = event.getEntity();
        event.deathMessage(null); // Cancel global broadcast to filter per recipient

        for (Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
            UserSettings s = userSettingsManager.getSettings(online.getUniqueId());
            String pref = s.getDeathMessages();
            if ("Off".equalsIgnoreCase(pref)) continue;
            if ("Friends".equalsIgnoreCase(pref) && !online.equals(victim) && !s.isFriend(victim.getName())) continue;
            online.sendMessage(deathMsg);
        }
        org.bukkit.Bukkit.getConsoleSender().sendMessage(deathMsg);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAdvancementDone(org.bukkit.event.player.PlayerAdvancementDoneEvent event) {
        Component advMsg = event.message();
        if (advMsg == null) {
            return;
        }

        Player player = event.getPlayer();
        event.message(null); // Cancel global broadcast to filter per recipient

        for (Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
            UserSettings s = userSettingsManager.getSettings(online.getUniqueId());
            String pref = s.getAdvancementMessages();
            if ("Off".equalsIgnoreCase(pref)) continue;
            if ("Friends".equalsIgnoreCase(pref) && !online.equals(player) && !s.isFriend(player.getName())) continue;
            online.sendMessage(advMsg);
        }
        org.bukkit.Bukkit.getConsoleSender().sendMessage(advMsg);
    }
}
