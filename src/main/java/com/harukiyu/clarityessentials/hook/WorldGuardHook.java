package com.harukiyu.clarityessentials.hook;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class WorldGuardHook {

    private static boolean available = false;
    private static Method wgGetInstanceMethod;
    private static Method wgGetPlatformMethod;
    private static Method platformGetRegionContainerMethod;
    private static Method bukkitAdapterAdaptWorldMethod;
    private static Method bukkitAdapterAsBlockVectorMethod;
    private static Method containerGetMethod;
    private static Method regionManagerGetApplicableRegionsMethod;
    private static Method protectedRegionGetIdMethod;

    private WorldGuardHook() {}

    public static void init() {
        try {
            Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
            if (wg == null || !wg.isEnabled()) {
                available = false;
                return;
            }

            Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            wgGetInstanceMethod = wgClass.getMethod("getInstance");
            Object wgInstance = wgGetInstanceMethod.invoke(null);

            wgGetPlatformMethod = wgClass.getMethod("getPlatform");
            Object platform = wgGetPlatformMethod.invoke(wgInstance);

            Class<?> containerClass = Class.forName("com.sk89q.worldguard.protection.regions.RegionContainer");
            platformGetRegionContainerMethod = platform.getClass().getMethod("getRegionContainer");

            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            bukkitAdapterAdaptWorldMethod = bukkitAdapterClass.getMethod("adapt", World.class);
            bukkitAdapterAsBlockVectorMethod = bukkitAdapterClass.getMethod("asBlockVector", Location.class);

            Class<?> worldEditWorldClass = Class.forName("com.sk89q.worldedit.world.World");
            containerGetMethod = containerClass.getMethod("get", worldEditWorldClass);

            Class<?> regionManagerClass = Class.forName("com.sk89q.worldguard.protection.managers.RegionManager");
            Class<?> blockVector3Class = Class.forName("com.sk89q.worldedit.math.BlockVector3");
            regionManagerGetApplicableRegionsMethod = regionManagerClass.getMethod("getApplicableRegions", blockVector3Class);

            Class<?> protectedRegionClass = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion");
            protectedRegionGetIdMethod = protectedRegionClass.getMethod("getId");

            available = true;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException | LinkageError | RuntimeException t) {
            available = false;
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    @NotNull
    public static Set<String> getRegionsAt(@NotNull Location location) {
        if (!available || location.getWorld() == null) {
            return Collections.emptySet();
        }

        try {
            Object wgInstance = wgGetInstanceMethod.invoke(null);
            Object platform = wgGetPlatformMethod.invoke(wgInstance);
            Object container = platformGetRegionContainerMethod.invoke(platform);

            Object weWorld = bukkitAdapterAdaptWorldMethod.invoke(null, location.getWorld());
            if (weWorld == null) return Collections.emptySet();

            Object regionManager = containerGetMethod.invoke(container, weWorld);
            if (regionManager == null) return Collections.emptySet();

            Object blockVector = bukkitAdapterAsBlockVectorMethod.invoke(null, location);
            Object applicableRegions = regionManagerGetApplicableRegionsMethod.invoke(regionManager, blockVector);
            if (applicableRegions instanceof Iterable<?> iterable) {
                Set<String> set = new HashSet<>();
                for (Object obj : iterable) {
                    if (obj != null) {
                        String id = (String) protectedRegionGetIdMethod.invoke(obj);
                        if (id != null) {
                            set.add(id.toLowerCase());
                        }
                    }
                }
                return set;
            }
        } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException | RuntimeException ignored) {
            // Safe fallback
        }
        return Collections.emptySet();
    }
}
