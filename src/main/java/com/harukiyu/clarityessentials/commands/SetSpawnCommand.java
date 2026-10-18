package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.spawn.SpawnManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SetSpawnCommand implements BasicCommand {

    private final SpawnManager spawnManager;
    private final ConfigManager configManager;

    public SetSpawnCommand(@NotNull SpawnManager spawnManager, @NotNull ConfigManager configManager) {
        this.spawnManager = spawnManager;
        this.configManager = configManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player player)) {
            LanguageHook.sendMessage(sender, "general.only_players");
            return;
        }

        if (!player.hasPermission("clarityessentials.command.setspawn")) {
            LanguageHook.sendMessage(player, "general.no_permission");
            return;
        }

        boolean isFirstJoin = false;
        if (args.length > 0) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("firstjoin") || sub.equals("first") || sub.equals("new")) {
                isFirstJoin = true;
            }
        }

        if (isFirstJoin) {
            spawnManager.setFirstJoinSpawn(player.getLocation());
            LanguageHook.sendMessage(player, "spawn.set_firstjoin");
        } else {
            spawnManager.setDefaultSpawn(player.getLocation());
            LanguageHook.sendMessage(player, "spawn.set_default");
        }

        if (configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), configManager.getSoundActionSuccess(), 1.0f, 1.0f);
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length <= 1) {
            String prefix = (args.length == 0) ? "" : args[0].toLowerCase(Locale.ROOT);
            List<String> list = new ArrayList<>();
            for (String sub : List.of("default", "firstjoin")) {
                if (sub.startsWith(prefix)) list.add(sub);
            }
            return list;
        }
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.setspawn";
    }
}
