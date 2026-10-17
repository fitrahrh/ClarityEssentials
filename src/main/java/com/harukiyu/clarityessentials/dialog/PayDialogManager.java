package com.harukiyu.clarityessentials.dialog;

import com.harukiyu.clarityessentials.ClarityEssentials;
import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.hook.VaultHook;
import com.harukiyu.clarityessentials.settings.UserSettings;
import com.harukiyu.clarityessentials.settings.UserSettingsManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PayDialogManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final ClarityEssentials plugin;
    private final ConfigManager configManager;
    private final UserSettingsManager settingsManager;
    private MenuDialogManager menuDialogManager;

    public PayDialogManager(@NotNull ClarityEssentials plugin,
                            @NotNull ConfigManager configManager,
                            @NotNull UserSettingsManager settingsManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.settingsManager = settingsManager;
    }

    public void setMenuDialogManager(@NotNull MenuDialogManager menuDialogManager) {
        this.menuDialogManager = menuDialogManager;
    }

    /**
     * Opens Pay list (image copy 4.png)
     */
    public void openPayPlayerList(@NotNull Player player) {
        Component title = LanguageHook.getComponent("pay.title", player);
        Component body = LanguageHook.getComponent("pay.body", player);

        List<ActionButton> buttons = new ArrayList<>();
        UserSettings userSettings = settingsManager.getSettings(player);

        // Online players
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            buttons.add(ActionButton.builder(Component.text(online.getName()))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openChooseAmount(p, online.getName());
                    })).build());
        }

        // Custom added players from user settings
        for (String savedName : userSettings.getTeleportList()) {
            boolean already = buttons.stream().anyMatch(b -> b.label().toString().contains(savedName));
            if (!already && !savedName.equalsIgnoreCase(player.getName())) {
                buttons.add(ActionButton.builder(Component.text(savedName))
                        .action(DialogAction.customClick((view, audience) -> {
                            if (audience instanceof Player p) openChooseAmount(p, savedName);
                        })).build());
            }
        }

        // + Add Player to List
        buttons.add(ActionButton.builder(LanguageHook.getComponent("pay.btn_add_player", player))
                .tooltip(LanguageHook.getComponent("pay.btn_add_player_tooltip", player))
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
     * Opens text input to add player name to list
     */
    public void openAddPlayerInput(@NotNull Player player) {
        Component title = LanguageHook.getComponent("pay.add_title", player);
        Component body = LanguageHook.getComponent("pay.add_body", player);

        Dialog dialog = Dialog.create(builder -> builder
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .inputs(List.of(
                                DialogInput.text("player_name", TextDialogInput.builder()
                                        .label(LanguageHook.getComponent("pay.input_label", player))
                                        .maxLength(16)
                                        .build())
                        ))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(LanguageHook.getComponent("pay.btn_confirm_add", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        String targetName = view.getText("player_name");
                                        if (targetName != null && !targetName.isBlank()) {
                                            targetName = targetName.trim();
                                            settingsManager.getSettings(p).addTeleportPlayer(targetName);
                                            settingsManager.saveSettingsAsync(p.getUniqueId());
                                            openChooseAmount(p, targetName);
                                        } else {
                                            openPayPlayerList(p);
                                        }
                                    }
                                })).build(),
                        ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) openPayPlayerList(p);
                                })).build()
                ))
                .build()
        );

        player.showDialog(dialog);
    }

    /**
     * Choose an amount to pay (image copy 5.png)
     */
    public void openChooseAmount(@NotNull Player player, @NotNull String targetName) {
        Component title = LanguageHook.getComponent("pay.choose_title", player,
                Placeholder.unparsed("target", targetName));
        Component body = LanguageHook.getComponent("pay.choose_body", player);

        List<ActionButton> buttons = new ArrayList<>();

        // Preset amounts: $ 3, $ 15, $ 30, $ 100
        double[] amounts = {3.0, 15.0, 30.0, 100.0};
        for (double amt : amounts) {
            String label = "$ " + (int) amt;
            buttons.add(ActionButton.builder(MM.deserialize("<green>" + label + "</green>"))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            executePayment(p, targetName, amt);
                        }
                    })).build());
        }

        // Custom
        buttons.add(ActionButton.builder(LanguageHook.getComponent("pay.btn_custom", player))
                .tooltip(LanguageHook.getComponent("pay.btn_custom_tooltip", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        openCustomAmountInput(p, targetName);
                    }
                })).build());

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openPayPlayerList(p);
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
     * Opens Custom Amount text input
     */
    public void openCustomAmountInput(@NotNull Player player, @NotNull String targetName) {
        Component title = LanguageHook.getComponent("pay.custom_title", player,
                Placeholder.unparsed("target", targetName));
        Component body = LanguageHook.getComponent("pay.custom_body", player);

        Dialog dialog = Dialog.create(builder -> builder
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .inputs(List.of(
                                DialogInput.text("amount", TextDialogInput.builder()
                                        .label(LanguageHook.getComponent("pay.custom_label", player))
                                        .maxLength(10)
                                        .build())
                        ))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(LanguageHook.getComponent("pay.btn_pay", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        String raw = view.getText("amount");
                                        try {
                                            double amt = Double.parseDouble(raw);
                                            executePayment(p, targetName, amt);
                                        } catch (Throwable t) {
                                            LanguageHook.sendMessage(p, "pay.invalid_amount");
                                            openChooseAmount(p, targetName);
                                        }
                                    }
                                })).build(),
                        ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) openChooseAmount(p, targetName);
                                })).build()
                ))
                .build()
        );

        player.showDialog(dialog);
    }

    private void executePayment(@NotNull Player player, @NotNull String targetName, double amount) {
        if (amount <= 0) {
            LanguageHook.sendMessage(player, "pay.invalid_amount");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target.getName() == null) {
            LanguageHook.sendMessage(player, "general.player_not_found", Placeholder.unparsed("player", targetName));
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            LanguageHook.sendMessage(player, "pay.cannot_pay_self");
            return;
        }

        if (!VaultHook.hasEconomy()) {
            // Fallback command execution
            player.performCommand("pay " + targetName + " " + amount);
            return;
        }

        if (VaultHook.getBalance(player) < amount) {
            LanguageHook.sendMessage(player, "pay.not_enough_money");
            return;
        }

        boolean success = VaultHook.transfer(player, target, amount);
        if (success) {
            LanguageHook.sendMessage(player, "pay.success_sender",
                    Placeholder.unparsed("amount", VaultHook.formatMoney(amount)),
                    Placeholder.unparsed("target", target.getName()));
            if (target.isOnline() && target.getPlayer() != null) {
                LanguageHook.sendMessage(target.getPlayer(), "pay.success_receiver",
                        Placeholder.unparsed("amount", VaultHook.formatMoney(amount)),
                        Placeholder.unparsed("sender", player.getName()));
            }
        } else {
            LanguageHook.sendMessage(player, "pay.failed");
        }
    }
}
