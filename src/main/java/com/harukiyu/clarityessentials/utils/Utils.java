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

    /**
     * Parses human-readable duration strings (e.g. "10s", "5m", "2h", "1d", "unlimited") into seconds.
     * Returns -1 for unlimited/permanent, or -2 for invalid format.
     */
    public static long parseDuration(@NotNull String input) {
        String trimmed = input.trim().toLowerCase();
        if (trimmed.isEmpty()) {
            return -2;
        }

        if (trimmed.equals("unlimited") || trimmed.equals("infinite") || trimmed.equals("inf")
                || trimmed.equals("perm") || trimmed.equals("permanent") || trimmed.equals("forever")
                || trimmed.equals("-1")) {
            return -1;
        }

        // Pure numbers treated as seconds
        if (trimmed.matches("^\\d+$")) {
            try {
                return Long.parseLong(trimmed);
            } catch (NumberFormatException e) {
                return -2;
            }
        }

        Pattern pattern = Pattern.compile("(\\d+)\\s*([smhdw])");
        Matcher matcher = pattern.matcher(trimmed);
        long totalSeconds = 0;
        boolean matched = false;

        while (matcher.find()) {
            matched = true;
            long val;
            try {
                val = Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return -2;
            }
            String unit = matcher.group(2);
            switch (unit) {
                case "s" -> totalSeconds += val;
                case "m" -> totalSeconds += val * 60L;
                case "h" -> totalSeconds += val * 3600L;
                case "d" -> totalSeconds += val * 86400L;
                case "w" -> totalSeconds += val * 604800L;
                default -> { return -2; }
            }
        }

        return matched ? totalSeconds : -2;
    }

    /**
     * Formats seconds into human readable format (e.g. "2h 30m", "45s").
     */
    @NotNull
    public static String formatDuration(long seconds) {
        if (seconds < 0) {
            return "Unlimited";
        }
        if (seconds == 0) {
            return "0s";
        }

        long days = seconds / 86400L;
        long rem = seconds % 86400L;
        long hours = rem / 3600L;
        rem %= 3600L;
        long minutes = rem / 60L;
        long secs = rem % 60L;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (secs > 0 || sb.isEmpty()) sb.append(secs).append("s");

        return sb.toString().trim();
    }
}
