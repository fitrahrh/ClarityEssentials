package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.message.PrivateMessageManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class MsgCommand implements BasicCommand {

    private final PrivateMessageManager messageManager;

    public MsgCommand(@NotNull PrivateMessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player player)) {
            LanguageHook.sendMessage(sender, "general.only_players");
            return;
        }

        if (args.length < 2) {
            LanguageHook.sendMessage(player, "message.usage_msg");
            return;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            LanguageHook.sendMessage(player, "general.player_not_found", Placeholder.unparsed("player", targetName));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) sb.append(" ");
            sb.append(args[i]);
        }

        messageManager.sendPrivateMessage(player, target, sb.toString());
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length <= 1) {
            String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
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
        return "clarityessentials.command.msg";
    }
}
