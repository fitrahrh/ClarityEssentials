package com.harukiyu.clarityessentials.utils;

import com.harukiyu.clarityessentials.hook.LanguageHook;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerValidator {

    private PlayerValidator() {}

    public record ValidationResult(boolean valid, @Nullable String resolvedName) {}

    @NotNull
    public static ValidationResult validatePlayerName(@NotNull Player sender, @Nullable String rawName) {
        if (rawName == null || rawName.isBlank()) {
            LanguageHook.sendMessage(sender, "validation.name_empty");
            sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return new ValidationResult(false, null);
        }

        String trimmed = rawName.trim();
        if (trimmed.equalsIgnoreCase(sender.getName())) {
            LanguageHook.sendMessage(sender, "validation.cannot_add_self");
            sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return new ValidationResult(false, null);
        }

        Player onlineTarget = Bukkit.getPlayerExact(trimmed);
        if (onlineTarget != null) {
            return new ValidationResult(true, onlineTarget.getName());
        }

        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(trimmed);
        if (offlineTarget.hasPlayedBefore() || offlineTarget.isOnline()) {
            String name = offlineTarget.getName() != null ? offlineTarget.getName() : trimmed;
            return new ValidationResult(true, name);
        }

        LanguageHook.sendMessage(sender, "validation.player_not_found", Placeholder.unparsed("player", trimmed));
        sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        return new ValidationResult(false, null);
    }
}
