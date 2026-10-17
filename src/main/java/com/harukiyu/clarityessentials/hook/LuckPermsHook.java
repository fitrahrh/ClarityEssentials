package com.harukiyu.clarityessentials.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class LuckPermsHook {

    private static boolean isEnabled = false;
    private static LuckPerms luckPerms = null;

    public static void init() {
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            try {
                luckPerms = LuckPermsProvider.get();
                isEnabled = true;
            } catch (Throwable ignored) {
                isEnabled = false;
            }
        } else {
            isEnabled = false;
        }
    }

    public static boolean isAvailable() {
        return isEnabled && luckPerms != null;
    }

    @NotNull
    public static String getPrimaryGroup(@NotNull Player player) {
        if (!isAvailable()) {
            return "default";
        }
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String group = user.getPrimaryGroup();
                return group != null ? group.toLowerCase() : "default";
            }
        } catch (Throwable ignored) {}
        return "default";
    }

    @NotNull
    public static String getPrefix(@NotNull Player player) {
        if (!isAvailable()) {
            return "";
        }
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null && user.getCachedData().getMetaData().getPrefix() != null) {
                return user.getCachedData().getMetaData().getPrefix();
            }
        } catch (Throwable ignored) {}
        return "";
    }

    @NotNull
    public static String getSuffix(@NotNull Player player) {
        if (!isAvailable()) {
            return "";
        }
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null && user.getCachedData().getMetaData().getSuffix() != null) {
                return user.getCachedData().getMetaData().getSuffix();
            }
        } catch (Throwable ignored) {}
        return "";
    }
}
