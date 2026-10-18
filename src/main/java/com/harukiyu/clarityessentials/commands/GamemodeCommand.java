package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class GamemodeCommand implements BasicCommand {

    private final ConfigManager configManager;

    private static final List<String> MODE_SUGGESTIONS = List.of(
            "survival", "creative", "adventure", "spectator"
    );

    public GamemodeCommand(@NotNull ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) return;

        if (args.length == 0) {
            LanguageHook.sendMessage(sender, "gamemode.usage");
            return;
        }

        GameMode mode = parseGameMode(args[0]);
        if (mode == null) {
            LanguageHook.sendMessage(sender, "gamemode.invalid_mode", Placeholder.unparsed("mode", args[0]));
            return;
        }

        String permNode = "clarityessentials.command.gamemode." + mode.name().toLowerCase(Locale.ROOT);
        if (!sender.hasPermission(permNode) && !sender.hasPermission("clarityessentials.command.gamemode")) {
            LanguageHook.sendMessage(sender, "general.no_permission");
            return;
        }

        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                LanguageHook.sendMessage(sender, "gamemode.usage_console");
                return;
            }

            setMode(configManager, player, mode);
            LanguageHook.sendMessage(player, "gamemode.changed_self",
                    Placeholder.unparsed("mode", formatModeName(mode)));
            return;
        }

        // Change mode for target
        if (!sender.hasPermission("clarityessentials.command.gamemode.others")) {
            LanguageHook.sendMessage(sender, "gamemode.no_permission_others");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            LanguageHook.sendMessage(sender, "general.player_not_found", Placeholder.unparsed("player", args[1]));
            return;
        }

        setMode(configManager, target, mode);
        LanguageHook.sendMessage(sender, "gamemode.changed_target",
                Placeholder.unparsed("target", target.getName()),
                Placeholder.unparsed("mode", formatModeName(mode)));
        LanguageHook.sendMessage(target, "gamemode.target_changed",
                Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("mode", formatModeName(mode)));
    }

    public static void setMode(@Nullable ConfigManager configManager, @NotNull Player player, @NotNull GameMode mode) {
        player.setGameMode(mode);
        if (configManager == null || configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        }
    }

    public static void setMode(@NotNull Player player, @NotNull GameMode mode) {
        setMode(null, player, mode);
    }

    @Nullable
    public static GameMode parseGameMode(@NotNull String input) {
        return switch (input.toLowerCase(Locale.ROOT)) {
            case "0", "s", "survival" -> GameMode.SURVIVAL;
            case "1", "c", "creative" -> GameMode.CREATIVE;
            case "2", "a", "adventure" -> GameMode.ADVENTURE;
            case "3", "sp", "spectator" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    @NotNull
    public static String formatModeName(@NotNull GameMode mode) {
        return switch (mode) {
            case SURVIVAL -> "Survival";
            case CREATIVE -> "Creative";
            case ADVENTURE -> "Adventure";
            case SPECTATOR -> "Spectator";
        };
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (args.length <= 1) {
            String prefix = (args.length == 0) ? "" : args[0].toLowerCase(Locale.ROOT);
            List<String> list = new ArrayList<>();
            for (String m : MODE_SUGGESTIONS) {
                if (m.startsWith(prefix)) list.add(m);
            }
            return list;
        } else if (args.length == 2 && sender != null && sender.hasPermission("clarityessentials.command.gamemode.others")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
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
        return "clarityessentials.command.gamemode";
    }
}
