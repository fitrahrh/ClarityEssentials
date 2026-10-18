package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.dialog.UpdateDialogManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UpdateCommand implements BasicCommand {

    private final UpdateDialogManager updateDialogManager;
    private final ConfigManager configManager;

    public UpdateCommand(@NotNull UpdateDialogManager updateDialogManager, @NotNull ConfigManager configManager) {
        this.updateDialogManager = updateDialogManager;
        this.configManager = configManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) return;

        if (sender instanceof Player player) {
            updateDialogManager.openChangelogDialog(player, false);
            return;
        }

        // Console fallback
        sender.sendMessage(MiniMessage.miniMessage().deserialize(configManager.getChangelogTitle()));
        for (String line : configManager.getChangelogBody()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(line));
        }
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.update";
    }
}
