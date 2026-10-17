package com.harukiyu.clarityessentials.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern CURLY_PLACEHOLDER_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");
    private static final Set<String> CORE_VARIABLES = Set.of("prefix", "suffix", "player", "nickname", "message");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private Utils() {}

    /**
     * Converts legacy color codes (& and hex &#RRGGBB) to MiniMessage tags or Adventure Component.
     */
    @NotNull
    public static Component parseLegacy(@NotNull String text) {
        String converted = convertHexToMiniMessage(text);
        return AMPERSAND_SERIALIZER.deserialize(converted);
    }

    /**
     * Translates &#rrggbb hex patterns to <#rrggbb> for MiniMessage compatibility.
     */
    @NotNull
    public static String convertHexToMiniMessage(@NotNull String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<#" + matcher.group(1) + ">");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Converts legacy color codes (§ and & and §x hex) to MiniMessage tags.
     */
    @NotNull
    public static String convertLegacyToMiniMessage(@NotNull String text) {
        if (text.indexOf('§') == -1 && text.indexOf('&') == -1) {
            return text;
        }

        // Handle hex &#rrggbb
        text = convertHexToMiniMessage(text);

        // Handle §x§r§1§r§2§g§1§g§2§b§1§b§2 / &x&r&1&r&2&g&1&g&2&b&1&b&2
        text = text.replaceAll("(?i)[§&]x[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])", "<#$1$2$3$4$5$6>");

        // Standard 16 colors & styles
        text = text
                .replaceAll("(?i)[§&]0", "<black>")
                .replaceAll("(?i)[§&]1", "<dark_blue>")
                .replaceAll("(?i)[§&]2", "<dark_green>")
                .replaceAll("(?i)[§&]3", "<dark_aqua>")
                .replaceAll("(?i)[§&]4", "<dark_red>")
                .replaceAll("(?i)[§&]5", "<dark_purple>")
                .replaceAll("(?i)[§&]6", "<gold>")
                .replaceAll("(?i)[§&]7", "<gray>")
                .replaceAll("(?i)[§&]8", "<dark_gray>")
                .replaceAll("(?i)[§&]9", "<blue>")
                .replaceAll("(?i)[§&]a", "<green>")
                .replaceAll("(?i)[§&]b", "<aqua>")
                .replaceAll("(?i)[§&]c", "<red>")
                .replaceAll("(?i)[§&]d", "<light_purple>")
                .replaceAll("(?i)[§&]e", "<yellow>")
                .replaceAll("(?i)[§&]f", "<white>")
                .replaceAll("(?i)[§&]k", "<obfuscated>")
                .replaceAll("(?i)[§&]l", "<bold>")
                .replaceAll("(?i)[§&]m", "<strikethrough>")
                .replaceAll("(?i)[§&]n", "<underlined>")
                .replaceAll("(?i)[§&]o", "<italic>")
                .replaceAll("(?i)[§&]r", "<reset>");
        return text;
    }

    /**
     * Converts any non-core {placeholder} tokens to %placeholder% so PlaceholderAPI can expand them.
     */
    @NotNull
    public static String convertCurlyToPapi(@NotNull String text) {
        if (!text.contains("{")) {
            return text;
        }
        Matcher matcher = CURLY_PLACEHOLDER_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String token = matcher.group(1).toLowerCase();
            if (!CORE_VARIABLES.contains(token)) {
                matcher.appendReplacement(sb, "%" + matcher.group(1) + "%");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Parses a string that might contain MiniMessage tags.
     */
    @NotNull
    public static Component parseMiniMessage(@NotNull String text) {
        return MINI_MESSAGE.deserialize(text);
    }
}
