package com.harukiyu.clarityessentials.home;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.data.DataManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import com.harukiyu.clarityessentials.teleport.TeleportManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

public class HomeManager {

    private static final Pattern VALID_HOME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{1,16}$");

    private final ConfigManager configManager;
    private final DataManager dataManager;
    private final TeleportManager teleportManager;

    public HomeManager(@NotNull ConfigManager configManager,
                       @NotNull DataManager dataManager,
                       @NotNull TeleportManager teleportManager) {
        this.configManager = configManager;
        this.dataManager = dataManager;
        this.teleportManager = teleportManager;
    }

    public enum SetHomeResult {
        SUCCESS,
        LIMIT_REACHED,
        INVALID_NAME
    }

    /**
     * Calculates base maximum home limit for a player from config or rank permissions.
     */
    public int getBaseHomes(@NotNull Player player) {
        int max = configManager.getHomeDefaultLimit();
        for (Map.Entry<String, Integer> entry : configManager.getHomeRankLimits().entrySet()) {
            if (player.hasPermission("clarityessentials.homes." + entry.getKey())) {
                if (entry.getValue() > max) {
                    max = entry.getValue();
                }
            }
        }
        return max;
    }

    /**
     * Gets bonus / extra purchased home slots (digital currency style).
     */
    public int getExtraHomes(@NotNull Player player) {
        return dataManager.getExtraHomes(player.getUniqueId());
    }

    public int getExtraHomes(@NotNull java.util.UUID uuid) {
        return dataManager.getExtraHomes(uuid);
    }

    public void addExtraHomes(@NotNull java.util.UUID uuid, int amount) {
        dataManager.addExtraHomes(uuid, amount);
    }

    public boolean takeExtraHomes(@NotNull java.util.UUID uuid, int amount) {
        return dataManager.takeExtraHomes(uuid, amount);
    }

    public void setExtraHomes(@NotNull java.util.UUID uuid, int amount) {
        dataManager.setExtraHomes(uuid, amount);
    }

    /**
     * Calculates total maximum home limit for a player (Base + Extra bonus slots).
     */
    public int getMaxHomes(@NotNull Player player) {
        if (player.hasPermission(configManager.getHomeUnlimitedPermission()) || player.isOp()) {
            return Integer.MAX_VALUE;
        }

        return getBaseHomes(player) + getExtraHomes(player);
    }

    @NotNull
    public Collection<Home> getHomes(@NotNull Player player) {
        return dataManager.getPlayerHomes(player.getUniqueId()).values();
    }

    public int getHomeCount(@NotNull Player player) {
        return dataManager.getPlayerHomes(player.getUniqueId()).size();
    }

    @Nullable
    public Home getHome(@NotNull Player player, @NotNull String name) {
        return dataManager.getHome(player.getUniqueId(), name);
    }

    /**
     * Creates or updates a home at the player's current location.
     */
    @NotNull
    public SetHomeResult setHome(@NotNull Player player, @NotNull String name) {
        if (!VALID_HOME_PATTERN.matcher(name).matches()) {
            return SetHomeResult.INVALID_NAME;
        }

        Map<String, Home> currentHomes = dataManager.getPlayerHomes(player.getUniqueId());
        boolean isUpdating = currentHomes.containsKey(name.toLowerCase());

        if (!isUpdating && currentHomes.size() >= getMaxHomes(player)) {
            return SetHomeResult.LIMIT_REACHED;
        }

        Home home = Home.fromLocation(name, player.getLocation());
        dataManager.setHome(player.getUniqueId(), home);

        if (configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), configManager.getSoundActionSuccess(), 1.0f, 1.0f);
        }
        return SetHomeResult.SUCCESS;
    }

    /**
     * Deletes a player's home by name.
     */
    public boolean deleteHome(@NotNull Player player, @NotNull String name) {
        boolean deleted = dataManager.deleteHome(player.getUniqueId(), name);
        if (deleted && configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), configManager.getSoundActionSuccess(), 1.0f, 1.0f);
        }
        return deleted;
    }

    /**
     * Teleports a player to their home location with warmup.
     */
    public boolean teleportHome(@NotNull Player player, @NotNull String name) {
        Home home = getHome(player, name);
        if (home == null) {
            LanguageHook.sendMessage(player, "homes.not_found", Placeholder.unparsed("home", name));
            if (configManager.isSoundsEnabled()) {
                player.playSound(player.getLocation(), configManager.getSoundActionFailure(), 1.0f, 1.0f);
            }
            return false;
        }

        Location loc = home.toLocation();
        if (loc == null || loc.getWorld() == null) {
            LanguageHook.sendMessage(player, "homes.not_found", Placeholder.unparsed("home", name));
            return false;
        }

        LanguageHook.sendMessage(player, "homes.teleported", Placeholder.unparsed("home", home.getName()));
        teleportManager.teleportWithWarmup(player, loc);
        return true;
    }
}
