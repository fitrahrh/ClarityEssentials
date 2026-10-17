package com.harukiyu.clarityessentials.hook;

import com.harukiyu.clarityessentials.ClarityEssentials;
import com.harukiyu.clarityessentials.settings.UserSettings;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

public class ClarityPlaceholderExpansion extends PlaceholderExpansion {

    private final ClarityEssentials plugin;
    private final String identifier;

    public ClarityPlaceholderExpansion(@NotNull ClarityEssentials plugin, @NotNull String identifier) {
        this.plugin = plugin;
        this.identifier = identifier;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public @NotNull String getAuthor() {
        return "Harukiyu";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) {
            return "";
        }

        UUID uuid = offlinePlayer.getUniqueId();
        String sub = params.toLowerCase(Locale.ROOT);

        switch (sub) {
            case "homes_count", "homes" -> {
                return String.valueOf(plugin.getDataManager().getPlayerHomes(uuid).size());
            }
            case "homes_max" -> {
                if (offlinePlayer.isOnline()) {
                    Player online = offlinePlayer.getPlayer();
                    if (online != null) {
                        int max = plugin.getHomeManager().getMaxHomes(online);
                        return max == Integer.MAX_VALUE ? "Unlimited" : String.valueOf(max);
                    }
                }
                int fallback = plugin.getConfigManager().getHomeDefaultLimit() + plugin.getDataManager().getExtraHomes(uuid);
                return String.valueOf(fallback);
            }
            case "homes_base" -> {
                if (offlinePlayer.isOnline()) {
                    Player online = offlinePlayer.getPlayer();
                    if (online != null) {
                        return String.valueOf(plugin.getHomeManager().getBaseHomes(online));
                    }
                }
                return String.valueOf(plugin.getConfigManager().getHomeDefaultLimit());
            }
            case "homes_extra", "homes_currency" -> {
                return String.valueOf(plugin.getDataManager().getExtraHomes(uuid));
            }
            case "homes_remaining" -> {
                int count = plugin.getDataManager().getPlayerHomes(uuid).size();
                int max = plugin.getConfigManager().getHomeDefaultLimit() + plugin.getDataManager().getExtraHomes(uuid);
                if (offlinePlayer.isOnline()) {
                    Player online = offlinePlayer.getPlayer();
                    if (online != null) {
                        max = plugin.getHomeManager().getMaxHomes(online);
                        if (max == Integer.MAX_VALUE) return "Unlimited";
                    }
                }
                return String.valueOf(Math.max(0, max - count));
            }
            case "nickname" -> {
                String nick = plugin.getDataManager().getNickname(uuid);
                return nick != null ? nick : (offlinePlayer.getName() != null ? offlinePlayer.getName() : "");
            }
            case "raw_nickname" -> {
                String raw = plugin.getDataManager().getRawNickname(uuid);
                return raw != null ? raw : (offlinePlayer.getName() != null ? offlinePlayer.getName() : "");
            }
            case "realname" -> {
                return offlinePlayer.getName() != null ? offlinePlayer.getName() : "";
            }
            case "tpa_status" -> {
                return plugin.getDataManager().isTpaEnabled(uuid, true) ? "Enabled" : "Disabled";
            }
            case "friends_count" -> {
                return String.valueOf(plugin.getUserSettingsManager().getSettings(uuid).getFriendsList().size());
            }
            case "friends_online", "friends_online_count" -> {
                UserSettings s = plugin.getUserSettingsManager().getSettings(uuid);
                int onlineCount = 0;
                for (String friendName : s.getFriendsList()) {
                    Player friend = Bukkit.getPlayer(friendName);
                    if (friend != null && friend.isOnline()) {
                        onlineCount++;
                    }
                }
                return String.valueOf(onlineCount);
            }
            case "setting_public_chat" -> {
                return plugin.getUserSettingsManager().getSettings(uuid).isPublicChat() ? "ON" : "OFF";
            }
            case "setting_pm", "setting_private_messages" -> {
                return plugin.getUserSettingsManager().getSettings(uuid).getPrivateMessages();
            }
            case "setting_socialspy" -> {
                return plugin.getUserSettingsManager().getSettings(uuid).isSocialSpy() ? "ON" : "OFF";
            }
            case "setting_join_leave" -> {
                return plugin.getUserSettingsManager().getSettings(uuid).getJoinLeaveMessages();
            }
            default -> {
                return null;
            }
        }
    }
}
