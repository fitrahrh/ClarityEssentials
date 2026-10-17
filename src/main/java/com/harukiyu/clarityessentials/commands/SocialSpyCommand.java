package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.message.PrivateMessageManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SocialSpyCommand implements BasicCommand {

    private final PrivateMessageManager messageManager;

    public SocialSpyCommand(@NotNull PrivateMessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player player)) {
            LanguageHook.sendMessage(sender, "general.only_players");
            return;
        }

        if (!player.hasPermission("clarityessentials.socialspy") && !player.hasPermission("clarityessentials.admin")) {
            LanguageHook.sendMessage(player, "general.no_permission");
            return;
        }

        messageManager.toggleSocialSpy(player);
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.socialspy";
    }
}
