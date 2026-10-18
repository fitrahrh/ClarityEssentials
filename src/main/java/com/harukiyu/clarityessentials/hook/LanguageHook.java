package com.harukiyu.clarityessentials.hook;

import com.harukiyu.clarityessentials.ClarityEssentials;
import com.harukiyu.clarityessentials.settings.UserSettings;
import com.harukiyu.claritylangcore.api.ClarityLangAPI;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
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
    private static final ClickCallback.Options DIALOG_OPTIONS = ClickCallback.Options.builder().build();

    public static final Map<String, String> SUPPORTED_LANG_NAMES = new LinkedHashMap<>();
    static {
        SUPPORTED_LANG_NAMES.put("en_US", "English");
        SUPPORTED_LANG_NAMES.put("id_ID", "Bahasa Indonesia");
        SUPPORTED_LANG_NAMES.put("es_ES", "Español");
        SUPPORTED_LANG_NAMES.put("pt_BR", "Português");
        SUPPORTED_LANG_NAMES.put("zh_CN", "简体中文");
        SUPPORTED_LANG_NAMES.put("ja_JP", "日本語");
        SUPPORTED_LANG_NAMES.put("de_DE", "Deutsch");
        SUPPORTED_LANG_NAMES.put("fr_FR", "Français");
        SUPPORTED_LANG_NAMES.put("ru_RU", "Русский");
    }

    private static final List<String> SUPPORTED_LANGS = new ArrayList<>(SUPPORTED_LANG_NAMES.keySet());

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

        // 1. Check user settings if player explicitly selected a language in ClarityEssentials
        ClarityEssentials plugin = ClarityEssentials.getInstance();
        if (plugin != null && plugin.getUserSettingsManager() != null) {
            UserSettings settings = plugin.getUserSettingsManager().getSettings(player.getUniqueId());
            if (settings != null && settings.getLanguage() != null && !settings.getLanguage().isBlank()) {
                return normalizeLanguageCode(settings.getLanguage());
            }
        }

        // 2. Check ClarityLangCore if present
        if (isClarityLangCorePresent) {
            try {
                String code = ClarityLangAPI.getPlayerLanguageCode(player);
                if (code != null && !code.isBlank()) {
                    return normalizeLanguageCode(code);
                }
            } catch (Throwable ignored) {}
        }

        // 3. Fall back to client locale
        try {
            Locale clientLocale = player.locale();
            String fullTag = clientLocale.toString().toLowerCase(Locale.ROOT);
            String lang = clientLocale.getLanguage().toLowerCase(Locale.ROOT);

            String matched = matchSupportedLanguage(fullTag, lang);
            if (matched != null) {
                return matched;
            }
        } catch (Throwable ignored) {}

        return "en_US";
    }

    public static String normalizeLanguageCode(@NotNull String code) {
        String lower = code.toLowerCase(Locale.ROOT).replace("-", "_");
        if (lower.startsWith("id") || lower.startsWith("in")) return "id_ID";
        if (lower.startsWith("pt")) return "pt_BR";
        if (lower.startsWith("es")) return "es_ES";
        if (lower.startsWith("zh")) return "zh_CN";
        if (lower.startsWith("ja")) return "ja_JP";
        if (lower.startsWith("de")) return "de_DE";
        if (lower.startsWith("fr")) return "fr_FR";
        if (lower.startsWith("ru")) return "ru_RU";
        if (lower.startsWith("en")) return "en_US";
        for (String supported : SUPPORTED_LANGS) {
            if (supported.equalsIgnoreCase(code)) {
                return supported;
            }
        }
        return "en_US";
    }

    private static String matchSupportedLanguage(String fullTag, String lang) {
        if (lang.equals("in") || lang.equals("id") || fullTag.startsWith("in") || fullTag.startsWith("id")) return "id_ID";
        if (lang.equals("pt") || fullTag.startsWith("pt")) return "pt_BR";
        if (lang.equals("es") || fullTag.startsWith("es")) return "es_ES";
        if (lang.equals("zh") || fullTag.startsWith("zh")) return "zh_CN";
        if (lang.equals("ja") || fullTag.startsWith("ja")) return "ja_JP";
        if (lang.equals("de") || fullTag.startsWith("de")) return "de_DE";
        if (lang.equals("fr") || fullTag.startsWith("fr")) return "fr_FR";
        if (lang.equals("ru") || fullTag.startsWith("ru")) return "ru_RU";
        if (lang.equals("en") || fullTag.startsWith("en")) return "en_US";
        return null;
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
        openLanguageDialog(player, true);
    }

    public static void openLanguageDialog(@NotNull Player player, boolean playSound) {
        ClarityEssentials plugin = ClarityEssentials.getInstance();
        if (playSound && plugin != null && plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(player.getLocation(), plugin.getConfigManager().getSoundDialogOpen(), 1.0f, 1.0f);
        }

        String currentCode = getEffectiveLanguageCode(player);
        Component title = getComponent("language.title", player);
        Component body = getComponent("language.body", player);

        List<ActionButton> buttons = new ArrayList<>();
        for (Map.Entry<String, String> entry : SUPPORTED_LANG_NAMES.entrySet()) {
            String code = entry.getKey();
            String name = entry.getValue();
            boolean isCurrent = code.equalsIgnoreCase(currentCode);

            Component label = MINI_MESSAGE.deserialize(isCurrent ? "<green>✔ " + name + "</green>" : "<white>" + name + "</white>");
            ActionButton.Builder btnBuilder = ActionButton.builder(label);

            btnBuilder.action(DialogAction.customClick((view, audience) -> {
                if (audience instanceof Player p) {
                    setPlayerLanguage(p, code);
                    openLanguageDialog(p, false);
                }
            }, DIALOG_OPTIONS));

            buttons.add(btnBuilder.build());
        }

        // Add Auto (Client Locale) button
        boolean isAuto = false;
        if (plugin != null && plugin.getUserSettingsManager() != null) {
            UserSettings settings = plugin.getUserSettingsManager().getSettings(player.getUniqueId());
            if (settings != null && (settings.getLanguage() == null || settings.getLanguage().isBlank())) {
                isAuto = true;
            }
        }
        Component autoLabel = MINI_MESSAGE.deserialize(isAuto ? "<green>✔ </green><gray>Automatic</gray>" : "<gray>Automatic</gray>");
        buttons.add(ActionButton.builder(autoLabel)
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        setPlayerLanguage(p, "");
                        openLanguageDialog(p, false);
                    }
                }, DIALOG_OPTIONS)).build());

        // Back button to settings
        ActionButton backBtn = ActionButton.builder(getComponent("menu.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        if (plugin != null && plugin.getSettingsDialogManager() != null) {
                            plugin.getSettingsDialogManager().openMainSettings(p, false);
                        } else {
                            p.closeDialog();
                        }
                    }
                }, DIALOG_OPTIONS)).build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, 2))
        );

        player.showDialog(dialog);
    }

    public static void setPlayerLanguage(@NotNull Player player, @NotNull String langCode) {
        ClarityEssentials plugin = ClarityEssentials.getInstance();
        if (plugin != null && plugin.getUserSettingsManager() != null) {
            UserSettings settings = plugin.getUserSettingsManager().getSettings(player.getUniqueId());
            if (settings != null) {
                settings.setLanguage(langCode);
                plugin.getUserSettingsManager().saveSettingsAsync(player.getUniqueId());
            }
        }

        if (isClarityLangCorePresent) {
            try {
                if (langCode.isBlank()) {
                    ClarityLangAPI.resetPlayerLanguage(player.getUniqueId());
                } else {
                    ClarityLangAPI.setPlayerLanguage(player.getUniqueId(), langCode);
                }
            } catch (Throwable ignored) {}
        }

        String displayName = langCode.isBlank() ? "Automatic" : SUPPORTED_LANG_NAMES.getOrDefault(langCode, langCode);
        sendMessage(player, "language.changed", Placeholder.unparsed("language", displayName));

        if (plugin != null && plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(player.getLocation(), plugin.getConfigManager().getSoundActionSuccess(), 1.0f, 1.0f);
        }
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
    public static String getRawString(@NotNull String key) {
        return getRawString(key, "en_US");
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
