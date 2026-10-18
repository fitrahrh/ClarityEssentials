package com.harukiyu.clarityessentials.dialog;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.settings.UserSettings;
import com.harukiyu.clarityessentials.settings.UserSettingsManager;
import com.harukiyu.clarityessentials.teleport.TeleportManager;
import com.harukiyu.clarityessentials.utils.PlayerValidator;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TeleportDialogManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final ClickCallback.Options DEFAULT_OPTIONS = ClickCallback.Options.builder().build();

    private final ConfigManager configManager;
    private final UserSettingsManager settingsManager;
    private final TeleportManager teleportManager;
    private MenuDialogManager menuDialogManager;

    public TeleportDialogManager(@NotNull ConfigManager configManager,
                                 @NotNull UserSettingsManager settingsManager,
                                 @NotNull TeleportManager teleportManager) {
        this.configManager = configManager;
        this.settingsManager = settingsManager;
        this.teleportManager = teleportManager;
    }

    public void setMenuDialogManager(@NotNull MenuDialogManager menuDialogManager) {
        this.menuDialogManager = menuDialogManager;
    }

    /**
     * Opens Teleport List (image copy 14.png)
     */
    public void openTeleportList(@NotNull Player player) {
        openTeleportList(player, true);
    }

    public void openTeleportList(@NotNull Player player, boolean playSound) {
        if (playSound && configManager != null && configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), configManager.getSoundDialogOpen(), 1.0f, 1.0f);
        }

        Component title = LanguageHook.getComponent("teleport_menu.title", player);
        Component body = LanguageHook.getComponent("teleport_menu.body", player);

        List<ActionButton> buttons = new ArrayList<>();
        UserSettings userSettings = settingsManager.getSettings(player);
        Set<String> displayed = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        // 1. Online players
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            displayed.add(online.getName());
            Component label = MM.deserialize("<white>" + online.getName() + "</white> <green>●</green>");
            buttons.add(ActionButton.builder(label)
                    .tooltip(MM.deserialize("<gray>Status: <green>Online</green> - Click to teleport</gray>"))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openTeleportAction(p, online.getName());
                    }, DEFAULT_OPTIONS)).build());
        }

        // 2. Custom added players (offline ones)
        for (String savedName : userSettings.getTeleportList()) {
            if (!displayed.contains(savedName) && !savedName.equalsIgnoreCase(player.getName())) {
                displayed.add(savedName);
                Component label = MM.deserialize("<white>" + savedName + "</white> <gray>○</gray>");
                buttons.add(ActionButton.builder(label)
                        .tooltip(MM.deserialize("<gray>Status: <gray>Offline</gray> - Saved in teleport list</gray>"))
                        .action(DialogAction.customClick((view, audience) -> {
                            if (audience instanceof Player p) openTeleportAction(p, savedName);
                        }, DEFAULT_OPTIONS)).build());
            }
        }

        // + Add Player to List
        buttons.add(ActionButton.builder(LanguageHook.getComponent("teleport_menu.btn_add_player", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openAddPlayerInput(p);
                }, DEFAULT_OPTIONS)).build());

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p && menuDialogManager != null) {
                        menuDialogManager.openDashboard(p, false);
                    } else if (audience instanceof Player p) {
                        p.closeDialog();
                    }
                }, DEFAULT_OPTIONS)).build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 2))
        );

        player.showDialog(dialog);
    }

    /**
     * Opens Add a Player dialog with strict validation
     */
    public void openAddPlayerInput(@NotNull Player player) {
        Component title = LanguageHook.getComponent("teleport_menu.add_title", player);
        Component body = LanguageHook.getComponent("teleport_menu.add_body", player);

        TextDialogInput textInput = DialogInput.text("player_name", LanguageHook.getComponent("teleport_menu.add_label", player))
                .maxLength(16)
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .inputs(List.of(textInput))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(LanguageHook.getComponent("teleport_menu.btn_add_to_list", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        String raw = view.getText("player_name");
                                        PlayerValidator.ValidationResult res = PlayerValidator.validatePlayerName(p, raw);
                                        if (!res.valid()) {
                                            openTeleportList(p, false);
                                            return;
                                        }

                                        String targetName = res.resolvedName();
                                        if (targetName == null) {
                                            openTeleportList(p, false);
                                            return;
                                        }

                                        UserSettings s = settingsManager.getSettings(p);
                                        if (s.getTeleportList().stream().anyMatch(n -> n.equalsIgnoreCase(targetName))) {
                                            LanguageHook.sendMessage(p, "general.already_added", Placeholder.unparsed("player", targetName));
                                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                            openTeleportList(p, false);
                                            return;
                                        }

                                        s.addTeleportPlayer(targetName);
                                        settingsManager.saveSettingsAsync(p.getUniqueId());
                                        LanguageHook.sendMessage(p, "teleport_menu.player_added", Placeholder.unparsed("player", targetName));
                                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                                        openTeleportList(p, false);
                                    }
                                }, DEFAULT_OPTIONS)).build(),
                        ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) openTeleportList(p, false);
                                }, DEFAULT_OPTIONS)).build()
                ))
        );

        player.showDialog(dialog);
    }

    /**
     * Opens Teleport Action choice: Teleport to / Teleport here / Remove from List
     */
    public void openTeleportAction(@NotNull Player player, @NotNull String targetName) {
        Component title = LanguageHook.getComponent("teleport_menu.action_title", player,
                Placeholder.unparsed("target", targetName));

        List<ActionButton> buttons = new ArrayList<>();
        UserSettings userSettings = settingsManager.getSettings(player);

        // Teleport to
        buttons.add(ActionButton.builder(LanguageHook.getComponent("teleport_menu.btn_teleport_to", player))
                .tooltip(LanguageHook.getComponent("teleport_menu.btn_teleport_to_tooltip", player, Placeholder.unparsed("target", targetName)))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        Player target = Bukkit.getPlayerExact(targetName);
                        if (target != null) {
                            teleportManager.sendTpa(p, target);
                        } else {
                            LanguageHook.sendMessage(p, "general.player_not_found", Placeholder.unparsed("player", targetName));
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        }
                    }
                }, DEFAULT_OPTIONS)).build());

        // Teleport here
        buttons.add(ActionButton.builder(LanguageHook.getComponent("teleport_menu.btn_teleport_here", player))
                .tooltip(LanguageHook.getComponent("teleport_menu.btn_teleport_here_tooltip", player, Placeholder.unparsed("target", targetName)))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        Player target = Bukkit.getPlayerExact(targetName);
                        if (target != null) {
                            teleportManager.sendTpaHere(p, target);
                        } else {
                            LanguageHook.sendMessage(p, "general.player_not_found", Placeholder.unparsed("player", targetName));
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        }
                    }
                }, DEFAULT_OPTIONS)).build());

        // Remove from List (if in teleportList)
        if (userSettings.getTeleportList().stream().anyMatch(n -> n.equalsIgnoreCase(targetName))) {
            buttons.add(ActionButton.builder(LanguageHook.getComponent("teleport_menu.btn_remove", player))
                    .tooltip(LanguageHook.getComponent("teleport_menu.btn_remove_tooltip", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openConfirmRemoveTeleport(p, targetName);
                    }, DEFAULT_OPTIONS)).build());
        }

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openTeleportList(p, false);
                }, DEFAULT_OPTIONS)).build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 1))
        );

        player.showDialog(dialog);
    }

    /**
     * Confirmation dialog to remove player from teleport list
     */
    public void openConfirmRemoveTeleport(@NotNull Player player, @NotNull String targetName) {
        Component title = LanguageHook.getComponent("teleport_menu.confirm_remove_title", player);
        Component body = LanguageHook.getComponent("teleport_menu.confirm_remove_body", player,
                Placeholder.unparsed("target", targetName));

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(LanguageHook.getComponent("teleport_menu.btn_confirm_remove", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        settingsManager.getSettings(p).removeTeleportPlayer(targetName);
                                        settingsManager.saveSettingsAsync(p.getUniqueId());
                                        LanguageHook.sendMessage(p, "teleport_menu.player_removed", Placeholder.unparsed("player", targetName));
                                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                                        openTeleportList(p, false);
                                    }
                                }, DEFAULT_OPTIONS)).build(),
                        ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) openTeleportAction(p, targetName);
                                }, DEFAULT_OPTIONS)).build()
                ))
        );

        player.showDialog(dialog);
    }
}
