package com.harukiyu.clarityessentials.dialog;

import com.harukiyu.clarityessentials.hook.LanguageHook;
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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FriendsDialogManager {

    private final UserSettingsManager settingsManager;
    private MenuDialogManager menuDialogManager;

    public FriendsDialogManager(@NotNull UserSettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    public void setMenuDialogManager(@NotNull MenuDialogManager menuDialogManager) {
        this.menuDialogManager = menuDialogManager;
    }

    /**
     * Opens Friends Menu (image copy 16.png)
     */
    public void openFriends(@NotNull Player player) {
        UserSettings s = settingsManager.getSettings(player);
        int friendsCount = s.getFriendsList().size();
        int followingCount = s.getFollowingList().size();

        Component title = LanguageHook.getComponent("friends.title", player,
                Placeholder.unparsed("friends", String.valueOf(friendsCount)),
                Placeholder.unparsed("following", String.valueOf(followingCount)));

        List<ActionButton> buttons = new ArrayList<>();

        // Filter
        buttons.add(ActionButton.builder(LanguageHook.getComponent("friends.btn_filter", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openFriends(p);
                })).build());

        // Search
        buttons.add(ActionButton.builder(LanguageHook.getComponent("friends.btn_search", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openFollowInput(p);
                })).build());

        // Existing friends or "No friends or following"
        if (s.getFriendsList().isEmpty() && s.getFollowingList().isEmpty()) {
            buttons.add(ActionButton.builder(LanguageHook.getComponent("friends.no_friends", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openFollowInput(p);
                    })).build());
        } else {
            for (String friend : s.getFriendsList()) {
                buttons.add(ActionButton.builder(Component.text(friend))
                        .action(DialogAction.customClick((view, audience) -> {
                            if (audience instanceof Player p) openFriends(p);
                        })).build());
            }
        }

        // + Follow
        buttons.add(ActionButton.builder(LanguageHook.getComponent("friends.btn_follow", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openFollowInput(p);
                })).build());

        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p && menuDialogManager != null) {
                        menuDialogManager.openDashboard(p);
                    }
                })).build();

        Dialog dialog = Dialog.create(builder -> builder
                .base(DialogBase.builder(title).build())
                .type(DialogType.multiAction(buttons, exitBtn, 3))
                .build()
        );

        player.showDialog(dialog);
    }

    /**
     * Opens Follow player input
     */
    public void openFollowInput(@NotNull Player player) {
        Component title = LanguageHook.getComponent("friends.follow_title", player);
        Component body = LanguageHook.getComponent("friends.follow_body", player);

        Dialog dialog = Dialog.create(builder -> builder
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .inputs(List.of(
                                DialogInput.text("player_name", TextDialogInput.builder()
                                        .label(LanguageHook.getComponent("friends.follow_label", player))
                                        .maxLength(16)
                                        .build())
                        ))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(LanguageHook.getComponent("friends.btn_confirm_follow", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) {
                                        String target = view.getText("player_name");
                                        if (target != null && !target.isBlank()) {
                                            settingsManager.getSettings(p).addFollowing(target.trim());
                                            settingsManager.saveSettingsAsync(p.getUniqueId());
                                        }
                                        openFriends(p);
                                    }
                                })).build(),
                        ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                                .action(DialogAction.customClick((view, audience) -> {
                                    if (audience instanceof Player p) openFriends(p);
                                })).build()
                ))
                .build()
        );

        player.showDialog(dialog);
    }
}
