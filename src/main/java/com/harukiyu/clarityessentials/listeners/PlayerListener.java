package com.harukiyu.clarityessentials.listeners;

import com.harukiyu.clarityessentials.nickname.NicknameManager;
import com.harukiyu.clarityessentials.teleport.TeleportManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {

    private final NicknameManager nicknameManager;
    private final TeleportManager teleportManager;

    public PlayerListener(@NotNull NicknameManager nicknameManager, @NotNull TeleportManager teleportManager) {
        this.nicknameManager = nicknameManager;
        this.teleportManager = teleportManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        nicknameManager.applyNickname(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        teleportManager.handleQuit(player);
    }
}
