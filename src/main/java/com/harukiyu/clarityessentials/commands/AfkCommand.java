package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.afk.AfkManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class AfkCommand implements BasicCommand {

    private final AfkManager afkManager;

    public AfkCommand(@NotNull AfkManager afkManager) {
        this.afkManager = afkManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) return;

        if (!(sender instanceof Player player)) {
            LanguageHook.sendMessage(sender, "general.only_players");
            return;
        }

        if (!player.hasPermission("clarityessentials.command.afk")) {
            LanguageHook.sendMessage(player, "general.no_permission");
            return;
        }

        String reason = args.length > 0 ? String.join(" ", args) : null;
        afkManager.toggleAfk(player, reason);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.afk";
    }
}
