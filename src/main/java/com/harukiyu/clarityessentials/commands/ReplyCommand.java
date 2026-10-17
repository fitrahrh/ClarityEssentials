package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.message.PrivateMessageManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReplyCommand implements BasicCommand {

    private final PrivateMessageManager messageManager;

    public ReplyCommand(@NotNull PrivateMessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player player)) {
            LanguageHook.sendMessage(sender, "general.only_players");
            return;
        }

        if (args.length == 0) {
            LanguageHook.sendMessage(player, "message.usage_reply");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(args[i]);
        }

        messageManager.reply(player, sb.toString());
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.reply";
    }
}
