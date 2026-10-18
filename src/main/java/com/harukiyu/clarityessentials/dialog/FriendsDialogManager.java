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

public class FriendsDialogManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final ClickCallback.Options DEFAULT_OPTIONS = ClickCallback.Options.builder().build();

    private final ConfigManager configManager;
    private final UserSettingsManager settingsManager;
    private final TeleportManager teleportManager;
    private final PayDialogManager payDialogManager;
    private MenuDialogManager menuDialogManager;

    public FriendsDialogManager(@NotNull ConfigManager configManager,
                                @NotNull UserSettingsManager settingsManager,
                                @NotNull TeleportManager teleportManager,
                                @NotNull PayDialogManager payDialogManager) {
        this.configManager = configManager;
        this.settingsManager = settingsManager;
        this.teleportManager = teleportManager;
        this.payDialogManager = payDialogManager;
    }

    public FriendsDialogManager(@NotNull UserSettingsManager settingsManager,
                                @NotNull TeleportManager teleportManager,
                                @NotNull PayDialogManager payDialogManager) {
        this(null, settingsManager, teleportManager, payDialogManager);
    }

    public void setMenuDialogManager(@NotNull MenuDialogManager menuDialogManager) {
        this.menuDialogManager = menuDialogManager;
    }

    /**
     * Opens Friends Menu (image copy 16.png)
     */
    public void openFriends(@NotNull Player player) {
        openFriends(player, true);
    }

    public void openFriends(@NotNull Player player, boolean playSound) {
        if (playSound && configManager != null && configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), configManager.getSoundDialogOpen(), 1.0f, 1.0f);
        }

        UserSettings s = settingsManager.getSettings(player);
        int friendsCount = s.getFriendsList().size();

        Component title = LanguageHook.getComponent("friends.title", player,
                Placeholder.unparsed("count", String.valueOf(friendsCount)));

        List<ActionButton> buttons = new ArrayList<>();

        // + Add Friend Button
        buttons.add(ActionButton.builder(LanguageHook.getComponent("friends.btn_add_friend", player))
                .tooltip(LanguageHook.getComponent("friends.btn_add_friend_tooltip", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openAddFriendInput(p);
                }, DEFAULT_OPTIONS)).build());

        // Search Friend Button
        buttons.add(ActionButton.builder(LanguageHook.getComponent("friends.btn_search", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openSearchFriendInput(p);
                }, DEFAULT_OPTIONS)).build());

        // Existing Friends List or "No friends" placeholder
        if (s.getFriendsList().isEmpty()) {
            buttons.add(ActionButton.builder(LanguageHook.getComponent("friends.no_friends", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openAddFriendInput(p);
                    }, DEFAULT_OPTIONS)).build());
        } else {
            for (String friend : s.getFriendsList()) {
                Player friendOnline = Bukkit.getPlayerExact(friend);
                boolean isOnline = friendOnline != null && friendOnline.isOnline();
                String dot = isOnline ? "<green>●</green>" : "<gray>○</gray>";
                Component label = MM.deserialize("<white>" + friend + "</white> " + dot);

                buttons.add(ActionButton.builder(label)
                        .tooltip(MM.deserialize("<gray>Status: " + (isOnline ? "<green>Online</green>" : "<gray>Offline</gray>") + " - Click for options</gray>"))
                        .action(DialogAction.customClick((view, audience) -> {
                            if (audience instanceof Player p) openFriendDetail(p, friend, isOnline);
                        }, DEFAULT_OPTIONS)).build());
            }
        }

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
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 2))
        );

        player.showDialog(dialog);
    }

    /**
     * Opens Add Friend text input dialog with strict validation
     */
    public void openAddFriendInput(@NotNull Player player) {
        Component title = LanguageHook.getComponent("friends.add_title", player);
        Component body = LanguageHook.getComponent("friends.add_body", player);

        TextDialogInput textInput = DialogInput.text("player_name", LanguageHook.getComponent("friends.add_label", player))
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
                        ActionButton.builder(LanguageHook.getComponent("friends.btn_confirm_add", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        String raw = view.getText("player_name");
                                        PlayerValidator.ValidationResult res = PlayerValidator.validatePlayerName(p, raw);
                                        if (!res.valid()) {
                                            openFriends(p, false);
                                            return;
                                        }

                                        String targetName = res.resolvedName();
                                        if (targetName == null) {
                                            openFriends(p, false);
                                            return;
                                        }

                                        UserSettings s = settingsManager.getSettings(p);
                                        if (s.isFriend(targetName)) {
                                            LanguageHook.sendMessage(p, "general.already_added", Placeholder.unparsed("player", targetName));
                                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                            openFriends(p, false);
                                            return;
                                        }

                                        s.addFriend(targetName);
                                        settingsManager.saveSettingsAsync(p.getUniqueId());
                                        LanguageHook.sendMessage(p, "friends.added_success", Placeholder.unparsed("target", targetName));
                                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                                        openFriends(p, false);
                                    }
                                }, DEFAULT_OPTIONS)).build(),
                        ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) openFriends(p, false);
                                }, DEFAULT_OPTIONS)).build()
                ))
        );

        player.showDialog(dialog);
    }

    /**
     * Opens Search Friend text input dialog
     */
    public void openSearchFriendInput(@NotNull Player player) {
        Component title = LanguageHook.getComponent("friends.search_title", player);
        Component body = LanguageHook.getComponent("friends.search_body", player);

        TextDialogInput textInput = DialogInput.text("search_name", LanguageHook.getComponent("friends.search_label", player))
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
                        ActionButton.builder(LanguageHook.getComponent("friends.btn_confirm_search", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        String query = view.getText("search_name");
                                        if (query == null || query.isBlank()) {
                                            openFriends(p, false);
                                            return;
                                        }

                                        String trimmed = query.trim();
                                        UserSettings s = settingsManager.getSettings(p);
                                        String matchedFriend = s.getFriendsList().stream()
                                                .filter(f -> f.equalsIgnoreCase(trimmed))
                                                .findFirst()
                                                .orElse(null);

                                        if (matchedFriend != null) {
                                            Player friendOnline = Bukkit.getPlayerExact(matchedFriend);
                                            boolean isOnline = friendOnline != null && friendOnline.isOnline();
                                            openFriendDetail(p, matchedFriend, isOnline);
                                        } else {
                                            LanguageHook.sendMessage(p, "friends.search_not_found", Placeholder.unparsed("target", trimmed));
                                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                            openFriends(p, false);
                                        }
                                    }
                                }, DEFAULT_OPTIONS)).build(),
                        ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) openFriends(p, false);
                                }, DEFAULT_OPTIONS)).build()
                ))
        );

        player.showDialog(dialog);
    }

    /**
     * Opens Friend Detail dialog (Teleport, Pay, Remove Friend)
     */
    public void openFriendDetail(@NotNull Player player, @NotNull String friendName, boolean isOnline) {
        Component title = LanguageHook.getComponent("friends.friend_detail_title", player,
                Placeholder.unparsed("target", friendName));
        Component statusComponent = LanguageHook.getComponent(isOnline ? "friends.status_online" : "friends.status_offline", player);
        Component body = LanguageHook.getComponent("friends.friend_detail_body", player,
                Placeholder.component("status", statusComponent));

        List<ActionButton> buttons = new ArrayList<>();

        // 1. Teleport Request
        buttons.add(ActionButton.builder(LanguageHook.getComponent("friends.btn_tpa", player))
                .tooltip(LanguageHook.getComponent("friends.btn_tpa_tooltip", player, Placeholder.unparsed("target", friendName)))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        Player target = Bukkit.getPlayerExact(friendName);
                        if (target != null && target.isOnline()) {
                            teleportManager.sendTpa(p, target);
                            p.closeDialog();
                        } else {
                            LanguageHook.sendMessage(p, "friends.target_offline", Placeholder.unparsed("target", friendName));
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            openFriendDetail(p, friendName, false);
                        }
                    }
                }, DEFAULT_OPTIONS)).build());

        // 2. Send Money (Pay)
        buttons.add(ActionButton.builder(LanguageHook.getComponent("friends.btn_pay", player))
                .tooltip(LanguageHook.getComponent("friends.btn_pay_tooltip", player, Placeholder.unparsed("target", friendName)))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        payDialogManager.openChooseAmount(p, friendName);
                    }
                }, DEFAULT_OPTIONS)).build());

        // 3. Remove Friend
        buttons.add(ActionButton.builder(LanguageHook.getComponent("friends.btn_remove_friend", player))
                .tooltip(LanguageHook.getComponent("friends.btn_remove_friend_tooltip", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        openConfirmRemoveFriend(p, friendName);
                    }
                }, DEFAULT_OPTIONS)).build());

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openFriends(p, false);
                }, DEFAULT_OPTIONS)).build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 1))
        );

        player.showDialog(dialog);
    }

    /**
     * Confirmation dialog to remove a friend
     */
    public void openConfirmRemoveFriend(@NotNull Player player, @NotNull String friendName) {
        Component title = LanguageHook.getComponent("friends.confirm_remove_title", player);
        Component body = LanguageHook.getComponent("friends.confirm_remove_body", player,
                Placeholder.unparsed("target", friendName));

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(LanguageHook.getComponent("friends.btn_confirm_remove", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        settingsManager.getSettings(p).removeFriend(friendName);
                                        settingsManager.saveSettingsAsync(p.getUniqueId());
                                        LanguageHook.sendMessage(p, "friends.removed_success", Placeholder.unparsed("target", friendName));
                                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                                        openFriends(p, false);
                                    }
                                }, DEFAULT_OPTIONS)).build(),
                        ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        boolean isOnline = Bukkit.getPlayerExact(friendName) != null;
                                        openFriendDetail(p, friendName, isOnline);
                                    }
                                }, DEFAULT_OPTIONS)).build()
                ))
        );

        player.showDialog(dialog);
    }
}
