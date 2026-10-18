package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.hook.LanguageHook;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class GamemodeAliasCommand implements BasicCommand {

    private final GameMode mode;
    private final String permission;

    public GamemodeAliasCommand(@NotNull GameMode mode, @NotNull String permission) {
        this.mode = mode;
        this.permission = permission;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) return;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                LanguageHook.sendMessage(sender, "gamemode.usage_console");
                return;
            }

            if (!player.hasPermission(permission) && !player.hasPermission("clarityessentials.command.gamemode")) {
                LanguageHook.sendMessage(player, "general.no_permission");
                return;
            }

            GamemodeCommand.setMode(player, mode);
            LanguageHook.sendMessage(player, "gamemode.changed_self",
                    Placeholder.unparsed("mode", GamemodeCommand.formatModeName(mode)));
            return;
        }

        // Target mode
        if (!sender.hasPermission("clarityessentials.command.gamemode.others")) {
            LanguageHook.sendMessage(sender, "gamemode.no_permission_others");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            LanguageHook.sendMessage(sender, "general.player_not_found", Placeholder.unparsed("player", args[0]));
            return;
        }

        GamemodeCommand.setMode(target, mode);
        LanguageHook.sendMessage(sender, "gamemode.changed_target",
                Placeholder.unparsed("target", target.getName()),
                Placeholder.unparsed("mode", GamemodeCommand.formatModeName(mode)));
        LanguageHook.sendMessage(target, "gamemode.target_changed",
                Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("mode", GamemodeCommand.formatModeName(mode)));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (args.length <= 1 && sender != null && sender.hasPermission("clarityessentials.command.gamemode.others")) {
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
        return permission;
    }
}
