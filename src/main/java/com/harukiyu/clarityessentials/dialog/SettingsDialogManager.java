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
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SettingsDialogManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final ClickCallback.Options DEFAULT_OPTIONS = ClickCallback.Options.builder().build();

    private final ConfigManager configManager;
    private final UserSettingsManager settingsManager;
    private final TeleportManager teleportManager;
    private MenuDialogManager menuDialogManager;

    public SettingsDialogManager(@NotNull ConfigManager configManager,
                                 @NotNull UserSettingsManager settingsManager,
                                 @NotNull TeleportManager teleportManager) {
        this.configManager = configManager;
        this.settingsManager = settingsManager;
        this.teleportManager = teleportManager;
    }

    public void setMenuDialogManager(@NotNull MenuDialogManager menuDialogManager) {
        this.menuDialogManager = menuDialogManager;
    }

    private void executeSafeCommand(@NotNull Player player, @NotNull String command) {
        if (command.isBlank()) return;
        String cleanCmd = command.startsWith("/") ? command.substring(1) : command;
        try {
            player.performCommand(cleanCmd);
        } catch (CommandException | IllegalArgumentException | IllegalStateException e) {
            player.sendMessage(MM.deserialize("<prefix><gray>Command <yellow>/" + cleanCmd + "</yellow> could not be executed or is not installed.</gray>"));
        }
    }

    /**
     * Opens Main Settings Category Menu (image copy 9.png)
     */
    public void openMainSettings(@NotNull Player player) {
        openMainSettings(player, true);
    }

    public void openMainSettings(@NotNull Player player, boolean playSound) {
        if (playSound && configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), configManager.getSoundDialogOpen(), 1.0f, 1.0f);
        }

        Component title = LanguageHook.getComponent("settings.title", player);
        Component body = LanguageHook.getComponent("settings.body", player);

        List<ActionButton> buttons = new ArrayList<>();

        // Chat
        buttons.add(ActionButton.builder(LanguageHook.getComponent("settings.btn_chat", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openChatSettings(p);
                }, DEFAULT_OPTIONS)).build());

        // Notifications
        buttons.add(ActionButton.builder(LanguageHook.getComponent("settings.btn_notifications", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openNotificationSettings(p);
                }, DEFAULT_OPTIONS)).build());

        // PvP
        buttons.add(ActionButton.builder(LanguageHook.getComponent("settings.btn_pvp", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        executeSafeCommand(p, configManager.getCmdPvp());
                        openMainSettings(p, false);
                    }
                }, DEFAULT_OPTIONS)).build());

        // Visuals
        buttons.add(ActionButton.builder(LanguageHook.getComponent("settings.btn_visuals", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openMainSettings(p, false);
                }, DEFAULT_OPTIONS)).build());

        // Privacy
        buttons.add(ActionButton.builder(LanguageHook.getComponent("settings.btn_privacy", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openPrivacySettings(p);
                }, DEFAULT_OPTIONS)).build());

        // Scoreboard
        buttons.add(ActionButton.builder(LanguageHook.getComponent("settings.btn_scoreboard", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        executeSafeCommand(p, configManager.getCmdScoreboard());
                        openMainSettings(p, false);
                    }
                }, DEFAULT_OPTIONS)).build());

        // General (Language & Nickname)
        buttons.add(ActionButton.builder(LanguageHook.getComponent("settings.btn_general", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) LanguageHook.openLanguageDialog(p, false);
                }, DEFAULT_OPTIONS)).build());

        // Back
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
     * Opens Settings - Chat (image copy 10.png)
     */
    public void openChatSettings(@NotNull Player player) {
        UserSettings s = settingsManager.getSettings(player);

        Component title = LanguageHook.getComponent("settings.chat_title", player);
        List<ActionButton> buttons = new ArrayList<>();

        // Public Chat: ON / OFF
        buttons.add(ActionButton.builder(MM.deserialize("<white>Public Chat: </white>" + formatBool(s.isPublicChat())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setPublicChat(!s.isPublicChat());
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openChatSettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        // Private Messages: Anyone / Friends / Off
        buttons.add(ActionButton.builder(MM.deserialize("<white>Private Messages: </white>" + formatState(s.getPrivateMessages())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setPrivateMessages(cycleState(s.getPrivateMessages()));
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openChatSettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        // Server Chat Messages: ON / OFF
        buttons.add(ActionButton.builder(MM.deserialize("<white>Server Chat Messages: </white>" + formatBool(s.isServerChatMessages())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setServerChatMessages(!s.isServerChatMessages());
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openChatSettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        // Server Hotbar Messages: ON / OFF
        buttons.add(ActionButton.builder(MM.deserialize("<white>Server Hotbar Messages: </white>" + formatBool(s.isServerHotbarMessages())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setServerHotbarMessages(!s.isServerHotbarMessages());
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openChatSettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        // Death Messages: Anyone / Friends / Off
        buttons.add(ActionButton.builder(MM.deserialize("<white>Death Messages: </white>" + formatState(s.getDeathMessages())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setDeathMessages(cycleState(s.getDeathMessages()));
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openChatSettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        // Advancement Messages: Anyone / Friends / Off
        buttons.add(ActionButton.builder(MM.deserialize("<white>Advancement Messages: </white>" + formatState(s.getAdvancementMessages())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setAdvancementMessages(cycleState(s.getAdvancementMessages()));
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openChatSettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        // Join/Leave Messages: Anyone / Friends / Off
        buttons.add(ActionButton.builder(MM.deserialize("<white>Join/Leave Messages: </white>" + formatState(s.getJoinLeaveMessages())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setJoinLeaveMessages(cycleState(s.getJoinLeaveMessages()));
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openChatSettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openMainSettings(p, false);
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
     * Opens Settings - Privacy (image copy 11.png)
     */
    public void openPrivacySettings(@NotNull Player player) {
        UserSettings s = settingsManager.getSettings(player);

        Component title = LanguageHook.getComponent("settings.privacy_title", player);
        List<ActionButton> buttons = new ArrayList<>();

        // Teleport Requests: Anyone / Friends / Off
        buttons.add(ActionButton.builder(MM.deserialize("<white>Teleport Requests: </white>" + formatState(s.getTeleportRequests())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        String next = cycleState(s.getTeleportRequests());
                        s.setTeleportRequests(next);
                        teleportManager.setTpaEnabled(p, !"Off".equalsIgnoreCase(next));
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openPrivacySettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        // Teleport-Here Requests: Anyone / Friends / Off
        buttons.add(ActionButton.builder(MM.deserialize("<white>Teleport-Here Requests: </white>" + formatState(s.getTeleportHereRequests())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setTeleportHereRequests(cycleState(s.getTeleportHereRequests()));
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openPrivacySettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        // Allow Payments: Anyone / Friends / Off
        buttons.add(ActionButton.builder(MM.deserialize("<white>Allow Payments: </white>" + formatState(s.getAllowPayments())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setAllowPayments(cycleState(s.getAllowPayments()));
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openPrivacySettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        // Randomized Coords: ON / OFF
        buttons.add(ActionButton.builder(MM.deserialize("<white>Randomized Coords: </white>" + formatBool(s.isRandomizedCoords())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setRandomizedCoords(!s.isRandomizedCoords());
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openPrivacySettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        // Private Transactions: ON / OFF
        buttons.add(ActionButton.builder(MM.deserialize("<white>Private Transactions: </white>" + formatBool(s.isPrivateTransactions())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setPrivateTransactions(!s.isPrivateTransactions());
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openPrivacySettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openMainSettings(p, false);
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
     * Opens Settings - Notifications (image copy 12.png)
     */
    public void openNotificationSettings(@NotNull Player player) {
        UserSettings s = settingsManager.getSettings(player);

        Component title = LanguageHook.getComponent("settings.notifications_title", player);
        List<ActionButton> buttons = new ArrayList<>();

        buttons.add(ActionButton.builder(MM.deserialize("<white>Pay Alerts: </white>" + formatBool(s.isPayAlerts())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setPayAlerts(!s.isPayAlerts());
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openNotificationSettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        buttons.add(ActionButton.builder(MM.deserialize("<white>Teleport Alerts: </white>" + formatBool(s.isTeleportAlerts())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setTeleportAlerts(!s.isTeleportAlerts());
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openNotificationSettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        buttons.add(ActionButton.builder(MM.deserialize("<white>Bounty Alerts: </white>" + formatBool(s.isBountyAlerts())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setBountyAlerts(!s.isBountyAlerts());
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openNotificationSettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        buttons.add(ActionButton.builder(MM.deserialize("<white>Auction Alerts: </white>" + formatBool(s.isAuctionAlerts())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setAuctionAlerts(!s.isAuctionAlerts());
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openNotificationSettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        buttons.add(ActionButton.builder(MM.deserialize("<white>Order Alerts: </white>" + formatBool(s.isOrderAlerts())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setOrderAlerts(!s.isOrderAlerts());
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openNotificationSettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        buttons.add(ActionButton.builder(MM.deserialize("<white>Server Sounds: </white>" + formatBool(s.isServerSounds())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setServerSounds(!s.isServerSounds());
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openNotificationSettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        buttons.add(ActionButton.builder(MM.deserialize("<white>Follow Alerts: </white>" + formatBool(s.isFollowAlerts())))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        s.setFollowAlerts(!s.isFollowAlerts());
                        settingsManager.saveSettingsAsync(p.getUniqueId());
                        openNotificationSettings(p);
                    }
                }, DEFAULT_OPTIONS)).build());

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openMainSettings(p, false);
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

    private String formatBool(boolean val) {
        return val ? "<green>ON</green>" : "<red>OFF</red>";
    }

    private String formatState(String state) {
        if ("Off".equalsIgnoreCase(state)) {
            return "<red>OFF</red>";
        } else if ("Friends".equalsIgnoreCase(state) || "Friends/Followed".equalsIgnoreCase(state)) {
            return "<yellow>Friends</yellow>";
        }
        return "<green>Anyone</green>";
    }

    private String cycleState(String current) {
        if ("Anyone".equalsIgnoreCase(current)) return "Friends";
        if ("Friends".equalsIgnoreCase(current)) return "Off";
        return "Anyone";
    }
}
