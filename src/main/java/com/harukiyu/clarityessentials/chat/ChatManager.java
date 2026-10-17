package com.harukiyu.clarityessentials.chat;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LuckPermsHook;
import com.harukiyu.clarityessentials.hook.PlaceholderAPIHook;
import com.harukiyu.clarityessentials.nickname.NicknameManager;
import com.harukiyu.clarityessentials.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatManager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    private final ConfigManager configManager;
    private final NicknameManager nicknameManager;

    public ChatManager(@NotNull ConfigManager configManager, @NotNull NicknameManager nicknameManager) {
        this.configManager = configManager;
        this.nicknameManager = nicknameManager;
    }

    /**
     * Renders a fully formatted chat message Component for a player.
     */
    @NotNull
    public Component formatMessage(@NotNull Player player, @NotNull Component originalMessage) {
        if (!configManager.isChatEnabled()) {
            return originalMessage;
        }

        // 1. Resolve format pattern
        String group = LuckPermsHook.getPrimaryGroup(player);
        String formatPattern = configManager.getGroupChatFormats().get(group);
        if (formatPattern == null) {
            formatPattern = configManager.getDefaultChatFormat();
        }

        // 2. Parse PlaceholderAPI on the format template
        formatPattern = PlaceholderAPIHook.setPlaceholders(player, formatPattern);

        // Standardize template tokens to MiniMessage tags
        formatPattern = formatPattern
                .replace("{prefix}", "<prefix>")
                .replace("{suffix}", "<suffix>")
                .replace("{player}", "<player>")
                .replace("{nickname}", "<nickname>")
                .replace("{message}", "<message>");

        // 3. Process player's chat message
        String rawText = PLAIN_SERIALIZER.serialize(originalMessage);
        Component messageComponent;
        if (player.hasPermission(configManager.getChatColorPermission())) {
            // Player is allowed to use colors / MiniMessage tags
            String formattedText = Utils.convertHexToMiniMessage(rawText);
            try {
                messageComponent = MINI_MESSAGE.deserialize(formattedText);
            } catch (Throwable t) {
                messageComponent = Component.text(rawText);
            }
        } else {
            // Unparsed plain text to prevent MiniMessage exploits
            messageComponent = Component.text(rawText);
        }

        // 4. Resolve metadata
        String prefix = LuckPermsHook.getPrefix(player);
        String suffix = LuckPermsHook.getSuffix(player);
        Component nickComponent = nicknameManager.getDisplayName(player);

        Component prefixComponent = prefix.isEmpty() ? Component.empty() : Utils.parseLegacy(prefix);
        Component suffixComponent = suffix.isEmpty() ? Component.empty() : Utils.parseLegacy(suffix);

        // 5. Construct final rendered Component
        try {
            return MINI_MESSAGE.deserialize(
                    formatPattern,
                    Placeholder.component("prefix", prefixComponent),
                    Placeholder.component("suffix", suffixComponent),
                    Placeholder.component("player", Component.text(player.getName())),
                    Placeholder.component("nickname", nickComponent),
                    Placeholder.component("message", messageComponent)
            );
        } catch (Throwable t) {
            // Fallback safe formatting
            return Component.text(player.getName() + " » " + rawText);
        }
    }
}
