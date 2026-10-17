package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.dialog.HomeDialogManager;
import com.harukiyu.clarityessentials.home.HomeManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SetHomeCommand implements BasicCommand {

    private final HomeManager homeManager;
    private final HomeDialogManager homeDialogManager;

    public SetHomeCommand(@NotNull HomeManager homeManager, @NotNull HomeDialogManager homeDialogManager) {
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

        if (args.length == 0) {
            homeDialogManager.openSetHomeInputDialog(player);
            return;
        }

        String homeName = args[0];

        HomeManager.SetHomeResult result = homeManager.setHome(player, homeName);
        switch (result) {
            case SUCCESS -> LanguageHook.sendMessage(player, "homes.set_success", Placeholder.unparsed("home", homeName));
            case LIMIT_REACHED -> LanguageHook.sendMessage(player, "homes.limit_reached",
                    Placeholder.unparsed("limit", String.valueOf(homeManager.getMaxHomes(player)))
            );
            case INVALID_NAME -> LanguageHook.sendMessage(player, "homes.invalid_name");
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.sethome";
    }
}
