package com.harukiyu.clarityessentials.hook;

import com.harukiyu.claritylangcore.api.ClarityLangAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class LanguageHook {

    private static boolean isClarityLangCorePresent = false;
    private static final Map<String, YamlConfiguration> fallbackBundles = new ConcurrentHashMap<>();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final List<String> SUPPORTED_LANGS = List.of(
            "en_US", "id_ID", "pt_BR", "es_ES", "zh_CN", "ja_JP", "de_DE", "fr_FR", "ru_RU"
    );

    public static void init(@NotNull Plugin plugin) {
        // Load all 9 embedded languages into fallback cache
        fallbackBundles.clear();
        for (String lang : SUPPORTED_LANGS) {
            try (InputStream stream = plugin.getResource("languages/" + lang + ".yml")) {
                if (stream != null) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    fallbackBundles.put(lang.toLowerCase(Locale.ROOT), config);
                }
            } catch (Throwable ignored) {}
        }

        if (Bukkit.getPluginManager().isPluginEnabled("ClarityLangCore")) {
            try {
                ClarityLangAPI.registerBundle(plugin, "essentials", "languages");
                isClarityLangCorePresent = true;
                plugin.getLogger().info("Successfully connected to ClarityLangCore translation bundle!");
            } catch (Throwable t) {
                plugin.getLogger().warning(() -> "Failed to register bundle with ClarityLangCore: " + t.getMessage());
                isClarityLangCorePresent = false;
            }
        } else {
            isClarityLangCorePresent = false;
            plugin.getLogger().info("ClarityLangCore not found. Running in standalone fallback mode.");
        }
    }

    public static boolean isCoreAvailable() {
        return isClarityLangCorePresent;
    }

    @NotNull
    public static String getEffectiveLanguageCode(@Nullable Player player) {
        if (player == null) {
            return "en_US";
        }
        if (isClarityLangCorePresent) {
            try {
                String code = ClarityLangAPI.getPlayerLanguageCode(player);
                if (!code.isBlank()) {
                    return code;
                }
            } catch (Throwable ignored) {}
        }
        try {
            Locale clientLocale = player.locale();
            String lang = clientLocale.getLanguage().toLowerCase(Locale.ROOT);
            for (String supported : SUPPORTED_LANGS) {
                if (supported.toLowerCase(Locale.ROOT).startsWith(lang)) {
                    return supported;
                }
            }
        } catch (Throwable ignored) {}
        return "en_US";
    }

    @NotNull
    private static TagResolver[] buildResolversWithPrefix(String langCode, @NotNull TagResolver... resolvers) {
        String prefix = getRawString("prefix", langCode);
        if (prefix == null) {
            prefix = "";
        }

        List<TagResolver> list = new ArrayList<>(resolvers.length + 1);
        list.add(Placeholder.parsed("prefix", prefix));
        Collections.addAll(list, resolvers);
        return list.toArray(TagResolver[]::new);
    }

    public static void sendMessage(@Nullable Audience audience, @NotNull String key, @NotNull TagResolver... resolvers) {
        if (audience == null) {
            return;
        }
        if (audience instanceof Player player) {
            sendMessage(player, key, resolvers);
            return;
        }

        // Console or non-player audience
        String langCode = "en_US";
        TagResolver[] enriched = buildResolversWithPrefix(langCode, resolvers);

        if (isClarityLangCorePresent) {
            try {
                ClarityLangAPI.sendMessage(audience, "essentials:" + key, enriched);
                return;
            } catch (Throwable ignored) {}
        }
        audience.sendMessage(getFallbackComponent(key, langCode, enriched));
    }

    public static void sendMessage(@Nullable Player player, @NotNull String key, @NotNull TagResolver... resolvers) {
        if (player == null) {
            return;
        }
        String langCode = getEffectiveLanguageCode(player);
        TagResolver[] enriched = buildResolversWithPrefix(langCode, resolvers);

        if (isClarityLangCorePresent) {
            try {
                ClarityLangAPI.sendMessage(player, "essentials:" + key, enriched);
                return;
            } catch (Throwable ignored) {}
        }
        player.sendMessage(getFallbackComponent(key, langCode, enriched));
    }

    public static void broadcastMessage(@NotNull String key, @NotNull TagResolver... resolvers) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            sendMessage(online, key, resolvers);
        }
        sendMessage(Bukkit.getConsoleSender(), key, resolvers);
    }

    @NotNull
    public static Component getComponent(@NotNull String key, @Nullable Player player, @NotNull TagResolver... resolvers) {
        String langCode = getEffectiveLanguageCode(player);
        TagResolver[] enriched = buildResolversWithPrefix(langCode, resolvers);

        if (isClarityLangCorePresent && player != null) {
            try {
                return ClarityLangAPI.getComponent("essentials:" + key, player, enriched);
            } catch (Throwable ignored) {}
        }
        return getFallbackComponent(key, langCode, enriched);
    }

    @NotNull
    public static List<Component> getComponentList(@NotNull String key, @Nullable Player player, @NotNull TagResolver... resolvers) {
        String langCode = getEffectiveLanguageCode(player);
        TagResolver[] enriched = buildResolversWithPrefix(langCode, resolvers);

        if (isClarityLangCorePresent && player != null) {
            try {
                return ClarityLangAPI.getComponentList("essentials:" + key, player, enriched);
            } catch (Throwable ignored) {}
        }

        YamlConfiguration config = getFallbackConfig(langCode);
        if (config.contains(key)) {
            List<String> rawList = config.getStringList(key);
            List<Component> result = new ArrayList<>();
            for (String line : rawList) {
                result.add(MINI_MESSAGE.deserialize(line, enriched));
            }
            return result;
        }
        return List.of(Component.text(key));
    }

    public static void openLanguageDialog(@NotNull Player player) {
        if (isClarityLangCorePresent) {
            try {
                ClarityLangAPI.openLanguageDialog(player);
                return;
            } catch (Throwable ignored) {}
        }
        player.sendMessage(MINI_MESSAGE.deserialize("<gradient:#38ef7d:#11998e>[Clarity]</gradient> <red>Language switcher requires ClarityLangCore plugin!</red>"));
    }

    @NotNull
    private static YamlConfiguration getFallbackConfig(String langCode) {
        YamlConfiguration config = fallbackBundles.get(langCode.toLowerCase(Locale.ROOT));
        if (config == null) {
            config = fallbackBundles.get("en_us");
        }
        if (config == null) {
            config = new YamlConfiguration();
        }
        return config;
    }

    @Nullable
    public static String getRawString(@NotNull String key, @NotNull String langCode) {
        if (isClarityLangCorePresent) {
            try {
                String val = ClarityLangAPI.getRawString("essentials:" + key, langCode);
                if (val != null) return val;
            } catch (Throwable ignored) {}
        }

        YamlConfiguration config = getFallbackConfig(langCode);
        if (config.contains(key)) {
            return config.getString(key);
        }

        // Try en_US fallback
        YamlConfiguration enConfig = fallbackBundles.get("en_us");
        if (enConfig != null && enConfig.contains(key)) {
            return enConfig.getString(key);
        }
        return null;
    }

    @NotNull
    private static Component getFallbackComponent(@NotNull String key, @NotNull String langCode, @NotNull TagResolver... resolvers) {
        String raw = getRawString(key, langCode);
        if (raw == null) {
            raw = key;
        }
        return MINI_MESSAGE.deserialize(raw, resolvers);
    }
}
