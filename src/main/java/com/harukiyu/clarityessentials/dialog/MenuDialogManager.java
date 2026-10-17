package com.harukiyu.clarityessentials.dialog;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MenuDialogManager {

    private final ConfigManager configManager;
    private final HomeDialogManager homeDialogManager;
    private final PayDialogManager payDialogManager;
    private final TeleportDialogManager teleportDialogManager;
    private final LeaderboardDialogManager leaderboardDialogManager;
    private final SettingsDialogManager settingsDialogManager;
    private final FriendsDialogManager friendsDialogManager;

    public MenuDialogManager(@NotNull ConfigManager configManager,
                             @NotNull HomeDialogManager homeDialogManager,
                             @NotNull PayDialogManager payDialogManager,
                             @NotNull TeleportDialogManager teleportDialogManager,
                             @NotNull LeaderboardDialogManager leaderboardDialogManager,
                             @NotNull SettingsDialogManager settingsDialogManager,
                             @NotNull FriendsDialogManager friendsDialogManager) {
        this.configManager = configManager;
        this.homeDialogManager = homeDialogManager;
        this.payDialogManager = payDialogManager;
        this.teleportDialogManager = teleportDialogManager;
        this.leaderboardDialogManager = leaderboardDialogManager;
        this.settingsDialogManager = settingsDialogManager;
        this.friendsDialogManager = friendsDialogManager;

        // Wire references for back-navigation
        this.homeDialogManager.setMenuDialogManager(this);
        this.payDialogManager.setMenuDialogManager(this);
        this.teleportDialogManager.setMenuDialogManager(this);
        this.leaderboardDialogManager.setMenuDialogManager(this);
        this.settingsDialogManager.setMenuDialogManager(this);
        this.friendsDialogManager.setMenuDialogManager(this);
    }

    /**
     * Opens the Centralized Native Player Dashboard dialog (image copy.png)
     */
    public void openDashboard(@NotNull Player player) {
        if (configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), configManager.getSoundDialogOpen(), 1.0f, 1.0f);
        }

        Component title = LanguageHook.getComponent("menu.title", player);

        // 14 Buttons arranged in 2 columns (7 rows) exactly matching image copy.png:
        // Left Column: Homes, Auction, Quick Buy, Orders, Sell, Teleport, Leaderboards
        // Right Column: RTP, RTP Queue, Friends, Shard Shop, Pay, Stats, Settings
        List<ActionButton> buttons = new ArrayList<>();

        // Row 1
        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_homes", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) homeDialogManager.openHomeDialog(p);
                })).build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_rtp", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                        p.performCommand(configManager.getCmdRtp());
                    }
                })).build());

        // Row 2
        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_auction", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                        p.performCommand(configManager.getCmdAuction());
                    }
                })).build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_rtp_queue", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                        p.performCommand(configManager.getCmdRtpQueue());
                    }
                })).build());

        // Row 3
        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_quick_buy", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                        p.performCommand(configManager.getCmdQuickBuy());
                    }
                })).build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_friends", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) friendsDialogManager.openFriends(p);
                })).build());

        // Row 4
        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_orders", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                        p.performCommand(configManager.getCmdOrders());
                    }
                })).build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_shard_shop", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                        p.performCommand(configManager.getCmdShardShop());
                    }
                })).build());

        // Row 5
        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_sell", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                        p.performCommand(configManager.getCmdSell());
                    }
                })).build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_pay", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) payDialogManager.openPayPlayerList(p);
                })).build());

        // Row 6
        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_teleport", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) teleportDialogManager.openTeleportList(p);
                })).build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_stats", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                        p.performCommand(configManager.getCmdStats());
                    }
                })).build());

        // Row 7
        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_leaderboards", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) leaderboardDialogManager.openCategories(p);
                })).build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_settings", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) settingsDialogManager.openMainSettings(p);
                })).build());

        // Close button
        ActionButton closeBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_close", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) p.closeDialog();
                })).build();

        Dialog dialog = Dialog.create(builder -> builder
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(buttons, closeBtn, 2))
                .build()
        );

        player.showDialog(dialog);
    }
}
