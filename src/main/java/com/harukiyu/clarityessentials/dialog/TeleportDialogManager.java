package com.harukiyu.clarityessentials.dialog;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.settings.UserSettings;
import com.harukiyu.clarityessentials.settings.UserSettingsManager;
import com.harukiyu.clarityessentials.teleport.TeleportManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TeleportDialogManager {

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
        Component title = LanguageHook.getComponent("teleport_menu.title", player);
        Component body = LanguageHook.getComponent("teleport_menu.body", player);

        List<ActionButton> buttons = new ArrayList<>();
        UserSettings userSettings = settingsManager.getSettings(player);

        // Online players
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            buttons.add(ActionButton.builder(Component.text(online.getName()))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openTeleportAction(p, online.getName());
                    })).build());
        }

        // Custom added players
        for (String savedName : userSettings.getTeleportList()) {
            boolean already = buttons.stream().anyMatch(b -> b.label().toString().contains(savedName));
            if (!already && !savedName.equalsIgnoreCase(player.getName())) {
                buttons.add(ActionButton.builder(Component.text(savedName))
                        .tooltip(LanguageHook.getComponent("teleport_menu.added_by_you", player))
                        .action(DialogAction.customClick((view, audience) -> {
                            if (audience instanceof Player p) openTeleportAction(p, savedName);
                        })).build());
            }
        }

        // + Add Player to List
        buttons.add(ActionButton.builder(LanguageHook.getComponent("teleport_menu.btn_add_player", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openAddPlayerInput(p);
                })).build());

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p && menuDialogManager != null) {
                        menuDialogManager.openDashboard(p);
                    }
                })).build();

        Dialog dialog = Dialog.create(builder -> builder
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 2))
                .build()
        );

        player.showDialog(dialog);
    }

    /**
     * Opens Add a Player dialog (image copy 13.png)
     */
    public void openAddPlayerInput(@NotNull Player player) {
        Component title = LanguageHook.getComponent("teleport_menu.add_title", player);
        Component body = LanguageHook.getComponent("teleport_menu.add_body", player);

        Dialog dialog = Dialog.create(builder -> builder
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .inputs(List.of(
                                DialogInput.text("player_name", TextDialogInput.builder()
                                        .label(LanguageHook.getComponent("teleport_menu.add_label", player))
                                        .maxLength(16)
                                        .build())
                        ))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(LanguageHook.getComponent("teleport_menu.btn_add_to_list", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        String targetName = view.getText("player_name");
                                        if (targetName != null && !targetName.isBlank()) {
                                            targetName = targetName.trim();
                                            settingsManager.getSettings(p).addTeleportPlayer(targetName);
                                            settingsManager.saveSettingsAsync(p.getUniqueId());
                                            openTeleportAction(p, targetName);
                                        } else {
                                            openTeleportList(p);
                                        }
                                    }
                                })).build(),
                        ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) openTeleportList(p);
                                })).build()
                ))
                .build()
        );

        player.showDialog(dialog);
    }

    /**
     * Opens Teleport Action choice: Teleport to / Teleport here (image copy 15.png)
     */
    public void openTeleportAction(@NotNull Player player, @NotNull String targetName) {
        Component title = LanguageHook.getComponent("teleport_menu.action_title", player,
                Placeholder.unparsed("target", targetName));

        List<ActionButton> buttons = new ArrayList<>();

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
                        }
                    }
                })).build());

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
                        }
                    }
                })).build());

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openTeleportList(p);
                })).build();

        Dialog dialog = Dialog.create(builder -> builder
                .base(DialogBase.builder(title).build())
                .type(DialogType.multiAction(buttons, exitBtn, 1))
                .build()
        );

        player.showDialog(dialog);
    }
}
