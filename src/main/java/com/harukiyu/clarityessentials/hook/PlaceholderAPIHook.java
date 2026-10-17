package com.harukiyu.clarityessentials.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlaceholderAPIHook {

    private static boolean isEnabled = false;

    public static void init() {
        isEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public static boolean isAvailable() {
        return isEnabled;
    }

    @NotNull
    public static String setPlaceholders(@Nullable Player player, @NotNull String text) {
        if (!isEnabled || text.indexOf('%') == -1) {
            return text;
        }
        try {
            return PlaceholderAPI.setPlaceholders(player, text);
        } catch (Throwable t) {
            return text;
        }
    }
}
