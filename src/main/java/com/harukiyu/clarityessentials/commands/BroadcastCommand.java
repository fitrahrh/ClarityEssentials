package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.hook.PlaceholderAPIHook;
import com.harukiyu.clarityessentials.utils.Utils;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class BroadcastCommand implements BasicCommand {

    private final ConfigManager configManager;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public BroadcastCommand(@NotNull ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) return;

        if (!sender.hasPermission("clarityessentials.command.broadcast")) {
            LanguageHook.sendMessage(sender, "general.no_permission");
            return;
        }

        if (args.length == 0) {
            LanguageHook.sendMessage(sender, "broadcast.usage");
            return;
        }

        String rawContent = String.join(" ", args);

        if (sender instanceof Player player) {
            rawContent = PlaceholderAPIHook.setPlaceholders(player, rawContent);
        }

        rawContent = Utils.convertLegacyToMiniMessage(rawContent);

        Component parsedContent;
        try {
            parsedContent = MINI_MESSAGE.deserialize(rawContent);
        } catch (Exception e) {
            parsedContent = Component.text(rawContent);
        }

        String langCode = (sender instanceof Player player) ? LanguageHook.getEffectiveLanguageCode(player) : "en_US";
        String format = LanguageHook.getRawString("broadcast.format", langCode);
        if (format == null || format.isBlank()) {
            format = "<newline><gold><bold>[BROADCAST]</bold></gold> <yellow><message></yellow><newline>";
        }

        Component finalBroadcast = MINI_MESSAGE.deserialize(format, Placeholder.component("message", parsedContent));

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(finalBroadcast);
            if (configManager.isSoundsEnabled()) {
                online.playSound(online.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
            }
        }
        Bukkit.getConsoleSender().sendMessage(finalBroadcast);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.broadcast";
    }
}
