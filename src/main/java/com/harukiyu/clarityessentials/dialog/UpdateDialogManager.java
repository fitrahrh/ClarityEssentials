package com.harukiyu.clarityessentials.dialog;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.settings.UserSettings;
import com.harukiyu.clarityessentials.settings.UserSettingsManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UpdateDialogManager {

    private static final ClickCallback.Options DEFAULT_OPTIONS = ClickCallback.Options.builder().build();

    private final Plugin plugin;
    private final ConfigManager configManager;
    private final UserSettingsManager userSettingsManager;

    public UpdateDialogManager(@NotNull Plugin plugin,
                               @NotNull ConfigManager configManager,
                               @NotNull UserSettingsManager userSettingsManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.userSettingsManager = userSettingsManager;
    }

    /**
     * Checks join criteria and displays either First Join Tips or Changelog Dialog.
     */
    public void checkAndShowJoinDialog(@NotNull Player player) {
        if (!configManager.isJoinDialogEnabled()) {
            return;
        }

        UserSettings settings = userSettingsManager.getSettings(player);

        // 1. First Join Tips Dialog
        if (!player.hasPlayedBefore() || !settings.isFirstJoinTipsShown()) {
            if (configManager.isFirstJoinTipsEnabled()) {
                settings.setFirstJoinTipsShown(true);
                userSettingsManager.saveSettingsAsync(player.getUniqueId());

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        openFirstJoinTipsDialog(player);
                    }
                }, 20L);
                return;
            }
        }

        // 2. Returning Player Server Updates & Changelog
        String currentUpdateId = configManager.getJoinDialogUpdateId();
        String dismissedUpdateId = settings.getLastDismissedUpdateId();

        boolean isNewUpdate = !currentUpdateId.equalsIgnoreCase(dismissedUpdateId);

        if (isNewUpdate) {
            // New update version released! Show dialog
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    openChangelogDialog(player, true);
                }
            }, 25L);
            return;
        }

        // If player already dismissed this update version, check daily reminder
        if (configManager.isJoinDialogDailyReminder() && dismissedUpdateId.isBlank()) {
            long lastShown = settings.getLastChangelogShownTime();
            long oneDayMs = 24 * 60 * 60 * 1000L;
            if (System.currentTimeMillis() - lastShown >= oneDayMs) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        openChangelogDialog(player, true);
                    }
                }, 25L);
            }
        }
    }

    /**
     * Opens the Native Dialog containing Beginner Tips for first-time players.
     */
    public void openFirstJoinTipsDialog(@NotNull Player player) {
        if (configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), configManager.getSoundDialogOpen(), 1.0f, 1.0f);
        }

        Component title = MiniMessage.miniMessage().deserialize(configManager.getFirstJoinTipsTitle());
        List<DialogBody> bodies = new ArrayList<>();
        for (String line : configManager.getFirstJoinTipsBody()) {
            bodies.add(DialogBody.plainMessage(MiniMessage.miniMessage().deserialize(line)));
        }

        ActionButton closeBtn = ActionButton.builder(LanguageHook.getComponent("joindialog.btn_close", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                    }
                }, DEFAULT_OPTIONS)).build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .body(bodies)
                        .build())
                .type(DialogType.notice(closeBtn))
        );

        player.showDialog(dialog);
    }

    /**
     * Opens the Server Updates / Changelog Native Dialog.
     * @param player player to show dialog to
     * @param showDontRemindButton whether to show the "Jangan ingatkan lagi" button
     */
    public void openChangelogDialog(@NotNull Player player, boolean showDontRemindButton) {
        if (configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), configManager.getSoundDialogOpen(), 1.0f, 1.0f);
        }

        Component title = MiniMessage.miniMessage().deserialize(configManager.getChangelogTitle());
        List<DialogBody> bodies = new ArrayList<>();
        for (String line : configManager.getChangelogBody()) {
            bodies.add(DialogBody.plainMessage(MiniMessage.miniMessage().deserialize(line)));
        }

        ActionButton closeBtn = ActionButton.builder(LanguageHook.getComponent("joindialog.btn_close", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        UserSettings s = userSettingsManager.getSettings(p);
                        s.setLastChangelogShownTime(System.currentTimeMillis());
                        userSettingsManager.saveSettingsAsync(p.getUniqueId());
                        p.closeDialog();
                    }
                }, DEFAULT_OPTIONS)).build();

        if (showDontRemindButton) {
            ActionButton dontRemindBtn = ActionButton.builder(LanguageHook.getComponent("joindialog.btn_dont_remind", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            UserSettings s = userSettingsManager.getSettings(p);
                            s.setLastDismissedUpdateId(configManager.getJoinDialogUpdateId());
                            s.setLastChangelogShownTime(System.currentTimeMillis());
                            userSettingsManager.saveSettingsAsync(p.getUniqueId());
                            LanguageHook.sendMessage(p, "joindialog.dont_remind_ack");
                            p.closeDialog();
                        }
                    }, DEFAULT_OPTIONS)).build();

            Dialog dialog = Dialog.create(builder -> builder.empty()
                    .base(DialogBase.builder(title)
                            .canCloseWithEscape(true)
                            .body(bodies)
                            .build())
                    .type(DialogType.confirmation(dontRemindBtn, closeBtn))
            );
            player.showDialog(dialog);
        } else {
            Dialog dialog = Dialog.create(builder -> builder.empty()
                    .base(DialogBase.builder(title)
                            .canCloseWithEscape(true)
                            .body(bodies)
                            .build())
                    .type(DialogType.notice(closeBtn))
            );
            player.showDialog(dialog);
        }
    }
}
