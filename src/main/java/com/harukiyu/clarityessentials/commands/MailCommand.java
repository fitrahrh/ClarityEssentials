package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.mail.Mail;
import com.harukiyu.clarityessentials.mail.MailManager;
import com.harukiyu.clarityessentials.utils.Utils;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;

public class MailCommand implements BasicCommand {

    private final MailManager mailManager;
    private final ConfigManager configManager;
    private static final int MAILS_PER_PAGE = 5;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public MailCommand(@NotNull MailManager mailManager, @NotNull ConfigManager configManager) {
        this.mailManager = mailManager;
        this.configManager = configManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) return;

        if (args.length == 0) {
            LanguageHook.sendMessage(sender, "mail.help");
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "read" -> handleRead(sender, args);
            case "send" -> handleSend(sender, args);
            case "sendall" -> handleSendAll(sender, args);
            case "sendtempall" -> handleSendTempAll(sender, args);
            case "clear" -> handleClear(sender, args);
            case "clearall" -> handleClearAll(sender);
            default -> LanguageHook.sendMessage(sender, "mail.help");
        }
    }

    private void handleRead(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            LanguageHook.sendMessage(sender, "general.only_players");
            return;
        }

        if (!player.hasPermission("clarityessentials.command.mail")) {
            LanguageHook.sendMessage(player, "general.no_permission");
            return;
        }

        int page = 1;
        if (args.length >= 2) {
            try {
                page = Math.max(1, Integer.parseInt(args[1]));
            } catch (NumberFormatException ignored) {}
        }

        List<Mail> mails = mailManager.getPlayerMails(player.getUniqueId());
        if (mails.isEmpty()) {
            LanguageHook.sendMessage(player, "mail.empty_inbox");
            return;
        }

        int totalPages = (int) Math.ceil((double) mails.size() / MAILS_PER_PAGE);
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * MAILS_PER_PAGE;
        int endIndex = Math.min(startIndex + MAILS_PER_PAGE, mails.size());

        LanguageHook.sendMessage(player, "mail.read_header",
                Placeholder.unparsed("page", String.valueOf(page)),
                Placeholder.unparsed("total", String.valueOf(totalPages)),
                Placeholder.unparsed("count", String.valueOf(mails.size()))
        );

        for (int i = startIndex; i < endIndex; i++) {
            Mail m = mails.get(i);
            int mailNum = i + 1;
            String timeStr = DATE_FORMAT.format(new Date(m.getTimestamp()));
            String typeTag = m.isGlobal() ? "<gold>[Broadcast]</gold> " : "";

            LanguageHook.sendMessage(player, "mail.read_entry",
                    Placeholder.unparsed("index", String.valueOf(mailNum)),
                    Placeholder.parsed("type", typeTag),
                    Placeholder.unparsed("sender", m.getSenderName()),
                    Placeholder.unparsed("date", timeStr),
                    Placeholder.unparsed("message", m.getMessage())
            );
        }

        LanguageHook.sendMessage(player, "mail.read_footer",
                Placeholder.unparsed("page", String.valueOf(page)),
                Placeholder.unparsed("total", String.valueOf(totalPages))
        );

        // Mark as read
        mailManager.markAllRead(player.getUniqueId());
    }

    private void handleSend(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("clarityessentials.command.mail.send") && !sender.hasPermission("clarityessentials.command.mail")) {
            LanguageHook.sendMessage(sender, "general.no_permission");
            return;
        }

        if (args.length < 3) {
            LanguageHook.sendMessage(sender, "mail.usage_send");
            return;
        }

        String targetName = args[1];
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
        if (message.isBlank()) {
            LanguageHook.sendMessage(sender, "mail.usage_send");
            return;
        }

        Player onlineTarget = Bukkit.getPlayer(targetName);
        UUID targetUuid;
        String resolvedName;

        if (onlineTarget != null) {
            targetUuid = onlineTarget.getUniqueId();
            resolvedName = onlineTarget.getName();
        } else {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            if (!offlineTarget.hasPlayedBefore() && !offlineTarget.isOnline()) {
                LanguageHook.sendMessage(sender, "general.player_not_found", Placeholder.unparsed("player", targetName));
                return;
            }
            targetUuid = offlineTarget.getUniqueId();
            resolvedName = offlineTarget.getName() != null ? offlineTarget.getName() : targetName;
        }

        String senderName = sender instanceof Player p ? p.getName() : "Console";
        UUID senderUuid = sender instanceof Player p ? p.getUniqueId() : null;

        mailManager.sendMail(targetUuid, senderName, senderUuid, message);
        LanguageHook.sendMessage(sender, "mail.sent_success",
                Placeholder.unparsed("target", resolvedName)
        );

        if (sender instanceof Player p && configManager.isSoundsEnabled()) {
            p.playSound(p.getLocation(), configManager.getSoundActionSuccess(), 1.0f, 1.0f);
        }
    }

    private void handleSendAll(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("clarityessentials.command.mail.sendall")) {
            LanguageHook.sendMessage(sender, "general.no_permission");
            return;
        }

        if (args.length < 2) {
            LanguageHook.sendMessage(sender, "mail.usage_sendall");
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        if (message.isBlank()) {
            LanguageHook.sendMessage(sender, "mail.usage_sendall");
            return;
        }

        String senderName = sender instanceof Player p ? p.getName() : "Server";
        UUID senderUuid = sender instanceof Player p ? p.getUniqueId() : null;

        mailManager.sendGlobalMail(senderName, senderUuid, 0L, message);
        LanguageHook.sendMessage(sender, "mail.sendall_success");
    }

    private void handleSendTempAll(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("clarityessentials.command.mail.sendall")) {
            LanguageHook.sendMessage(sender, "general.no_permission");
            return;
        }

        if (args.length < 3) {
            LanguageHook.sendMessage(sender, "mail.usage_sendtempall");
            return;
        }

        String durationStr = args[1];
        long durationSeconds = Utils.parseDuration(durationStr);
        if (durationSeconds <= 0) {
            LanguageHook.sendMessage(sender, "mail.invalid_duration", Placeholder.unparsed("duration", durationStr));
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
        if (message.isBlank()) {
            LanguageHook.sendMessage(sender, "mail.usage_sendtempall");
            return;
        }

        String senderName = sender instanceof Player p ? p.getName() : "Server";
        UUID senderUuid = sender instanceof Player p ? p.getUniqueId() : null;

        mailManager.sendGlobalMail(senderName, senderUuid, durationSeconds, message);
        LanguageHook.sendMessage(sender, "mail.sendtempall_success",
                Placeholder.unparsed("duration", Utils.formatDuration(durationSeconds))
        );
    }

    private void handleClear(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("clarityessentials.command.mail")) {
            LanguageHook.sendMessage(sender, "general.no_permission");
            return;
        }

        // /mail clear [number] OR /mail clear <player> [number]
        if (args.length == 2) {
            // Check if args[1] is a number (self clear)
            try {
                int index = Integer.parseInt(args[1]);
                if (!(sender instanceof Player player)) {
                    LanguageHook.sendMessage(sender, "general.only_players");
                    return;
                }
                boolean cleared = mailManager.clearMail(player.getUniqueId(), index);
                if (cleared) {
                    LanguageHook.sendMessage(player, "mail.cleared_single", Placeholder.unparsed("index", String.valueOf(index)));
                } else {
                    LanguageHook.sendMessage(player, "mail.invalid_index", Placeholder.unparsed("index", String.valueOf(index)));
                }
                return;
            } catch (NumberFormatException e) {
                // Not a number, check if admin clearing target player
                if (!sender.hasPermission("clarityessentials.command.mail.admin")) {
                    LanguageHook.sendMessage(sender, "mail.invalid_index", Placeholder.unparsed("index", args[1]));
                    return;
                }
                String targetName = args[1];
                OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
                if (!op.hasPlayedBefore() && !op.isOnline()) {
                    LanguageHook.sendMessage(sender, "general.player_not_found", Placeholder.unparsed("player", targetName));
                    return;
                }
                mailManager.clearPlayerMails(op.getUniqueId());
                LanguageHook.sendMessage(sender, "mail.cleared_target_all", Placeholder.unparsed("target", targetName));
                return;
            }
        }

        if (args.length >= 3) {
            // /mail clear <player> <index>
            if (!sender.hasPermission("clarityessentials.command.mail.admin")) {
                LanguageHook.sendMessage(sender, "general.no_permission");
                return;
            }
            String targetName = args[1];
            OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
            if (!op.hasPlayedBefore() && !op.isOnline()) {
                LanguageHook.sendMessage(sender, "general.player_not_found", Placeholder.unparsed("player", targetName));
                return;
            }
            try {
                int index = Integer.parseInt(args[2]);
                boolean cleared = mailManager.clearMail(op.getUniqueId(), index);
                if (cleared) {
                    LanguageHook.sendMessage(sender, "mail.cleared_target_single",
                            Placeholder.unparsed("target", targetName),
                            Placeholder.unparsed("index", String.valueOf(index))
                    );
                } else {
                    LanguageHook.sendMessage(sender, "mail.invalid_index", Placeholder.unparsed("index", String.valueOf(index)));
                }
            } catch (NumberFormatException e) {
                LanguageHook.sendMessage(sender, "mail.invalid_index", Placeholder.unparsed("index", args[2]));
            }
            return;
        }

        // /mail clear without number
        LanguageHook.sendMessage(sender, "mail.usage_clear");
    }

    private void handleClearAll(@NotNull CommandSender sender) {
        if (!(sender instanceof Player player)) {
            LanguageHook.sendMessage(sender, "general.only_players");
            return;
        }

        if (!player.hasPermission("clarityessentials.command.mail")) {
            LanguageHook.sendMessage(player, "general.no_permission");
            return;
        }

        mailManager.clearAll(player.getUniqueId());
        LanguageHook.sendMessage(player, "mail.cleared_all");
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (args.length <= 1) {
            String prefix = (args.length == 0) ? "" : args[0].toLowerCase(Locale.ROOT);
            List<String> list = new ArrayList<>();
            List<String> subs = new ArrayList<>(List.of("read", "send", "clear", "clearall"));
            if (sender != null && sender.hasPermission("clarityessentials.command.mail.sendall")) {
                subs.add("sendall");
                subs.add("sendtempall");
            }
            for (String s : subs) {
                if (s.startsWith(prefix)) list.add(s);
            }
            return list;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            if (sub.equals("send") || (sub.equals("clear") && sender != null && sender.hasPermission("clarityessentials.command.mail.admin"))) {
                List<String> list = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                        list.add(p.getName());
                    }
                }
                return list;
            }
            if (sub.equals("sendtempall")) {
                return List.of("1h", "12h", "1d", "7d", "30d");
            }
        }

        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.mail";
    }
}
