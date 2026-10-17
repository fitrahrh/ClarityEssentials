package com.harukiyu.clarityessentials.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
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
     * Parses a string that might contain MiniMessage tags.
     */
    @NotNull
    public static Component parseMiniMessage(@NotNull String text) {
        return MINI_MESSAGE.deserialize(text);
    }
}
