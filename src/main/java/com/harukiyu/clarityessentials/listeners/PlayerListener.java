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

    public PlayerListener(@NotNull ConfigManager configManager,
                          @NotNull NicknameManager nicknameManager,
                          @NotNull TeleportManager teleportManager,
                          @NotNull UserSettingsManager userSettingsManager) {
        this.configManager = configManager;
        this.nicknameManager = nicknameManager;
        this.teleportManager = teleportManager;
        this.userSettingsManager = userSettingsManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        nicknameManager.applyNickname(player);

        if (configManager.isEscMenuEnabled()) {
            try {
                ServerLinks serverLinks = player.getServer().getServerLinks().copy();
                serverLinks.addLink(Component.text(configManager.getEscMenuButtonText()), URI.create("clarity://menu"));
                player.sendLinks(serverLinks);
            } catch (Throwable ignored) {
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        teleportManager.handleQuit(player);
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
}
