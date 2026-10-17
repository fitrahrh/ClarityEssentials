package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.nickname.NicknameManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class RealnameCommand implements BasicCommand {

    private final NicknameManager nicknameManager;

    public RealnameCommand(@NotNull NicknameManager nicknameManager) {
        this.nicknameManager = nicknameManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) {
            return;
        }

        if (args.length == 0) {
            LanguageHook.sendMessage(sender, "nickname.usage_realname");
            return;
        }

        String searchNick = args[0];
        UUID uuid = nicknameManager.getPlayerByNickname(searchNick);

        if (uuid == null) {
            LanguageHook.sendMessage(sender, "nickname.realname_not_found", Placeholder.unparsed("nickname", searchNick));
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String realName = offlinePlayer.getName() != null ? offlinePlayer.getName() : uuid.toString();

        LanguageHook.sendMessage(sender, "nickname.realname_result",
                Placeholder.unparsed("nickname", searchNick),
                Placeholder.unparsed("realname", realName)
        );
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length <= 1) {
            String prefix = (args.length == 0) ? "" : args[0].toLowerCase(Locale.ROOT);
            List<String> list = new ArrayList<>();
            for (String nick : nicknameManager.getOnlineNicknames()) {
                if (nick.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    list.add(nick);
                }
            }
            return list;
        }
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.realname";
    }
}
