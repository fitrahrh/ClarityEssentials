package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.spawn.SpawnManager;
import com.harukiyu.clarityessentials.teleport.TeleportManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SpawnCommand implements BasicCommand {

    private final SpawnManager spawnManager;
    private final TeleportManager teleportManager;
    private final ConfigManager configManager;

    public SpawnCommand(@NotNull SpawnManager spawnManager,
                        @NotNull TeleportManager teleportManager,
                        @NotNull ConfigManager configManager) {
        this.spawnManager = spawnManager;
        this.teleportManager = teleportManager;
        this.configManager = configManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) return;

        Location spawnLoc = spawnManager.getSpawn();

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                LanguageHook.sendMessage(sender, "spawn.usage_console");
                return;
            }

            if (!player.hasPermission("clarityessentials.command.spawn")) {
                LanguageHook.sendMessage(player, "general.no_permission");
                return;
            }

            teleportManager.teleportWithWarmup(player, spawnLoc);
            return;
        }

        // Teleport target player to spawn
        if (!sender.hasPermission("clarityessentials.command.spawn.others")) {
            LanguageHook.sendMessage(sender, "spawn.no_permission_others");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            LanguageHook.sendMessage(sender, "general.player_not_found", Placeholder.unparsed("player", args[0]));
            return;
        }

        target.teleportAsync(spawnLoc).thenAccept(success -> {
            if (success) {
                LanguageHook.sendMessage(sender, "spawn.teleported_target", Placeholder.unparsed("target", target.getName()));
                LanguageHook.sendMessage(target, "spawn.target_teleported", Placeholder.unparsed("sender", sender.getName()));
                if (configManager.isSoundsEnabled()) {
                    target.playSound(target.getLocation(), configManager.getSoundTeleportSuccess(), 1.0f, 1.0f);
                }
            }
        });
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (args.length <= 1 && sender != null && sender.hasPermission("clarityessentials.command.spawn.others")) {
            String prefix = (args.length == 0) ? "" : args[0].toLowerCase(Locale.ROOT);
            List<String> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    list.add(p.getName());
                }
            }
            return list;
        }
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.spawn";
    }
}
