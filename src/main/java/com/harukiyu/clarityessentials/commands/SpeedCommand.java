package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
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

/**
 * Speed command referencing EssentialsX implementation:
 * - /speed <value> [player]
 * - /speed <fly|walk> <value> [player]
 * - /speed reset [player]
 * - /speed <fly|walk> reset [player]
 */
public class SpeedCommand implements BasicCommand {

    private final ConfigManager configManager;

    private enum SpeedMode {
        AUTO,
        FLY,
        WALK
    }

    public SpeedCommand(@NotNull ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) return;

        if (args.length == 0) {
            if (sender instanceof Player) {
                LanguageHook.sendMessage(sender, "speed.usage");
            } else {
                LanguageHook.sendMessage(sender, "speed.usage_console");
            }
            return;
        }

        SpeedMode mode = SpeedMode.AUTO;
        String speedArg;
        String targetArg = null;

        String firstArgLower = args[0].toLowerCase(Locale.ROOT);
        if (firstArgLower.equals("fly") || firstArgLower.equals("flying")) {
            mode = SpeedMode.FLY;
            if (args.length < 2) {
                LanguageHook.sendMessage(sender, "speed.usage");
                return;
            }
            speedArg = args[1];
            if (args.length >= 3) {
                targetArg = args[2];
            }
        } else if (firstArgLower.equals("walk") || firstArgLower.equals("walking")) {
            mode = SpeedMode.WALK;
            if (args.length < 2) {
                LanguageHook.sendMessage(sender, "speed.usage");
                return;
            }
            speedArg = args[1];
            if (args.length >= 3) {
                targetArg = args[2];
            }
        } else {
            mode = SpeedMode.AUTO;
            speedArg = args[0];
            if (args.length >= 2) {
                targetArg = args[1];
            }
        }

        // Resolve target player
        Player target;
        if (targetArg != null) {
            if (!sender.hasPermission("clarityessentials.command.speed.others")) {
                LanguageHook.sendMessage(sender, "speed.no_permission_others");
                return;
            }
            target = Bukkit.getPlayer(targetArg);
            if (target == null || !target.isOnline()) {
                LanguageHook.sendMessage(sender, "general.player_not_found", Placeholder.unparsed("player", targetArg));
                return;
            }
        } else {
            if (!(sender instanceof Player player)) {
                LanguageHook.sendMessage(sender, "speed.usage_console");
                return;
            }
            if (!player.hasPermission("clarityessentials.command.speed")) {
                LanguageHook.sendMessage(player, "general.no_permission");
                return;
            }
            target = player;
        }

        // Handle Reset
        if (speedArg.equalsIgnoreCase("reset")) {
            handleReset(sender, target, mode);
            return;
        }

        // Handle Speed Number Parsing
        float userSpeed;
        try {
            userSpeed = Float.parseFloat(speedArg);
        } catch (NumberFormatException e) {
            LanguageHook.sendMessage(sender, "speed.invalid_speed", Placeholder.unparsed("speed", speedArg));
            return;
        }

        if (userSpeed < 0.0f || userSpeed > 10.0f) {
            LanguageHook.sendMessage(sender, "speed.invalid_speed", Placeholder.unparsed("speed", speedArg));
            return;
        }

        String speedStr = (userSpeed == (long) userSpeed) ? String.valueOf((long) userSpeed) : String.valueOf(userSpeed);

        boolean isFly = (mode == SpeedMode.FLY) || (mode == SpeedMode.AUTO && target.isFlying());
        float realSpeed = getRealMoveSpeed(userSpeed, isFly);

        if (isFly) {
            target.setFlySpeed(realSpeed);
            if (sender.equals(target)) {
                LanguageHook.sendMessage(sender, "speed.fly_self", Placeholder.unparsed("speed", speedStr));
            } else {
                LanguageHook.sendMessage(sender, "speed.fly_target",
                        Placeholder.unparsed("target", target.getName()),
                        Placeholder.unparsed("speed", speedStr));
                LanguageHook.sendMessage(target, "speed.fly_target_notify",
                        Placeholder.unparsed("sender", sender.getName()),
                        Placeholder.unparsed("speed", speedStr));
            }
        } else {
            target.setWalkSpeed(realSpeed);
            if (sender.equals(target)) {
                LanguageHook.sendMessage(sender, "speed.walk_self", Placeholder.unparsed("speed", speedStr));
            } else {
                LanguageHook.sendMessage(sender, "speed.walk_target",
                        Placeholder.unparsed("target", target.getName()),
                        Placeholder.unparsed("speed", speedStr));
                LanguageHook.sendMessage(target, "speed.walk_target_notify",
                        Placeholder.unparsed("sender", sender.getName()),
                        Placeholder.unparsed("speed", speedStr));
            }
        }

        if (configManager.isSoundsEnabled()) {
            target.playSound(target.getLocation(), configManager.getSoundActionSuccess(), 1.0f, 1.0f);
        }
    }

    private void handleReset(@NotNull CommandSender sender, @NotNull Player target, @NotNull SpeedMode mode) {
        if (mode == SpeedMode.FLY) {
            target.setFlySpeed(0.1f);
            if (sender.equals(target)) {
                LanguageHook.sendMessage(sender, "speed.reset_fly_self");
            } else {
                LanguageHook.sendMessage(sender, "speed.reset_fly_target", Placeholder.unparsed("target", target.getName()));
                LanguageHook.sendMessage(target, "speed.reset_fly_target_notify", Placeholder.unparsed("sender", sender.getName()));
            }
        } else if (mode == SpeedMode.WALK) {
            target.setWalkSpeed(0.2f);
            if (sender.equals(target)) {
                LanguageHook.sendMessage(sender, "speed.reset_walk_self");
            } else {
                LanguageHook.sendMessage(sender, "speed.reset_walk_target", Placeholder.unparsed("target", target.getName()));
                LanguageHook.sendMessage(target, "speed.reset_walk_target_notify", Placeholder.unparsed("sender", sender.getName()));
            }
        } else {
            target.setFlySpeed(0.1f);
            target.setWalkSpeed(0.2f);
            if (sender.equals(target)) {
                LanguageHook.sendMessage(sender, "speed.reset_all_self");
            } else {
                LanguageHook.sendMessage(sender, "speed.reset_all_target", Placeholder.unparsed("target", target.getName()));
                LanguageHook.sendMessage(target, "speed.reset_all_target_notify", Placeholder.unparsed("sender", sender.getName()));
            }
        }

        if (configManager.isSoundsEnabled()) {
            target.playSound(target.getLocation(), configManager.getSoundActionSuccess(), 1.0f, 1.0f);
        }
    }

    /**
     * Converts speed from [0, 10] range to Bukkit speed [-1, 1] range.
     * EssentialsX conversion formula.
     */
    private float getRealMoveSpeed(float userSpeed, boolean isFly) {
        float defaultSpeed = isFly ? 0.1f : 0.2f;
        float maxSpeed = 1.0f;
        float result;
        if (userSpeed < 1.0f) {
            result = defaultSpeed * userSpeed;
        } else {
            float ratio = ((userSpeed - 1.0f) / 9.0f) * (maxSpeed - defaultSpeed);
            result = defaultSpeed + ratio;
        }
        return Math.max(0.0f, Math.min(1.0f, result));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        List<String> list = new ArrayList<>();
        List<String> speedPresets = List.of("1", "2", "3", "5", "10", "reset");

        if (args.length <= 1) {
            String prefix = (args.length == 0) ? "" : args[0].toLowerCase(Locale.ROOT);
            List<String> base = new ArrayList<>(List.of("fly", "walk"));
            base.addAll(speedPresets);
            for (String s : base) {
                if (s.startsWith(prefix)) list.add(s);
            }
            return list;
        }

        if (args.length == 2) {
            String first = args[0].toLowerCase(Locale.ROOT);
            String prefix = args[1].toLowerCase(Locale.ROOT);
            if (first.equals("fly") || first.equals("walk")) {
                for (String s : speedPresets) {
                    if (s.startsWith(prefix)) list.add(s);
                }
            } else if (sender != null && sender.hasPermission("clarityessentials.command.speed.others")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                        list.add(p.getName());
                    }
                }
            }
            return list;
        }

        if (args.length == 3) {
            String first = args[0].toLowerCase(Locale.ROOT);
            String prefix = args[2].toLowerCase(Locale.ROOT);
            if ((first.equals("fly") || first.equals("walk")) && sender != null && sender.hasPermission("clarityessentials.command.speed.others")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                        list.add(p.getName());
                    }
                }
            }
            return list;
        }

        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.speed";
    }
}
