package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.hook.LanguageHook;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class LanguageCommand implements BasicCommand {

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

        if (args.length > 0) {
            String targetLang = args[0].trim();
            if (targetLang.equalsIgnoreCase("auto") || targetLang.equalsIgnoreCase("reset")) {
                LanguageHook.setPlayerLanguage(player, "");
                return;
            }
            String normalized = LanguageHook.normalizeLanguageCode(targetLang);
            LanguageHook.setPlayerLanguage(player, normalized);
            return;
        }

        LanguageHook.openLanguageDialog(player);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length <= 1) {
            return List.of("en_US", "id_ID", "es_ES", "pt_BR", "zh_CN", "ja_JP", "de_DE", "fr_FR", "ru_RU", "auto");
        }
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.language";
    }
}
