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
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.harukiyu.clarityessentials.nickname.NicknameManager;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.List;

public class MenuDialogManager {

    private static final ClickCallback.Options DEFAULT_OPTIONS = ClickCallback.Options.builder().build();

    private final ConfigManager configManager;
    private final HomeDialogManager homeDialogManager;
    private final PayDialogManager payDialogManager;
    private final TeleportDialogManager teleportDialogManager;
    private final LeaderboardDialogManager leaderboardDialogManager;
    private final SettingsDialogManager settingsDialogManager;
    private final FriendsDialogManager friendsDialogManager;
    private final NicknameManager nicknameManager;

    public MenuDialogManager(@NotNull ConfigManager configManager,
                             @NotNull HomeDialogManager homeDialogManager,
                             @NotNull PayDialogManager payDialogManager,
                             @NotNull TeleportDialogManager teleportDialogManager,
                             @NotNull LeaderboardDialogManager leaderboardDialogManager,
                             @NotNull SettingsDialogManager settingsDialogManager,
                             @NotNull FriendsDialogManager friendsDialogManager,
                             @NotNull NicknameManager nicknameManager) {
        this.configManager = configManager;
        this.homeDialogManager = homeDialogManager;
        this.payDialogManager = payDialogManager;
        this.teleportDialogManager = teleportDialogManager;
        this.leaderboardDialogManager = leaderboardDialogManager;
        this.settingsDialogManager = settingsDialogManager;
        this.friendsDialogManager = friendsDialogManager;
        this.nicknameManager = nicknameManager;
    }

    public void wireSubManagers() {
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

        // Arranged in 2 columns (5 rows, 10 buttons):
        // Row 1: Homes (L), RTP (R)
        // Row 2: Auction (L), Friends (R)
        // Row 3: Webstore (L), Teleport (R)
        // Row 4: Pay (L), Stats (R)
        // Row 5: Leaderboards (L), Settings (R)
        List<ActionButton> buttons = new ArrayList<>();

        // Row 1
        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_homes", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) homeDialogManager.openHomeDialog(p);
                }, DEFAULT_OPTIONS)).build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_rtp", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                        executeSafeCommand(p, configManager.getCmdRtp());
                    }
                }, DEFAULT_OPTIONS)).build());

        // Row 2
        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_auction", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                        executeSafeCommand(p, configManager.getCmdAuction());
                    }
                }, DEFAULT_OPTIONS)).build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_friends", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) friendsDialogManager.openFriends(p);
                }, DEFAULT_OPTIONS)).build());

        // Row 3
        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_webstore", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                        openWebstoreLink(p, configManager.getWebstoreUrl());
                    }
                }, DEFAULT_OPTIONS)).build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_teleport", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) teleportDialogManager.openTeleportList(p);
                }, DEFAULT_OPTIONS)).build());

        // Row 4
        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_pay", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) payDialogManager.openPayPlayerList(p);
                }, DEFAULT_OPTIONS)).build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_stats", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        p.closeDialog();
                        executeSafeCommand(p, configManager.getCmdStats());
                    }
                }, DEFAULT_OPTIONS)).build());

        // Row 5
        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_leaderboards", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) leaderboardDialogManager.openCategories(p);
                }, DEFAULT_OPTIONS)).build());

        buttons.add(ActionButton.builder(LanguageHook.getComponent("menu.btn_settings", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) settingsDialogManager.openMainSettings(p);
                }, DEFAULT_OPTIONS)).build());

        // Close button
        ActionButton closeBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_close", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) p.closeDialog();
                }, DEFAULT_OPTIONS)).build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(buttons, closeBtn, 2))
        );

        player.showDialog(dialog);
    }

    /**
     * Sends an interactive clickable webstore link to player chat
     */
    public void openWebstoreLink(@NotNull Player player, @NotNull String rawUrl) {
        String url = rawUrl.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        if (configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), configManager.getSoundActionSuccess(), 1.0f, 1.0f);
        }

        String langCode = LanguageHook.getEffectiveLanguageCode(player);
        String raw = LanguageHook.getRawString("menu.open_link_chat", langCode);
        if (raw == null || raw.isBlank()) {
            raw = "<newline><green>----------------------------------------</green><newline><yellow><bold>  CLARITY NETWORK STORE</bold></yellow><newline><gray>  Visit our official webstore for ranks, items & perks:</gray><newline>  <click:open_url:'<url>'><hover:show_text:'<green>Click here to open <aqua><url></aqua> in your browser</green>'><gold><underlined><url></underlined></gold> <aqua><bold>[OPEN STORE LINK]</bold></aqua></hover></click><newline><green>----------------------------------------</green>";
        }

        // Remove any globe symbol or icon in title, per user request: "cukup CLARITY NETWORK STORE"
        raw = raw.replace("🌐 ", "").replace("🌐", "");

        // Directly substitute URL so MiniMessage parses <click:open_url:'...'> with real URL
        String resolved = raw.replace("{url}", url).replace("<url>", url);

        String prefix = LanguageHook.getRawString("prefix", langCode);
        if (prefix == null) prefix = "";

        Component message = MiniMessage.miniMessage().deserialize(resolved, 
            Placeholder.parsed("prefix", prefix)
        );

        player.sendMessage(message);
    }

    /**
     * Opens Native Text Input Dialog to change player nickname
     */
    public void openNickDialog(@NotNull Player player) {
        String currentNick = nicknameManager.getRawNickname(player.getUniqueId());
        if (currentNick == null) {
            currentNick = player.getName();
        }

        Component title = LanguageHook.getComponent("nickname.dialog_title", player);
        Component body = LanguageHook.getComponent("nickname.dialog_body", player);
        Component inputLabel = LanguageHook.getComponent("nickname.dialog_input_label", player);

        TextDialogInput textInput = DialogInput.text("nick_value", inputLabel)
                .initial(currentNick)
                .maxLength(configManager.getNickMaxLength())
                .build();

        ActionButton confirmBtn = ActionButton.builder(LanguageHook.getComponent("nickname.dialog_confirm", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        String newNick = view.getText("nick_value");
                        if (newNick == null || newNick.isBlank() || newNick.equalsIgnoreCase(p.getName()) || newNick.equalsIgnoreCase("off") || newNick.equalsIgnoreCase("reset")) {
                            nicknameManager.resetNickname(p);
                            LanguageHook.sendMessage(p, "nickname.reset");
                            p.closeDialog();
                            return;
                        }

                        NicknameManager.NickResult result = nicknameManager.setNickname(p, newNick.trim());
                        switch (result) {
                            case SUCCESS -> {
                                LanguageHook.sendMessage(p, "nickname.changed", Placeholder.component("nickname", nicknameManager.getDisplayName(p)));
                                p.closeDialog();
                            }
                            case TOO_SHORT -> LanguageHook.sendMessage(p, "nickname.too_short", Placeholder.unparsed("min", String.valueOf(configManager.getNickMinLength())));
                            case TOO_LONG -> LanguageHook.sendMessage(p, "nickname.too_long", Placeholder.unparsed("max", String.valueOf(configManager.getNickMaxLength())));
                            case INVALID_REGEX -> LanguageHook.sendMessage(p, "nickname.invalid_characters");
                            case ALREADY_IN_USE -> LanguageHook.sendMessage(p, "nickname.already_taken");
                            case COLOR_NO_PERMISSION -> LanguageHook.sendMessage(p, "nickname.no_color_permission");
                        }
                    }
                }, DEFAULT_OPTIONS)).build();

        ActionButton resetBtn = ActionButton.builder(LanguageHook.getComponent("nickname.dialog_reset", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        nicknameManager.resetNickname(p);
                        LanguageHook.sendMessage(p, "nickname.reset");
                        p.closeDialog();
                    }
                }, DEFAULT_OPTIONS)).build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .inputs(List.of(textInput))
                        .build())
                .type(DialogType.confirmation(confirmBtn, resetBtn))
        );

        player.showDialog(dialog);
    }

    private void executeSafeCommand(@NotNull Player player, @NotNull String command) {
        if (command.isBlank()) return;
        String cleanCmd = command.startsWith("/") ? command.substring(1) : command;
        try {
            player.performCommand(cleanCmd);
        } catch (Throwable t) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<prefix><gray>Command <yellow>/" + cleanCmd + "</yellow> could not be executed or is not installed.</gray>"));
        }
    }
}
