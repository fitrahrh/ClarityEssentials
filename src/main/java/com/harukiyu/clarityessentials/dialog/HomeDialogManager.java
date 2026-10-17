package com.harukiyu.clarityessentials.dialog;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.home.Home;
import com.harukiyu.clarityessentials.home.HomeManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HomeDialogManager {

    private static final ClickCallback.Options DEFAULT_OPTIONS = ClickCallback.Options.builder().build();

    private final ConfigManager configManager;
    private final HomeManager homeManager;
    private MenuDialogManager menuDialogManager;

    public HomeDialogManager(@NotNull ConfigManager configManager, @NotNull HomeManager homeManager) {
        this.configManager = configManager;
        this.homeManager = homeManager;
    }

    public void setMenuDialogManager(@NotNull MenuDialogManager menuDialogManager) {
        this.menuDialogManager = menuDialogManager;
    }

    /**
     * Opens the main Paper Native Dialog for home management.
     */
    public void openHomeDialog(@NotNull Player player) {
        if (configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), configManager.getSoundDialogOpen(), 1.0f, 1.0f);
        }

        Collection<Home> homes = homeManager.getHomes(player);
        int maxHomes = homeManager.getMaxHomes(player);
        String maxStr = maxHomes == Integer.MAX_VALUE ? "∞" : String.valueOf(maxHomes);

        // Dialog Title
        Component title = LanguageHook.getComponent("homes.dialog_title", player);

        // Unified Dialog Body
        Component bodyComponent = LanguageHook.getComponent("homes.dialog_body", player,
                Placeholder.unparsed("current", String.valueOf(homes.size())),
                Placeholder.unparsed("max", maxStr)
        );
        List<DialogBody> bodies = List.of(DialogBody.plainMessage(bodyComponent));

        // MultiAction Buttons matching Donut SMP (image copy 2.png & image copy 3.png)
        List<ActionButton> buttons = new ArrayList<>();

        // 1. Add button for each saved home
        for (Home home : homes) {
            Component label = LanguageHook.getComponent("homes.dialog_btn_home", player,
                    Placeholder.unparsed("name", home.getName())
            );
            Component tooltip = LanguageHook.getComponent("homes.dialog_btn_home_tooltip", player,
                    Placeholder.unparsed("world", home.getWorldName()),
                    Placeholder.unparsed("coords", home.getFormattedCoordinates())
            );

            ActionButton btn = ActionButton.builder(label)
                    .tooltip(tooltip)
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            p.closeDialog();
                            homeManager.teleportHome(p, home.getName());
                        }
                    }, DEFAULT_OPTIONS))
                    .build();
            buttons.add(btn);
        }

        // 2. Add "New Home" slots for remaining available home capacity (up to displayed min slots)
        int availableNewSlots = Math.max(0, maxHomes - homes.size());
        int displayNewSlots = Math.min(availableNewSlots, 3);
        if (homes.isEmpty() && displayNewSlots == 0) displayNewSlots = 1;

        for (int i = 0; i < displayNewSlots; i++) {
            ActionButton newSlotBtn = ActionButton.builder(LanguageHook.getComponent("homes.btn_new_home", player))
                    .tooltip(LanguageHook.getComponent("homes.btn_new_home_tooltip", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            openSetHomeInputDialog(p);
                        }
                    }, DEFAULT_OPTIONS))
                    .build();
            buttons.add(newSlotBtn);
        }

        // 3. Add "Locked" slots preview (image copy 3.png)
        int lockedSlots = (maxHomes < 6 && maxHomes != Integer.MAX_VALUE) ? (6 - Math.min(6, homes.size() + displayNewSlots)) : 0;
        for (int i = 0; i < Math.min(lockedSlots, 3); i++) {
            ActionButton lockedBtn = ActionButton.builder(LanguageHook.getComponent("homes.btn_locked", player))
                    .tooltip(LanguageHook.getComponent("homes.btn_locked_tooltip", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            LanguageHook.sendMessage(p, "homes.locked_notice");
                        }
                    }, DEFAULT_OPTIONS))
                    .build();
            buttons.add(lockedBtn);
        }

        // 4. Delete Homes / Manage Homes
        if (!homes.isEmpty()) {
            ActionButton manageBtn = ActionButton.builder(LanguageHook.getComponent("homes.dialog_btn_manage", player))
                    .tooltip(LanguageHook.getComponent("homes.dialog_btn_manage_tooltip", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            openDeleteSelectorDialog(p);
                        }
                    }, DEFAULT_OPTIONS))
                    .build();
            buttons.add(manageBtn);
        }

        // 5. Back to Main Menu Button
        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p && menuDialogManager != null) {
                        menuDialogManager.openDashboard(p);
                    } else if (audience instanceof Player p) {
                        p.closeDialog();
                    }
                }, DEFAULT_OPTIONS))
                .build();

        int columns = buttons.size() >= 4 ? 4 : Math.max(1, buttons.size());
        Dialog homeDialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .body(bodies)
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, columns))
        );

        player.showDialog(homeDialog);
    }

    /**
     * Opens the Native Dialog with a Text Input allowing the player to name their home.
     */
    public void openSetHomeInputDialog(@NotNull Player player) {
        Collection<Home> current = homeManager.getHomes(player);
        int max = homeManager.getMaxHomes(player);

        if (current.size() >= max) {
            LanguageHook.sendMessage(player, "homes.limit_reached", Placeholder.unparsed("limit", String.valueOf(max)));
            player.closeDialog();
            return;
        }

        Component title = LanguageHook.getComponent("homes.dialog_sethome_input_title", player);
        Component body = LanguageHook.getComponent("homes.dialog_sethome_input_body", player);
        Component inputLabel = LanguageHook.getComponent("homes.dialog_sethome_input_label", player);

        // Determine smart default name
        String defaultName = "home";
        if (homeManager.getHome(player, defaultName) != null) {
            int index = 2;
            while (homeManager.getHome(player, "home" + index) != null) {
                index++;
            }
            defaultName = "home" + index;
        }

        TextDialogInput textInput = DialogInput.text("home_name", inputLabel)
                .initial(defaultName)
                .maxLength(32)
                .build();

        ActionButton confirmBtn = ActionButton.builder(LanguageHook.getComponent("homes.dialog_sethome_input_confirm", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        String homeName = view.getText("home_name");
                        if (homeName == null || homeName.isBlank()) {
                            homeName = "home";
                        }
                        homeName = homeName.trim();
                        HomeManager.SetHomeResult result = homeManager.setHome(p, homeName);
                        switch (result) {
                            case SUCCESS -> {
                                LanguageHook.sendMessage(p, "homes.set_success", Placeholder.unparsed("home", homeName));
                                openHomeDialog(p);
                            }
                            case LIMIT_REACHED -> {
                                LanguageHook.sendMessage(p, "homes.limit_reached", Placeholder.unparsed("limit", String.valueOf(max)));
                                p.closeDialog();
                            }
                            case INVALID_NAME -> {
                                LanguageHook.sendMessage(p, "homes.invalid_name");
                                openSetHomeInputDialog(p);
                            }
                        }
                    }
                }, DEFAULT_OPTIONS))
                .build();

        ActionButton cancelBtn = ActionButton.builder(LanguageHook.getComponent("homes.dialog_btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        openHomeDialog(p);
                    }
                }, DEFAULT_OPTIONS))
                .build();

        Dialog inputDialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .inputs(List.of(textInput))
                        .build())
                .type(DialogType.confirmation(confirmBtn, cancelBtn))
        );

        player.showDialog(inputDialog);
    }

    /**
     * Opens the Delete Selector Dialog to pick which home to delete.
     */
    public void openDeleteSelectorDialog(@NotNull Player player) {
        Collection<Home> homes = homeManager.getHomes(player);
        if (homes.isEmpty()) {
            openHomeDialog(player);
            return;
        }

        Component title = LanguageHook.getComponent("homes.dialog_delete_selector_title", player);
        Component body = LanguageHook.getComponent("homes.dialog_delete_selector_body", player);

        List<ActionButton> buttons = new ArrayList<>();
        for (Home home : homes) {
            Component label = LanguageHook.getComponent("homes.dialog_btn_delete_entry", player,
                    Placeholder.unparsed("name", home.getName())
            );
            ActionButton btn = ActionButton.builder(label)
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            openDeleteConfirmationDialog(p, home.getName());
                        }
                    }, DEFAULT_OPTIONS))
                    .build();
            buttons.add(btn);
        }

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("homes.dialog_btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        openHomeDialog(p);
                    }
                }, DEFAULT_OPTIONS))
                .build();

        Dialog selectorDialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, 3))
        );

        player.showDialog(selectorDialog);
    }

    /**
     * Opens the Native Dialog confirmation prompt before deleting a home.
     */
    public void openDeleteConfirmationDialog(@NotNull Player player, @NotNull String homeName) {
        Component title = LanguageHook.getComponent("homes.dialog_confirm_delete_title", player);
        Component body = LanguageHook.getComponent("homes.dialog_confirm_delete_body", player,
                Placeholder.unparsed("home", homeName)
        );

        // Confirm Button
        ActionButton confirmBtn = ActionButton.builder(LanguageHook.getComponent("homes.dialog_confirm_delete_yes", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        boolean deleted = homeManager.deleteHome(p, homeName);
                        if (deleted) {
                            LanguageHook.sendMessage(p, "homes.delete_success", Placeholder.unparsed("home", homeName));
                        }
                        openHomeDialog(p);
                    }
                }, DEFAULT_OPTIONS))
                .build();

        // Cancel Button
        ActionButton cancelBtn = ActionButton.builder(LanguageHook.getComponent("homes.dialog_confirm_delete_no", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        openDeleteSelectorDialog(p);
                    }
                }, DEFAULT_OPTIONS))
                .build();

        Dialog confirmDialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.confirmation(confirmBtn, cancelBtn))
        );

        player.showDialog(confirmDialog);
    }
}
