package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.fly.FlyManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.utils.Utils;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class FlyCommand implements BasicCommand {

    private final FlyManager flyManager;
    private final ConfigManager configManager;

    private static final List<String> DURATION_SUGGESTIONS = List.of(
            "off", "30s", "1m", "5m", "10m", "30m", "1h", "unlimited"
    );

    public FlyCommand(@NotNull FlyManager flyManager, @NotNull ConfigManager configManager) {
        this.flyManager = flyManager;
        this.configManager = configManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) return;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                LanguageHook.sendMessage(sender, "general.only_players");
                return;
            }

            if (!player.hasPermission("clarityessentials.command.fly")) {
                LanguageHook.sendMessage(player, "general.no_permission");
                return;
            }

            if (flyManager.isFlying(player)) {
                flyManager.setFly(player, false, 0, player);
            } else {
                long duration = -1;
                if (!player.hasPermission("clarityessentials.fly.unlimited")) {
                    String defaultDurStr = configManager.getFlyDefaultDuration();
                    long parsed = Utils.parseDuration(defaultDurStr);
                    duration = parsed >= 0 ? parsed : -1;
                }
                flyManager.setFly(player, true, duration, player);
            }
            return;
        }

        // Check if first argument is a target player or duration
        Player target = Bukkit.getPlayer(args[0]);

        if (target != null && target.isOnline()) {
            // /fly <player> [duration]
            if (!sender.hasPermission("clarityessentials.fly.others")) {
                LanguageHook.sendMessage(sender, "fly.no_permission_others");
                return;
            }

            if (args.length == 1) {
                // Toggle target
                boolean newState = !flyManager.isFlying(target);
                long duration = -1;
                if (newState && !target.hasPermission("clarityessentials.fly.unlimited")) {
                    long parsed = Utils.parseDuration(configManager.getFlyDefaultDuration());
                    duration = parsed >= 0 ? parsed : -1;
                }
                flyManager.setFly(target, newState, duration, sender);
                return;
            }

            // /fly <player> <duration/off>
            String durInput = args[1].toLowerCase(Locale.ROOT);
            if ("off".equals(durInput)) {
                flyManager.setFly(target, false, 0, sender);
                return;
            }

            long duration = Utils.parseDuration(durInput);
            if (duration < -1) {
                LanguageHook.sendMessage(sender, "fly.invalid_duration", Placeholder.unparsed("duration", args[1]));
                return;
            }

            flyManager.setFly(target, true, duration, sender);
            return;
        }

        // Not a target player -> must be /fly <duration/off> for self
        if (!(sender instanceof Player player)) {
            LanguageHook.sendMessage(sender, "general.only_players");
            return;
        }

        if (!player.hasPermission("clarityessentials.command.fly")) {
            LanguageHook.sendMessage(player, "general.no_permission");
            return;
        }

        String durInput = args[0].toLowerCase(Locale.ROOT);
        if ("off".equals(durInput)) {
            flyManager.setFly(player, false, 0, player);
            return;
        }

        long duration = Utils.parseDuration(durInput);
        if (duration < -1) {
            LanguageHook.sendMessage(player, "fly.invalid_duration", Placeholder.unparsed("duration", args[0]));
            return;
        }

        if (duration == -1 && !player.hasPermission("clarityessentials.fly.unlimited")) {
            LanguageHook.sendMessage(player, "fly.no_permission_unlimited");
            return;
        }

        flyManager.setFly(player, true, duration, player);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        List<String> list = new ArrayList<>();

        if (args.length <= 1) {
            String prefix = (args.length == 0) ? "" : args[0].toLowerCase(Locale.ROOT);
            for (String dur : DURATION_SUGGESTIONS) {
                if (dur.startsWith(prefix)) list.add(dur);
            }
            if (sender != null && sender.hasPermission("clarityessentials.fly.others")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                        list.add(p.getName());
                    }
                }
            }
        } else if (args.length == 2 && sender != null && sender.hasPermission("clarityessentials.fly.others")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            for (String dur : DURATION_SUGGESTIONS) {
                if (dur.startsWith(prefix)) list.add(dur);
            }
        }

        return list;
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.fly";
    }
}
