package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.ClarityEssentials;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class ClarityEssentialsCommand implements BasicCommand {

    private final ClarityEssentials plugin;

    public ClarityEssentialsCommand(@NotNull ClarityEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) {
            return;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("clarityessentials.admin")) {
                LanguageHook.sendMessage(sender, "general.no_permission");
                return;
            }

            long start = System.currentTimeMillis();
            plugin.reloadPlugin();
            long time = System.currentTimeMillis() - start;

            LanguageHook.sendMessage(sender, "general.reload_success", Placeholder.unparsed("time", String.valueOf(time)));
            return;
        }

        if (args[0].equalsIgnoreCase("homelimit")) {
            String[] subArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
            new HomeLimitCommand(plugin.getHomeManager()).execute(stack, subArgs);
            return;
        }

        LanguageHook.sendMessage(sender, "general.unknown_command");
    }

    private void sendHelp(@NotNull CommandSender sender) {
        LanguageHook.sendMessage(sender, "general.help.header");
        LanguageHook.sendMessage(sender, "general.help.menu");
        LanguageHook.sendMessage(sender, "general.help.nick");
        LanguageHook.sendMessage(sender, "general.help.realname");
        LanguageHook.sendMessage(sender, "general.help.tpa");
        LanguageHook.sendMessage(sender, "general.help.tpahere");
        LanguageHook.sendMessage(sender, "general.help.tpaccept");
        LanguageHook.sendMessage(sender, "general.help.tpdeny");
        LanguageHook.sendMessage(sender, "general.help.tptoggle");
        LanguageHook.sendMessage(sender, "general.help.home");
        LanguageHook.sendMessage(sender, "general.help.sethome");
        LanguageHook.sendMessage(sender, "general.help.delhome");
        if (sender.hasPermission("clarityessentials.admin")) {
            LanguageHook.sendMessage(sender, "general.help.reload");
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length <= 1) {
            List<String> list = new ArrayList<>();
            String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
            if ("help".startsWith(prefix)) list.add("help");
            if (stack.getSender().hasPermission("clarityessentials.admin") && "reload".startsWith(prefix)) {
                list.add("reload");
            }
            if (stack.getSender().hasPermission("clarityessentials.homelimit.admin") && "homelimit".startsWith(prefix)) {
                list.add("homelimit");
            }
            return list;
        }

        if (args[0].equalsIgnoreCase("homelimit")) {
            String[] subArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
            return new HomeLimitCommand(plugin.getHomeManager()).suggest(stack, subArgs);
        }
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.use";
    }
}
