package com.harukiyu.clarityessentials.dialog;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.leaderboard.LeaderboardManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardDialogManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final ClickCallback.Options DEFAULT_OPTIONS = ClickCallback.Options.builder().build();

    private final ConfigManager configManager;
    private final LeaderboardManager leaderboardManager;
    private MenuDialogManager menuDialogManager;

    public LeaderboardDialogManager(@NotNull ConfigManager configManager,
                                    @NotNull LeaderboardManager leaderboardManager) {
        this.configManager = configManager;
        this.leaderboardManager = leaderboardManager;
    }

    public LeaderboardDialogManager(@NotNull LeaderboardManager leaderboardManager) {
        this(null, leaderboardManager);
    }

    public void setMenuDialogManager(@NotNull MenuDialogManager menuDialogManager) {
        this.menuDialogManager = menuDialogManager;
    }

    /**
     * Opens Leaderboard Categories Menu (image copy 6.png)
     */
    public void openCategories(@NotNull Player player) {
        openCategories(player, true);
    }

    public void openCategories(@NotNull Player player, boolean playSound) {
        if (playSound && configManager != null && configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), configManager.getSoundDialogOpen(), 1.0f, 1.0f);
        }

        Component title = LanguageHook.getComponent("leaderboards.title", player);

        List<ActionButton> buttons = new ArrayList<>();

        // 1. Money
        buttons.add(ActionButton.builder(LanguageHook.getComponent("leaderboards.btn_money", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openLeaderboard(p, "money", "Top Money");
                }, DEFAULT_OPTIONS)).build());

        // 2. Deaths
        buttons.add(ActionButton.builder(LanguageHook.getComponent("leaderboards.btn_deaths", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openLeaderboard(p, "deaths", "Top Deaths");
                }, DEFAULT_OPTIONS)).build());

        // 3. Playtime
        buttons.add(ActionButton.builder(LanguageHook.getComponent("leaderboards.btn_playtime", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openLeaderboard(p, "playtime", "Top Playtime");
                }, DEFAULT_OPTIONS)).build());

        // 4. Blocks Placed
        buttons.add(ActionButton.builder(LanguageHook.getComponent("leaderboards.btn_blocks_placed", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openLeaderboard(p, "blocks_placed", "Top Blocks Placed");
                }, DEFAULT_OPTIONS)).build());

        // 5. Blocks Broken
        buttons.add(ActionButton.builder(LanguageHook.getComponent("leaderboards.btn_blocks_broken", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openLeaderboard(p, "blocks_broken", "Top Blocks Broken");
                }, DEFAULT_OPTIONS)).build());

        // 6. Mobs Killed
        buttons.add(ActionButton.builder(LanguageHook.getComponent("leaderboards.btn_mobs_killed", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openLeaderboard(p, "mobs_killed", "Top Mobs Killed");
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
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 3))
        );

        player.showDialog(dialog);
    }

    /**
     * Opens Leaderboard Category View (image copy 7.png & image copy 8.png)
     */
    public void openLeaderboard(@NotNull Player player, @NotNull String category, @NotNull String categoryTitle) {
        Component title = LanguageHook.getComponent("leaderboards.category_title", player,
                Placeholder.unparsed("category", categoryTitle));

        List<LeaderboardManager.Entry> top = leaderboardManager.getTopEntries(category, 10);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < top.size(); i++) {
            LeaderboardManager.Entry e = top.get(i);
            int rank = i + 1;
            String rankColor = switch (rank) {
                case 1 -> "<gold><bold>#1</bold></gold>";
                case 2 -> "<white><bold>#2</bold></white>";
                case 3 -> "<gold>#3</gold>";
                default -> "<gray>#" + rank + "</gray>";
            };
            sb.append(rankColor).append(" <white>").append(e.name()).append("</white>")
                    .append(" <dark_gray>-</dark_gray> <green>").append(e.formattedValue()).append("</green>\n");
        }

        Component bodyComp = MM.deserialize(sb.toString().trim());

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(ActionButton.builder(LanguageHook.getComponent("leaderboards.btn_view_full", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openCategories(p, false);
                }, DEFAULT_OPTIONS)).build());

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openCategories(p, false);
                }, DEFAULT_OPTIONS)).build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                        .body(List.of(DialogBody.plainMessage(bodyComp)))
                        .build())
                .type(DialogType.confirmation(backBtn, buttons.get(0)))
        );

        player.showDialog(dialog);
    }
}
