package com.harukiyu.clarityessentials;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.DialogKeys;
import io.papermc.paper.registry.keys.tags.DialogTagKeys;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClarityBootstrap implements PluginBootstrap {

    private static final TypedKey<Dialog> PAUSE_DIALOG = DialogKeys.create(Key.key("clarity", "pause_menu"));
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final ClickCallback.Options CALLBACK_OPTIONS = ClickCallback.Options.builder()
            .uses(-1)
            .lifetime(Duration.ofDays(3650L))
            .build();

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        File dataFolder = context.getDataDirectory().toFile();
        YamlConfiguration config = loadYaml(dataFolder, "config.yml");

        boolean enabled = config.getBoolean("esc-menu.enabled", true);
        if (!enabled) {
            context.getLogger().info("Clarity pause menu additions disabled in config.yml.");
            return;
        }

        String buttonText = config.getString("esc-menu.button-text", "Clarity Menu");
        String dialogTitle = config.getString("esc-menu.dialog-title", "Clarity Menu");

        Component externalTitle = parseComponent(buttonText);
        Component title = parseComponent(dialogTitle);

        int columns = Math.max(1, Math.min(4, config.getInt("esc-menu.columns", 2)));

        List<ActionButton> buttons = buildActionButtons(config);

        ActionButton exitButton = ActionButton.builder(parseComponent("<white>Close</white>"))
                .tooltip(parseComponent("<gray>Return to game</gray>"))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player player) {
                        player.closeDialog();
                    }
                }, CALLBACK_OPTIONS))
                .build();

        // 1. Register PAUSE_DIALOG into DIALOG registry
        context.getLifecycleManager().registerEventHandler(RegistryEvents.DIALOG.compose(), event -> {
            try {
                event.registry().register(PAUSE_DIALOG, builder -> builder
                        .base(DialogBase.builder(title)
                                .externalTitle(externalTitle)
                                .canCloseWithEscape(true)
                                .pause(false)
                                .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                                .build())
                        .type(DialogType.multiAction(buttons, exitButton, columns))
                );
            } catch (Throwable t) {
                context.getLogger().warn("Failed to compose DIALOG registry: " + t.getMessage());
            }
        });

        // 2. Add PAUSE_DIALOG to #minecraft:pause_screen_additions via postFlatten lifecycle event
        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.TAGS.postFlatten(RegistryKey.DIALOG),
                event -> {
                    try {
                        PostFlattenTagRegistrar<Dialog> registrar = event.registrar();
                        registrar.addToTag(DialogTagKeys.PAUSE_SCREEN_ADDITIONS, List.of(PAUSE_DIALOG));
                        context.getLogger().info("Successfully added Clarity to #minecraft:pause_screen_additions!");
                    } catch (Throwable t) {
                        context.getLogger().warn("Failed to register pause screen additions tag: " + t.getMessage());
                    }
                }
        );
    }

    private static List<ActionButton> buildActionButtons(YamlConfiguration config) {
        List<ActionButton> buttons = new ArrayList<>();

        ConfigurationSection customActions = config.getConfigurationSection("esc-menu.actions");
        if (customActions != null) {
            Set<String> actionKeys = customActions.getKeys(false);
            if (!actionKeys.isEmpty()) {
                for (String key : actionKeys) {
                    if (!customActions.getBoolean(key + ".enabled", true)) continue;
                    String label = java.util.Objects.requireNonNullElse(customActions.getString(key + ".label"), key);
                    String tooltip = customActions.getString(key + ".tooltip");
                    String type = java.util.Objects.requireNonNullElse(customActions.getString(key + ".type"), "player-command");
                    String command = java.util.Objects.requireNonNullElse(customActions.getString(key + ".command"), "");
                    String feature = java.util.Objects.requireNonNullElse(customActions.getString(key + ".feature"), key);

                    ActionButton.Builder btnBuilder = ActionButton.builder(parseComponent(label));
                    if (tooltip != null && !tooltip.isBlank()) {
                        btnBuilder.tooltip(parseComponent(tooltip));
                    }

                    btnBuilder.action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player player) {
                            if ("feature".equalsIgnoreCase(type)) {
                                executeFeature(player, feature, command);
                            } else {
                                executeCommand(player, type, command);
                            }
                        }
                    }, CALLBACK_OPTIONS));

                    buttons.add(btnBuilder.build());
                }
                return buttons;
            }
        }

        // Default: 12 Standard Buttons matching Donut SMP layout (2 columns)
        String cmdRtp = config.getString("esc-menu.commands.rtp", "rtp");
        String cmdAuction = config.getString("esc-menu.commands.auction", "ah");
        String cmdStats = config.getString("esc-menu.commands.stats", "stats");
        String cmdSkills = config.getString("esc-menu.commands.skills", "skills");

        // Row 1
        buttons.add(createFeatureButton("<white>Homes</white>", "<gray>Manage and teleport to saved homes</gray>", "homes", "home"));
        buttons.add(createCommandButton("<white>RTP</white>", "<gray>Random teleport into the wilderness</gray>", cmdRtp));

        // Row 2
        buttons.add(createCommandButton("<white>Auction</white>", "<gray>Browse server auction house</gray>", cmdAuction));
        buttons.add(createFeatureButton("<white>Friends</white>", "<gray>Manage friends and teleport</gray>", "friends", "friends"));

        // Row 3
        buttons.add(createFeatureButton("<gray>Coming Soon</gray>", "<gray>Feature in development</gray>", "coming_soon", ""));
        buttons.add(createFeatureButton("<white>Teleport</white>", "<gray>Quick teleport list to players</gray>", "teleport", "tpa"));

        // Row 4
        buttons.add(createFeatureButton("<white>Pay</white>", "<gray>Send money to other players</gray>", "pay", "pay"));
        buttons.add(createCommandButton("<white>Stats</white>", "<gray>View player statistics</gray>", cmdStats));

        // Row 5
        buttons.add(createFeatureButton("<white>Leaderboards</white>", "<gray>View top players rankings</gray>", "leaderboards", "leaderboard"));
        buttons.add(createFeatureButton("<white>Settings</white>", "<gray>Customize your player settings</gray>", "settings", "settings"));

        // Row 6
        buttons.add(createCommandButton("<white>Skills</white>", "<gray>Open player skills</gray>", cmdSkills));
        buttons.add(createFeatureButton("<gray>Coming Soon</gray>", "<gray>Feature in development</gray>", "coming_soon", ""));

        return buttons;
    }

    private static ActionButton createFeatureButton(String label, String tooltip, String feature, String commandFallback) {
        return ActionButton.builder(parseComponent(label))
                .tooltip(parseComponent(tooltip))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player player) {
                        executeFeature(player, feature, commandFallback);
                    }
                }, CALLBACK_OPTIONS))
                .build();
    }

    private static ActionButton createCommandButton(String label, String tooltip, String command) {
        return ActionButton.builder(parseComponent(label))
                .tooltip(parseComponent(tooltip))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player player) {
                        executeCommand(player, "player-command", command);
                    }
                }, CALLBACK_OPTIONS))
                .build();
    }

    private static void executeFeature(@NotNull Player player, @NotNull String feature, @NotNull String commandFallback) {
        Runnable action = () -> {
            ClarityEssentials plugin = ClarityEssentials.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                if ("webstore".equalsIgnoreCase(feature) || "store".equalsIgnoreCase(feature) || "coming_soon".equalsIgnoreCase(feature)) {
                    player.sendMessage(parseComponent("<gray>This feature is coming soon in a future update!</gray>"));
                } else if (!commandFallback.isBlank()) {
                    closeScreenThenRun(player, () -> {
                        try {
                            player.performCommand(commandFallback.startsWith("/") ? commandFallback.substring(1) : commandFallback);
                        } catch (CommandException | IllegalArgumentException | IllegalStateException ignored) {}
                    });
                }
                return;
            }

            switch (feature.toLowerCase()) {
                case "homes", "home" -> plugin.getHomeDialogManager().openHomeDialog(player, false);
                case "teleport", "tp" -> plugin.getTeleportDialogManager().openTeleportList(player, false);
                case "friends", "friend" -> plugin.getFriendsDialogManager().openFriends(player, false);
                case "pay" -> plugin.getPayDialogManager().openPayPlayerList(player, false);
                case "settings", "setting" -> plugin.getSettingsDialogManager().openMainSettings(player, false);
                case "leaderboards", "leaderboard" -> plugin.getLeaderboardDialogManager().openCategories(player, false);
                case "menu", "dashboard" -> plugin.getMenuDialogManager().openDashboard(player, false);
                case "webstore", "store", "web", "coming_soon" -> plugin.getMenuDialogManager().openComingSoonDialog(player, false);
                default -> {
                    if (!commandFallback.isBlank()) {
                        closeScreenThenRun(player, () -> {
                            try {
                                player.performCommand(commandFallback.startsWith("/") ? commandFallback.substring(1) : commandFallback);
                            } catch (CommandException | IllegalArgumentException | IllegalStateException ignored) {}
                        });
                    }
                }
            }
        };

        if (Bukkit.isPrimaryThread()) {
            action.run();
        } else {
            ClarityEssentials plugin = ClarityEssentials.getInstance();
            if (plugin != null && plugin.isEnabled()) {
                Bukkit.getScheduler().runTask(plugin, action);
            } else {
                action.run();
            }
        }
    }

    private static void executeCommand(@NotNull Player player, @NotNull String type, @NotNull String rawCommand) {
        if (rawCommand.isBlank()) return;
        String cmd = rawCommand.startsWith("/") ? rawCommand.substring(1) : rawCommand;
        cmd = cmd.replace("%player%", player.getName()).replace("%uuid%", player.getUniqueId().toString());
        String finalCmd = cmd;

        closeScreenThenRun(player, () -> {
            try {
                if ("console-command".equalsIgnoreCase(type) || "console".equalsIgnoreCase(type)) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                } else {
                    player.performCommand(finalCmd);
                }
            } catch (CommandException | IllegalArgumentException | IllegalStateException ignored) {}
        });
    }

    public static void closeScreenThenRun(@NotNull Player player, @NotNull Runnable task) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ClarityEssentials");
        if (plugin != null && plugin.isEnabled()) {
            if (Bukkit.isPrimaryThread()) {
                try {
                    player.closeDialog();
                } catch (Exception ignored) {}
                try {
                    player.closeInventory();
                } catch (Exception ignored) {}
                Bukkit.getScheduler().runTaskLater(plugin, task, 1L);
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        player.closeDialog();
                    } catch (Exception ignored) {}
                    try {
                        player.closeInventory();
                    } catch (Exception ignored) {}
                    Bukkit.getScheduler().runTaskLater(plugin, task, 1L);
                });
            }
        } else {
            task.run();
        }
    }

    private static Component parseComponent(String text) {
        if (text == null || text.isBlank()) {
            return Component.empty();
        }
        try {
            if (!text.contains("<") && !text.contains("&")) {
                return Component.text(text).color(NamedTextColor.WHITE);
            }
            return MINI_MESSAGE.deserialize(text);
        } catch (Exception e) {
            return Component.text(text).color(NamedTextColor.WHITE);
        }
    }

    private static YamlConfiguration loadYaml(File dataFolder, String path) {
        File file = new File(dataFolder, path);
        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        }
        InputStream stream = ClarityBootstrap.class.getResourceAsStream("/" + path);
        if (stream == null) {
            return new YamlConfiguration();
        }
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (Exception e) {
            return new YamlConfiguration();
        }
    }
}
