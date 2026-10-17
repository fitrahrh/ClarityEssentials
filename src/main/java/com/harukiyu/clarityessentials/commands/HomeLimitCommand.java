package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.home.HomeManager;
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

public class HomeLimitCommand implements BasicCommand {

    private final HomeManager homeManager;

    public HomeLimitCommand(@NotNull HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) return;

        // Player checking their own home limit: /homelimit
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                LanguageHook.sendMessage(sender, "general.only_players");
                return;
            }
            sendQuotaInfo(sender, player);
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        // /homelimit check <player> or /homelimit <player>
        if (sub.equals("check") || args.length == 1) {
            if (!sender.hasPermission("clarityessentials.homelimit.admin")) {
                LanguageHook.sendMessage(sender, "general.no_permission");
                return;
            }

            String targetName = sub.equals("check") && args.length > 1 ? args[1] : args[0];
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                LanguageHook.sendMessage(sender, "general.player_not_found", Placeholder.unparsed("player", targetName));
                return;
            }

            sendQuotaInfo(sender, target);
            return;
        }

        // Subcommands requiring admin permission: give, take, set
        if (!sender.hasPermission("clarityessentials.homelimit.admin")) {
            LanguageHook.sendMessage(sender, "general.no_permission");
            return;
        }

        if (args.length < 3) {
            LanguageHook.sendMessage(sender, "homelimit.usage");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            LanguageHook.sendMessage(sender, "general.player_not_found", Placeholder.unparsed("player", args[1]));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount < 0) {
                LanguageHook.sendMessage(sender, "homelimit.invalid_amount");
                return;
            }
        } catch (NumberFormatException e) {
            LanguageHook.sendMessage(sender, "homelimit.invalid_amount");
            return;
        }

        switch (sub) {
            case "give", "add" -> {
                homeManager.addExtraHomes(target.getUniqueId(), amount);
                int newMax = homeManager.getMaxHomes(target);
                LanguageHook.sendMessage(sender, "homelimit.given",
                        Placeholder.unparsed("amount", String.valueOf(amount)),
                        Placeholder.unparsed("player", target.getName()),
                        Placeholder.unparsed("max", String.valueOf(newMax))
                );
                LanguageHook.sendMessage(target, "homelimit.received",
                        Placeholder.unparsed("amount", String.valueOf(amount)),
                        Placeholder.unparsed("max", String.valueOf(newMax))
                );
            }
            case "take", "remove" -> {
                homeManager.takeExtraHomes(target.getUniqueId(), amount);
                int newMax = homeManager.getMaxHomes(target);
                LanguageHook.sendMessage(sender, "homelimit.taken",
                        Placeholder.unparsed("amount", String.valueOf(amount)),
                        Placeholder.unparsed("player", target.getName()),
                        Placeholder.unparsed("max", String.valueOf(newMax))
                );
                LanguageHook.sendMessage(target, "homelimit.deducted",
                        Placeholder.unparsed("amount", String.valueOf(amount)),
                        Placeholder.unparsed("max", String.valueOf(newMax))
                );
            }
            case "set" -> {
                homeManager.setExtraHomes(target.getUniqueId(), amount);
                int newMax = homeManager.getMaxHomes(target);
                LanguageHook.sendMessage(sender, "homelimit.set",
                        Placeholder.unparsed("amount", String.valueOf(amount)),
                        Placeholder.unparsed("player", target.getName()),
                        Placeholder.unparsed("max", String.valueOf(newMax))
                );
                LanguageHook.sendMessage(target, "homelimit.received_set",
                        Placeholder.unparsed("amount", String.valueOf(amount)),
                        Placeholder.unparsed("max", String.valueOf(newMax))
                );
            }
            default -> LanguageHook.sendMessage(sender, "homelimit.usage");
        }
    }

    private void sendQuotaInfo(@NotNull CommandSender viewer, @NotNull Player target) {
        int current = homeManager.getHomeCount(target);
        int max = homeManager.getMaxHomes(target);
        int base = homeManager.getBaseHomes(target);
        int extra = homeManager.getExtraHomes(target);
        String maxStr = max == Integer.MAX_VALUE ? "Unlimited" : String.valueOf(max);

        LanguageHook.sendMessage(viewer, "homelimit.info_header", Placeholder.unparsed("player", target.getName()));
        LanguageHook.sendMessage(viewer, "homelimit.info_count",
                Placeholder.unparsed("current", String.valueOf(current)),
                Placeholder.unparsed("max", maxStr)
        );
        LanguageHook.sendMessage(viewer, "homelimit.info_breakdown",
                Placeholder.unparsed("base", String.valueOf(base)),
                Placeholder.unparsed("extra", String.valueOf(extra))
        );
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (!stack.getSender().hasPermission("clarityessentials.homelimit.admin")) {
            return List.of();
        }

        if (args.length <= 1) {
            String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
            List<String> list = new ArrayList<>();
            for (String sub : List.of("check", "give", "take", "set")) {
                if (sub.startsWith(prefix)) list.add(sub);
            }
            return list;
        }

        if (args.length == 2) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            List<String> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    list.add(p.getName());
                }
            }
            return list;
        }

        if (args.length == 3 && List.of("give", "take", "set").contains(args[0].toLowerCase(Locale.ROOT))) {
            return List.of("1", "2", "3", "5", "10");
        }

        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.homelimit";
    }
}
