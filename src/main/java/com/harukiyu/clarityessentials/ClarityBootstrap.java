package com.harukiyu.clarityessentials;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogBase.DialogAfterAction;
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
                        closeScreenThenRun(player, () -> {});
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
                                .afterAction(DialogAfterAction.NONE)
                                .build())
                        .type(DialogType.multiAction(buttons, exitButton, columns))
                );

                // Also register CUSTOM_OPTIONS as a fallback
                event.registry().registerWith(DialogKeys.CUSTOM_OPTIONS, builderFactory -> builderFactory.empty()
                        .base(DialogBase.builder(title)
                                .externalTitle(externalTitle)
                                .canCloseWithEscape(true)
                                .pause(false)
                                .afterAction(DialogAfterAction.NONE)
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
        if (customActions != null && !customActions.getKeys(false).isEmpty()) {
            for (String key : customActions.getKeys(false)) {
                if (!customActions.getBoolean(key + ".enabled", true)) continue;
                String label = customActions.getString(key + ".label", key);
                String tooltip = customActions.getString(key + ".tooltip", "");
                String type = customActions.getString(key + ".type", "player-command");
                String command = customActions.getString(key + ".command", "");
                String feature = customActions.getString(key + ".feature", key);

                ActionButton.Builder btnBuilder = ActionButton.builder(parseComponent(label));
                if (!tooltip.isBlank()) {
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

        // Default: 14 Standard Buttons matching Donut SMP layout (2 columns)
        String cmdRtp = config.getString("esc-menu.commands.rtp", "rtp");
        String cmdRtpQueue = config.getString("esc-menu.commands.rtp-queue", "rtp queue");
        String cmdAuction = config.getString("esc-menu.commands.auction", "ah");
        String cmdQuickBuy = config.getString("esc-menu.commands.quick-buy", "buy");
        String cmdOrders = config.getString("esc-menu.commands.orders", "orders");
        String cmdShardShop = config.getString("esc-menu.commands.shard-shop", "shards");
        String cmdSell = config.getString("esc-menu.commands.sell", "sell");
        String cmdStats = config.getString("esc-menu.commands.stats", "stats");

        // Row 1
        buttons.add(createFeatureButton("<white>Homes</white>", "<gray>Manage and teleport to saved homes</gray>", "homes", "home"));
        buttons.add(createCommandButton("<white>RTP</white>", "<gray>Random teleport into the wilderness</gray>", cmdRtp));

        // Row 2
        buttons.add(createCommandButton("<white>Auction</white>", "<gray>Browse server auction house</gray>", cmdAuction));
        buttons.add(createCommandButton("<white>RTP Queue</white>", "<gray>Join queue for wilderness teleport</gray>", cmdRtpQueue));

        // Row 3
        buttons.add(createCommandButton("<white>Quick Buy</white>", "<gray>Fast purchase shop</gray>", cmdQuickBuy));
        buttons.add(createFeatureButton("<white>Friends</white>", "<gray>Manage friends and teleport</gray>", "friends", "friends"));

        // Row 4
        buttons.add(createCommandButton("<white>Orders</white>", "<gray>View market orders</gray>", cmdOrders));
        buttons.add(createCommandButton("<white>Shard Shop</white>", "<gray>Spend shards on rare items</gray>", cmdShardShop));

        // Row 5
        buttons.add(createCommandButton("<white>Sell</white>", "<gray>Sell items for money</gray>", cmdSell));
        buttons.add(createFeatureButton("<white>Pay</white>", "<gray>Send money to other players</gray>", "pay", "pay"));

        // Row 6
        buttons.add(createFeatureButton("<white>Teleport</white>", "<gray>Quick teleport list to players</gray>", "teleport", "tpa"));
        buttons.add(createCommandButton("<white>Stats</white>", "<gray>View player statistics</gray>", cmdStats));

        // Row 7
        buttons.add(createFeatureButton("<white>Leaderboards</white>", "<gray>View top players rankings</gray>", "leaderboards", "leaderboard"));
        buttons.add(createFeatureButton("<white>Settings</white>", "<gray>Customize your player settings</gray>", "settings", "settings"));

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
        closeScreenThenRun(player, () -> {
            ClarityEssentials plugin = ClarityEssentials.getInstance();
            if (plugin == null || !plugin.isEnabled()) {
                if (!commandFallback.isBlank()) {
                    player.performCommand(commandFallback.startsWith("/") ? commandFallback.substring(1) : commandFallback);
                }
                return;
            }

            switch (feature.toLowerCase()) {
                case "homes", "home" -> plugin.getHomeDialogManager().openHomeDialog(player);
                case "teleport", "tp" -> plugin.getTeleportDialogManager().openTeleportList(player);
                case "friends", "friend" -> plugin.getFriendsDialogManager().openFriends(player);
                case "pay" -> plugin.getPayDialogManager().openPayPlayerList(player);
                case "settings", "setting" -> plugin.getSettingsDialogManager().openMainSettings(player);
                case "leaderboards", "leaderboard" -> plugin.getLeaderboardDialogManager().openCategories(player);
                case "menu", "dashboard" -> plugin.getMenuDialogManager().openDashboard(player);
                default -> {
                    if (!commandFallback.isBlank()) {
                        player.performCommand(commandFallback.startsWith("/") ? commandFallback.substring(1) : commandFallback);
                    }
                }
            }
        });
    }

    private static void executeCommand(@NotNull Player player, @NotNull String type, @NotNull String rawCommand) {
        if (rawCommand.isBlank()) return;
        String cmd = rawCommand.startsWith("/") ? rawCommand.substring(1) : rawCommand;
        cmd = cmd.replace("%player%", player.getName()).replace("%uuid%", player.getUniqueId().toString());
        String finalCmd = cmd;

        closeScreenThenRun(player, () -> {
            if ("console-command".equalsIgnoreCase(type) || "console".equalsIgnoreCase(type)) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
            } else {
                player.performCommand(finalCmd);
            }
        });
    }

    public static void closeScreenThenRun(@NotNull Player player, @NotNull Runnable task) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ClarityEssentials");
        if (plugin != null && plugin.isEnabled()) {
            if (Bukkit.isPrimaryThread()) {
                try {
                    player.closeDialog();
                } catch (Throwable ignored) {}
                try {
                    player.closeInventory();
                } catch (Throwable ignored) {}
                Bukkit.getScheduler().runTaskLater(plugin, task, 1L);
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        player.closeDialog();
                    } catch (Throwable ignored) {}
                    try {
                        player.closeInventory();
                    } catch (Throwable ignored) {}
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
