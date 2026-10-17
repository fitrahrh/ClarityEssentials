package com.harukiyu.clarityessentials.nickname;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.data.DataManager;
import com.harukiyu.clarityessentials.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class NicknameManager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]*>");

    private final ConfigManager configManager;
    private final DataManager dataManager;

    public NicknameManager(@NotNull ConfigManager configManager, @NotNull DataManager dataManager) {
        this.configManager = configManager;
        this.dataManager = dataManager;
    }

    public enum NickResult {
        SUCCESS,
        TOO_SHORT,
        TOO_LONG,
        INVALID_REGEX,
        ALREADY_IN_USE,
        COLOR_NO_PERMISSION
    }

    /**
     * Validates and sets a player's nickname.
     */
    @NotNull
    public NickResult setNickname(@NotNull Player player, @NotNull String rawInput) {
        boolean hasUnsafeBypass = player.hasPermission(configManager.getNickUnsafeBypassPermission());
        boolean hasColorPerm = player.hasPermission(configManager.getNickColorPermission());

        boolean hasColorTags = TAG_PATTERN.matcher(rawInput).find() || rawInput.contains("&") || rawInput.contains("§");
        if (hasColorTags && !hasColorPerm && !hasUnsafeBypass) {
            return NickResult.COLOR_NO_PERMISSION;
        }

        // Convert legacy color codes if any
        String processed = Utils.convertHexToMiniMessage(rawInput);
        if (processed.contains("&")) {
            processed = processed.replace("&0", "<black>")
                    .replace("&1", "<dark_blue>")
                    .replace("&2", "<dark_green>")
                    .replace("&3", "<dark_aqua>")
                    .replace("&4", "<dark_red>")
                    .replace("&5", "<dark_purple>")
                    .replace("&6", "<gold>")
                    .replace("&7", "<gray>")
                    .replace("&8", "<dark_gray>")
                    .replace("&9", "<blue>")
                    .replace("&a", "<green>")
                    .replace("&b", "<aqua>")
                    .replace("&c", "<red>")
                    .replace("&d", "<light_purple>")
                    .replace("&e", "<yellow>")
                    .replace("&f", "<white>")
                    .replace("&l", "<bold>")
                    .replace("&o", "<italic>")
                    .replace("&n", "<underlined>")
                    .replace("&m", "<strikethrough>")
                    .replace("&k", "<obfuscated>")
                    .replace("&r", "<reset>");
        }

        // Get plain text representation
        Component parsedComponent = MINI_MESSAGE.deserialize(processed);
        String plainText = PLAIN_SERIALIZER.serialize(parsedComponent);

        if (!hasUnsafeBypass) {
            // Check lengths
            if (plainText.length() < configManager.getNickMinLength()) {
                return NickResult.TOO_SHORT;
            }
            if (plainText.length() > configManager.getNickMaxLength()) {
                return NickResult.TOO_LONG;
            }

            // Check regex
            if (!configManager.getAllowedNicksPattern().matcher(plainText).matches()) {
                return NickResult.INVALID_REGEX;
            }

            // Check if another player has this username or nickname
            Player existingPlayer = Bukkit.getPlayerExact(plainText);
            if (existingPlayer != null && !existingPlayer.getUniqueId().equals(player.getUniqueId())) {
                return NickResult.ALREADY_IN_USE;
            }
            UUID existingUuid = dataManager.getUuidByNickname(plainText);
            if (existingUuid != null && !existingUuid.equals(player.getUniqueId())) {
                return NickResult.ALREADY_IN_USE;
            }
        }

        // Store and apply
        dataManager.setNickname(player.getUniqueId(), processed, plainText);
        applyNickname(player, parsedComponent);

        return NickResult.SUCCESS;
    }

    /**
     * Resets a player's nickname back to their real username.
     */
    public void resetNickname(@NotNull Player player) {
        dataManager.setNickname(player.getUniqueId(), null, null);
        Component defaultName = Component.text(player.getName());
        player.displayName(defaultName);
        if (configManager.isNickChangeTabName()) {
            player.playerListName(defaultName);
        }
    }

    /**
     * Applies the nickname from cache/data to the player when joining or updating.
     */
    public void applyNickname(@NotNull Player player) {
        String nick = dataManager.getNickname(player.getUniqueId());
        if (nick != null && !nick.isBlank()) {
            try {
                Component comp = MINI_MESSAGE.deserialize(nick);
                applyNickname(player, comp);
                return;
            } catch (Throwable ignored) {}
        }
        Component defaultName = Component.text(player.getName());
        player.displayName(defaultName);
        if (configManager.isNickChangeTabName()) {
            player.playerListName(defaultName);
        }
    }

    private void applyNickname(@NotNull Player player, @NotNull Component component) {
        player.displayName(component);
        if (configManager.isNickChangeTabName()) {
            player.playerListName(component);
        }
    }

    @Nullable
    public String getNickname(@NotNull UUID uuid) {
        return dataManager.getNickname(uuid);
    }

    @Nullable
    public String getRawNickname(@NotNull UUID uuid) {
        return dataManager.getRawNickname(uuid);
    }

    @NotNull
    public Component getDisplayName(@NotNull Player player) {
        String nick = dataManager.getNickname(player.getUniqueId());
        if (nick != null && !nick.isBlank()) {
            try {
                return MINI_MESSAGE.deserialize(nick);
            } catch (Throwable ignored) {}
        }
        return Component.text(player.getName());
    }

    @Nullable
    public UUID getPlayerByNickname(@NotNull String nickname) {
        return dataManager.getUuidByNickname(nickname);
    }

    @NotNull
    public List<String> getOnlineNicknames() {
        List<String> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            String nick = dataManager.getRawNickname(p.getUniqueId());
            if (nick != null && !nick.isBlank()) {
                list.add(nick);
            }
        }
        return list;
    }
}
