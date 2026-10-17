package com.harukiyu.clarityessentials.chat;

import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.settings.UserSettings;
import com.harukiyu.clarityessentials.settings.UserSettingsManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ChatListener implements Listener {

    private final ChatManager chatManager;
    private final UserSettingsManager userSettingsManager;

    public ChatListener(@NotNull ChatManager chatManager, @NotNull UserSettingsManager userSettingsManager) {
        this.chatManager = chatManager;
        this.userSettingsManager = userSettingsManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        UserSettings senderSettings = userSettingsManager.getSettings(sender.getUniqueId());

        // If the sender disabled public chat, block sending and notify them
        if (!senderSettings.isPublicChat()) {
            LanguageHook.sendMessage(sender, "chat.public_chat_disabled");
            event.setCancelled(true);
            return;
        }

        // Filter out recipients who have turned off public chat in their /menu settings
        event.viewers().removeIf(audience -> {
            if (audience instanceof Player viewer) {
                UserSettings viewerSettings = userSettingsManager.getSettings(viewer.getUniqueId());
                return !viewerSettings.isPublicChat();
            }
            return false;
        });

        event.renderer((source, sourceDisplayName, message, viewer) -> 
            chatManager.formatMessage(source, message)
        );
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLegacyAsyncPlayerChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        UserSettings senderSettings = userSettingsManager.getSettings(sender.getUniqueId());

        if (!senderSettings.isPublicChat()) {
            LanguageHook.sendMessage(sender, "chat.public_chat_disabled");
            event.setCancelled(true);
            return;
        }

        event.getRecipients().removeIf(recipient -> {
            UserSettings viewerSettings = userSettingsManager.getSettings(recipient.getUniqueId());
            return !viewerSettings.isPublicChat();
        });
    }
}
