package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.dialog.HomeDialogManager;
import com.harukiyu.clarityessentials.home.Home;
import com.harukiyu.clarityessentials.home.HomeManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
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

public class HomeCommand implements BasicCommand {

    private final HomeManager homeManager;
    private final HomeDialogManager homeDialogManager;

    public HomeCommand(@NotNull HomeManager homeManager, @NotNull HomeDialogManager homeDialogManager) {
        this.homeManager = homeManager;
        this.homeDialogManager = homeDialogManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) {
            return;
        }
        if (!(sender instanceof Player player)) {
            LanguageHook.sendMessage(sender, "general.only_players");
            return;
        }

        // If no arguments, open Paper Native Dialog!
        if (args.length == 0) {
            homeDialogManager.openHomeDialog(player);
            return;
        }

        // Teleport to named home
        homeManager.teleportHome(player, args[0]);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (args.length <= 1 && sender instanceof Player player) {
            String prefix = (args.length == 0) ? "" : args[0].toLowerCase(Locale.ROOT);
            List<String> list = new ArrayList<>();
            for (Home home : homeManager.getHomes(player)) {
                if (home.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    list.add(home.getName());
                }
            }
            return list;
        }
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.home";
    }
}
