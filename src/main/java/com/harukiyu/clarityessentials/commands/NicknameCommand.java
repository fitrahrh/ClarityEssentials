package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.dialog.MenuDialogManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.nickname.NicknameManager;
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

public class NicknameCommand implements BasicCommand {

    private final ConfigManager configManager;
    private final NicknameManager nicknameManager;
    private final MenuDialogManager menuDialogManager;

    public NicknameCommand(@NotNull ConfigManager configManager,
                           @NotNull NicknameManager nicknameManager,
                           @NotNull MenuDialogManager menuDialogManager) {
        this.configManager = configManager;
        this.nicknameManager = nicknameManager;
        this.menuDialogManager = menuDialogManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) {
            return;
        }

        if (args.length == 0) {
            if (sender instanceof Player player) {
                menuDialogManager.openNickDialog(player);
                return;
            }
            LanguageHook.sendMessage(sender, "nickname.usage_nick");
            return;
        }

        // Subcommand: /nick off or /nick reset
        if (args.length == 1 && (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("reset"))) {
            if (!(sender instanceof Player player)) {
                LanguageHook.sendMessage(sender, "general.only_players");
                return;
            }
            nicknameManager.resetNickname(player);
            LanguageHook.sendMessage(player, "nickname.reset");
            return;
        }

        // Subcommand: /nick <player> reset or /nick <player> <new_nick> (Admin)
        if (args.length >= 2 && sender.hasPermission("clarityessentials.nick.others")) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                LanguageHook.sendMessage(sender, "general.player_not_found", Placeholder.unparsed("player", args[0]));
                return;
            }

            if (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("reset")) {
                nicknameManager.resetNickname(target);
                LanguageHook.sendMessage(sender, "nickname.reset_other", Placeholder.unparsed("player", target.getName()));
                LanguageHook.sendMessage(target, "nickname.reset");
                return;
            }

            NicknameManager.NickResult result = nicknameManager.setNickname(target, args[1]);
            handleResult(sender, target, args[1], result, true);
            return;
        }

        // Normal: /nick <new_nick>
        if (!(sender instanceof Player player)) {
            LanguageHook.sendMessage(sender, "general.only_players");
            return;
        }

        NicknameManager.NickResult result = nicknameManager.setNickname(player, args[0]);
        handleResult(player, player, args[0], result, false);
    }

    private void handleResult(CommandSender sender, Player target, String nickInput, NicknameManager.NickResult result, boolean isOther) {
        switch (result) {
            case SUCCESS -> {
                if (isOther) {
                    LanguageHook.sendMessage(sender, "nickname.changed_other",
                            Placeholder.unparsed("player", target.getName()),
                            Placeholder.unparsed("nickname", nickInput)
                    );
                    LanguageHook.sendMessage(target, "nickname.changed", Placeholder.unparsed("nickname", nickInput));
                } else {
                    LanguageHook.sendMessage(target, "nickname.changed", Placeholder.unparsed("nickname", nickInput));
                }
            }
            case TOO_SHORT -> LanguageHook.sendMessage(sender, "nickname.too_short",
                    Placeholder.unparsed("min", String.valueOf(configManager.getNickMinLength()))
            );
            case TOO_LONG -> LanguageHook.sendMessage(sender, "nickname.too_long",
                    Placeholder.unparsed("max", String.valueOf(configManager.getNickMaxLength()))
            );
            case INVALID_REGEX -> LanguageHook.sendMessage(sender, "nickname.invalid_regex",
                    Placeholder.unparsed("min", String.valueOf(configManager.getNickMinLength())),
                    Placeholder.unparsed("max", String.valueOf(configManager.getNickMaxLength()))
            );
            case ALREADY_IN_USE -> LanguageHook.sendMessage(sender, "nickname.already_in_use",
                    Placeholder.unparsed("nickname", nickInput)
            );
            case COLOR_NO_PERMISSION -> LanguageHook.sendMessage(sender, "nickname.color_no_permission");
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        List<String> list = new ArrayList<>();

        if (args.length <= 1) {
            String prefix = (args.length == 0) ? "" : args[0].toLowerCase(Locale.ROOT);
            if ("reset".startsWith(prefix)) list.add("reset");
            if ("off".startsWith(prefix)) list.add("off");

            if (sender.hasPermission("clarityessentials.nick.others")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                        list.add(p.getName());
                    }
                }
            }
        } else if (args.length == 2 && sender.hasPermission("clarityessentials.nick.others")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            if ("reset".startsWith(prefix)) list.add("reset");
            if ("off".startsWith(prefix)) list.add("off");
        }

        return list;
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.nick";
    }
}
