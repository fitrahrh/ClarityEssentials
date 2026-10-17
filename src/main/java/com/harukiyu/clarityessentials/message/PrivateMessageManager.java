package com.harukiyu.clarityessentials.message;

import com.harukiyu.clarityessentials.ClarityEssentials;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.nickname.NicknameManager;
import com.harukiyu.clarityessentials.settings.UserSettings;
import com.harukiyu.clarityessentials.settings.UserSettingsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PrivateMessageManager {

    private final UserSettingsManager userSettingsManager;
    private final NicknameManager nicknameManager;

    // UUID -> Last messaged player UUID (for /r reply)
    private final Map<UUID, UUID> lastReplyMap = new ConcurrentHashMap<>();

    public PrivateMessageManager(@NotNull ClarityEssentials plugin,
                                 @NotNull UserSettingsManager userSettingsManager,
                                 @NotNull NicknameManager nicknameManager) {
        this.userSettingsManager = userSettingsManager;
        this.nicknameManager = nicknameManager;
    }

    public boolean sendPrivateMessage(@NotNull Player sender, @NotNull Player recipient, @NotNull String message) {
        if (sender.equals(recipient)) {
            LanguageHook.sendMessage(sender, "message.cannot_message_self");
            return false;
        }

        UserSettings senderSettings = userSettingsManager.getSettings(sender.getUniqueId());
        if ("Off".equalsIgnoreCase(senderSettings.getPrivateMessages())) {
            LanguageHook.sendMessage(sender, "message.sender_pm_off");
            return false;
        }

        UserSettings recipientSettings = userSettingsManager.getSettings(recipient.getUniqueId());
        if ("Off".equalsIgnoreCase(recipientSettings.getPrivateMessages())) {
            LanguageHook.sendMessage(sender, "message.recipient_pm_off", Placeholder.unparsed("player", recipient.getName()));
            return false;
        }

        if ("Friends".equalsIgnoreCase(recipientSettings.getPrivateMessages())) {
            if (!recipientSettings.isFriend(sender.getName())) {
                LanguageHook.sendMessage(sender, "message.recipient_pm_friends_only", Placeholder.unparsed("player", recipient.getName()));
                return false;
            }
        }

        // Send to sender
        LanguageHook.sendMessage(sender, "message.format_to",
                Placeholder.component("target", nicknameManager.getDisplayName(recipient)),
                Placeholder.unparsed("message", message)
        );

        // Send to recipient
        LanguageHook.sendMessage(recipient, "message.format_from",
                Placeholder.component("sender", nicknameManager.getDisplayName(sender)),
                Placeholder.unparsed("message", message)
        );

        // Play notification sound to recipient
        try {
            recipient.playSound(recipient.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        } catch (Throwable ignored) {}

        // Update reply targets
        lastReplyMap.put(sender.getUniqueId(), recipient.getUniqueId());
        lastReplyMap.put(recipient.getUniqueId(), sender.getUniqueId());

        // Social Spy for Admins
        notifySocialSpy(sender, recipient, message);

        return true;
    }

    public boolean reply(@NotNull Player sender, @NotNull String message) {
        UUID targetUuid = lastReplyMap.get(sender.getUniqueId());
        if (targetUuid == null) {
            LanguageHook.sendMessage(sender, "message.no_reply_target");
            return false;
        }

        Player recipient = Bukkit.getPlayer(targetUuid);
        if (recipient == null || !recipient.isOnline()) {
            LanguageHook.sendMessage(sender, "message.reply_offline");
            return false;
        }

        return sendPrivateMessage(sender, recipient, message);
    }

    public void toggleSocialSpy(@NotNull Player player) {
        UserSettings settings = userSettingsManager.getSettings(player.getUniqueId());
        boolean newState = !settings.isSocialSpy();
        settings.setSocialSpy(newState);
        userSettingsManager.saveSettingsAsync(player.getUniqueId());

        if (newState) {
            LanguageHook.sendMessage(player, "message.socialspy_enabled");
        } else {
            LanguageHook.sendMessage(player, "message.socialspy_disabled");
        }
    }

    private void notifySocialSpy(@NotNull Player sender, @NotNull Player recipient, @NotNull String message) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(sender) || online.equals(recipient)) {
                continue;
            }
            if (online.hasPermission("clarityessentials.socialspy") || online.hasPermission("clarityessentials.admin")) {
                UserSettings s = userSettingsManager.getSettings(online.getUniqueId());
                if (s.isSocialSpy()) {
                    Component spyComponent = LanguageHook.getComponent("message.socialspy_format", online,
                            Placeholder.component("sender", nicknameManager.getDisplayName(sender)),
                            Placeholder.component("target", nicknameManager.getDisplayName(recipient)),
                            Placeholder.unparsed("message", message)
                    );
                    online.sendMessage(spyComponent);
                }
            }
        }

        // Also log to console
        Component consoleSpy = LanguageHook.getComponent("message.socialspy_format", null,
                Placeholder.component("sender", nicknameManager.getDisplayName(sender)),
                Placeholder.component("target", nicknameManager.getDisplayName(recipient)),
                Placeholder.unparsed("message", message)
        );
        Bukkit.getConsoleSender().sendMessage(consoleSpy);
    }
}
