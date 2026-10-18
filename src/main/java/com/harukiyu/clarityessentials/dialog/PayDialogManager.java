package com.harukiyu.clarityessentials.dialog;

import com.harukiyu.clarityessentials.ClarityEssentials;
import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.hook.VaultHook;
import com.harukiyu.clarityessentials.settings.UserSettings;
import com.harukiyu.clarityessentials.settings.UserSettingsManager;
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
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class PayDialogManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final ClickCallback.Options DEFAULT_OPTIONS = ClickCallback.Options.builder().build();

    private final UserSettingsManager settingsManager;
    private MenuDialogManager menuDialogManager;

    public PayDialogManager(@NotNull ClarityEssentials plugin,
                            @NotNull ConfigManager configManager,
                            @NotNull UserSettingsManager settingsManager) {
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
        Set<String> displayed = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        // 1. Online players
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            displayed.add(online.getName());
            Component label = MM.deserialize("<white>" + online.getName() + "</white> <green>●</green>");
            buttons.add(ActionButton.builder(label)
                    .tooltip(MM.deserialize("<gray>Status: <green>Online</green> - Click to pay</gray>"))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openChooseAmount(p, online.getName());
                    }, DEFAULT_OPTIONS)).build());
        }

        // 2. Custom added players in pay list (offline ones)
        for (String savedName : userSettings.getPayList()) {
            if (!displayed.contains(savedName) && !savedName.equalsIgnoreCase(player.getName())) {
                displayed.add(savedName);
                Component label = MM.deserialize("<white>" + savedName + "</white> <gray>○</gray>");
                buttons.add(ActionButton.builder(label)
                        .tooltip(MM.deserialize("<gray>Status: <gray>Offline</gray> - Saved in pay list</gray>"))
                        .action(DialogAction.customClick((view, audience) -> {
                            if (audience instanceof Player p) openChooseAmount(p, savedName);
                        }, DEFAULT_OPTIONS)).build());
            }
        }

        // + Add Player to List
        buttons.add(ActionButton.builder(LanguageHook.getComponent("pay.btn_add_player", player))
                .tooltip(LanguageHook.getComponent("pay.btn_add_player_tooltip", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openAddPlayerInput(p);
                }, DEFAULT_OPTIONS)).build());

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p && menuDialogManager != null) {
                        menuDialogManager.openDashboard(p);
                    }
                }, DEFAULT_OPTIONS)).build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 2))
        );

        player.showDialog(dialog);
    }

    /**
     * Opens text input to add player name to pay list
     */
    public void openAddPlayerInput(@NotNull Player player) {
        Component title = LanguageHook.getComponent("pay.add_title", player);
        Component body = LanguageHook.getComponent("pay.add_body", player);

        TextDialogInput textInput = DialogInput.text("player_name", LanguageHook.getComponent("pay.input_label", player))
                .maxLength(16)
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .inputs(List.of(textInput))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(LanguageHook.getComponent("pay.btn_confirm_add", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        String raw = view.getText("player_name");
                                        PlayerValidator.ValidationResult res = PlayerValidator.validatePlayerName(p, raw);
                                        if (!res.valid()) {
                                            openPayPlayerList(p);
                                            return;
                                        }

                                        String targetName = res.resolvedName();
                                        if (targetName == null) {
                                            openPayPlayerList(p);
                                            return;
                                        }

                                        UserSettings s = settingsManager.getSettings(p);
                                        if (s.isPayPlayer(targetName)) {
                                            LanguageHook.sendMessage(p, "general.already_added", Placeholder.unparsed("player", targetName));
                                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                            openPayPlayerList(p);
                                            return;
                                        }

                                        s.addPayPlayer(targetName);
                                        settingsManager.saveSettingsAsync(p.getUniqueId());
                                        LanguageHook.sendMessage(p, "pay.player_added", Placeholder.unparsed("player", targetName));
                                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                                        openChooseAmount(p, targetName);
                                    }
                                }, DEFAULT_OPTIONS)).build(),
                        ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) openPayPlayerList(p);
                                }, DEFAULT_OPTIONS)).build()
                ))
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
        UserSettings userSettings = settingsManager.getSettings(player);

        // Preset amounts: $ 3, $ 15, $ 30, $ 100
        double[] amounts = {3.0, 15.0, 30.0, 100.0};
        for (double amt : amounts) {
            String label = "$ " + (int) amt;
            buttons.add(ActionButton.builder(MM.deserialize("<green>" + label + "</green>"))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            executePayment(p, targetName, amt);
                        }
                    }, DEFAULT_OPTIONS)).build());
        }

        // Custom
        buttons.add(ActionButton.builder(LanguageHook.getComponent("pay.btn_custom", player))
                .tooltip(LanguageHook.getComponent("pay.btn_custom_tooltip", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        openCustomAmountInput(p, targetName);
                    }
                }, DEFAULT_OPTIONS)).build());

        // Remove from List (if present in user's payList)
        if (userSettings.isPayPlayer(targetName)) {
            buttons.add(ActionButton.builder(LanguageHook.getComponent("pay.btn_remove", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openConfirmRemovePay(p, targetName);
                    }, DEFAULT_OPTIONS)).build());
        }

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openPayPlayerList(p);
                }, DEFAULT_OPTIONS)).build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 2))
        );

        player.showDialog(dialog);
    }

    /**
     * Confirmation dialog to remove player from pay list
     */
    public void openConfirmRemovePay(@NotNull Player player, @NotNull String targetName) {
        Component title = LanguageHook.getComponent("pay.confirm_remove_title", player);
        Component body = LanguageHook.getComponent("pay.confirm_remove_body", player,
                Placeholder.unparsed("target", targetName));

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(LanguageHook.getComponent("pay.btn_confirm_remove", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        settingsManager.getSettings(p).removePayPlayer(targetName);
                                        settingsManager.saveSettingsAsync(p.getUniqueId());
                                        LanguageHook.sendMessage(p, "pay.player_removed", Placeholder.unparsed("player", targetName));
                                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                                        openPayPlayerList(p);
                                    }
                                }, DEFAULT_OPTIONS)).build(),
                        ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) openChooseAmount(p, targetName);
                                }, DEFAULT_OPTIONS)).build()
                ))
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

        TextDialogInput textInput = DialogInput.text("amount", LanguageHook.getComponent("pay.custom_label", player))
                .maxLength(10)
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .inputs(List.of(textInput))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(LanguageHook.getComponent("pay.btn_pay", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        String raw = view.getText("amount");
                                        try {
                                            double amt = Double.parseDouble(raw);
                                            executePayment(p, targetName, amt);
                                        } catch (NumberFormatException t) {
                                            LanguageHook.sendMessage(p, "pay.invalid_amount");
                                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                            openChooseAmount(p, targetName);
                                        }
                                    }
                                }, DEFAULT_OPTIONS)).build(),
                        ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) openChooseAmount(p, targetName);
                                }, DEFAULT_OPTIONS)).build()
                ))
        );

        player.showDialog(dialog);
    }

    private void executePayment(@NotNull Player player, @NotNull String targetName, double amount) {
        if (amount <= 0) {
            LanguageHook.sendMessage(player, "pay.invalid_amount");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target.getName() == null) {
            LanguageHook.sendMessage(player, "general.player_not_found", Placeholder.unparsed("player", targetName));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            LanguageHook.sendMessage(player, "pay.cannot_pay_self");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Privacy setting check for receiver
        UserSettings targetSettings = settingsManager.getSettings(target.getUniqueId());
        String allowPay = targetSettings.getAllowPayments();
        if ("Off".equalsIgnoreCase(allowPay)) {
            LanguageHook.sendMessage(player, "pay.disabled_target", Placeholder.unparsed("target", targetName));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        if ("Friends".equalsIgnoreCase(allowPay) && !targetSettings.isFriend(player.getName())) {
            LanguageHook.sendMessage(player, "pay.friends_only", Placeholder.unparsed("target", targetName));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (!VaultHook.hasEconomy()) {
            try {
                player.performCommand("pay " + targetName + " " + amount);
            } catch (CommandException | IllegalArgumentException | IllegalStateException e) {
                LanguageHook.sendMessage(player, "pay.economy_not_found");
            }
            return;
        }

        if (VaultHook.getBalance(player) < amount) {
            LanguageHook.sendMessage(player, "pay.not_enough_money");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        boolean success = VaultHook.transfer(player, target, amount);
        if (success) {
            LanguageHook.sendMessage(player, "pay.success_sender",
                    Placeholder.unparsed("amount", VaultHook.formatMoney(amount)),
                    Placeholder.unparsed("target", target.getName()));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            if (target.isOnline() && target.getPlayer() != null) {
                if (targetSettings.isPayAlerts()) {
                    LanguageHook.sendMessage(target.getPlayer(), "pay.success_receiver",
                            Placeholder.unparsed("amount", VaultHook.formatMoney(amount)),
                            Placeholder.unparsed("sender", player.getName()));
                    target.getPlayer().playSound(target.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
            }
        } else {
            LanguageHook.sendMessage(player, "pay.failed");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }
}
