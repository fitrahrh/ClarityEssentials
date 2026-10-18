package com.harukiyu.clarityessentials.mail;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class MailManager {

    private final Plugin plugin;
    private final ConfigManager configManager;
    private final File mailsFile;
    private YamlConfiguration mailsConfig;

    // UUID -> List of personal mails
    private final Map<UUID, List<Mail>> personalMails = new ConcurrentHashMap<>();

    // Global / Broadcast mails
    private final List<Mail> globalMails = Collections.synchronizedList(new ArrayList<>());

    // UUID -> Set of cleared global mail IDs
    private final Map<UUID, Set<String>> clearedGlobalMails = new ConcurrentHashMap<>();

    // UUID -> Set of read global mail IDs
    private final Map<UUID, Set<String>> readGlobalMails = new ConcurrentHashMap<>();

    public MailManager(@NotNull Plugin plugin, @NotNull ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.mailsFile = new File(new File(plugin.getDataFolder(), "data"), "mails.yml");
        load();
    }

    public final synchronized void load() {
        if (!mailsFile.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            mailsFile.getParentFile().mkdirs();
        }
        if (!mailsFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                mailsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create mails.yml", e);
            }
        }

        this.mailsConfig = YamlConfiguration.loadConfiguration(mailsFile);
        personalMails.clear();
        globalMails.clear();
        clearedGlobalMails.clear();
        readGlobalMails.clear();

        // Load Global Mails
        ConfigurationSection globalSec = mailsConfig.getConfigurationSection("global");
        if (globalSec != null) {
            for (String key : globalSec.getKeys(false)) {
                ConfigurationSection sec = globalSec.getConfigurationSection(key);
                if (sec != null) {
                    Mail mail = Mail.fromMap(key, sec.getValues(false), true);
                    if (!mail.isExpired()) {
                        globalMails.add(mail);
                    }
                }
            }
        }

        // Load Inboxes
        ConfigurationSection inboxSec = mailsConfig.getConfigurationSection("inbox");
        if (inboxSec != null) {
            for (String uuidStr : inboxSec.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    ConfigurationSection playerSec = inboxSec.getConfigurationSection(uuidStr);
                    if (playerSec != null) {
                        List<Mail> list = new ArrayList<>();
                        ConfigurationSection mailsSec = playerSec.getConfigurationSection("mails");
                        if (mailsSec != null) {
                            for (String mailKey : mailsSec.getKeys(false)) {
                                ConfigurationSection mSec = mailsSec.getConfigurationSection(mailKey);
                                if (mSec != null) {
                                    Mail m = Mail.fromMap(mailKey, mSec.getValues(false), false);
                                    if (!m.isExpired()) {
                                        list.add(m);
                                    }
                                }
                            }
                        }
                        personalMails.put(uuid, list);

                        List<String> cleared = playerSec.getStringList("cleared_global");
                        clearedGlobalMails.put(uuid, new HashSet<>(cleared));

                        List<String> read = playerSec.getStringList("read_global");
                        readGlobalMails.put(uuid, new HashSet<>(read));
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public synchronized void saveSync() {
        if (mailsConfig == null) mailsConfig = new YamlConfiguration();
        mailsConfig.set("global", null);
        mailsConfig.set("inbox", null);

        // Save Global
        for (Mail m : globalMails) {
            if (!m.isExpired()) {
                mailsConfig.set("global." + m.getId(), m.toMap());
            }
        }

        // Save Personal
        Set<UUID> allUuids = new HashSet<>();
        allUuids.addAll(personalMails.keySet());
        allUuids.addAll(clearedGlobalMails.keySet());
        allUuids.addAll(readGlobalMails.keySet());

        for (UUID uuid : allUuids) {
            String path = "inbox." + uuid.toString();
            List<Mail> mails = personalMails.get(uuid);
            if (mails != null) {
                for (Mail m : mails) {
                    if (!m.isExpired()) {
                        mailsConfig.set(path + ".mails." + m.getId(), m.toMap());
                    }
                }
            }
            Set<String> cleared = clearedGlobalMails.get(uuid);
            if (cleared != null && !cleared.isEmpty()) {
                mailsConfig.set(path + ".cleared_global", new ArrayList<>(cleared));
            }
            Set<String> read = readGlobalMails.get(uuid);
            if (read != null && !read.isEmpty()) {
                mailsConfig.set(path + ".read_global", new ArrayList<>(read));
            }
        }

        try {
            mailsConfig.save(mailsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save mails.yml", e);
        }
    }

    public void saveAsync() {
        Bukkit.getAsyncScheduler().runNow(plugin, task -> saveSync());
    }

    public void sendMail(@NotNull UUID targetUuid,
                         @NotNull String senderName,
                         @Nullable UUID senderUuid,
                         @NotNull String message) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Mail mail = new Mail(id, senderName, senderUuid, System.currentTimeMillis(), 0L, message, false, false);
        personalMails.computeIfAbsent(targetUuid, k -> Collections.synchronizedList(new ArrayList<>())).add(mail);
        saveAsync();

        Player target = Bukkit.getPlayer(targetUuid);
        if (target != null && target.isOnline()) {
            LanguageHook.sendMessage(target, "mail.received_notification", Placeholder.unparsed("sender", senderName));
            if (configManager.isSoundsEnabled()) {
                target.playSound(target.getLocation(), configManager.getSoundActionSuccess(), 1.0f, 1.0f);
            }
        }
    }

    public void sendGlobalMail(@NotNull String senderName,
                               @Nullable UUID senderUuid,
                               long durationSeconds,
                               @NotNull String message) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        long expireTimestamp = durationSeconds > 0 ? (System.currentTimeMillis() + (durationSeconds * 1000L)) : 0L;
        Mail mail = new Mail(id, senderName, senderUuid, System.currentTimeMillis(), expireTimestamp, message, false, true);
        globalMails.add(mail);
        saveAsync();

        for (Player p : Bukkit.getOnlinePlayers()) {
            LanguageHook.sendMessage(p, "mail.received_broadcast_notification", Placeholder.unparsed("sender", senderName));
            if (configManager.isSoundsEnabled()) {
                p.playSound(p.getLocation(), configManager.getSoundActionSuccess(), 1.0f, 1.0f);
            }
        }
    }

    @NotNull
    public List<Mail> getPlayerMails(@NotNull UUID uuid) {
        List<Mail> combined = new ArrayList<>();
        Set<String> cleared = clearedGlobalMails.getOrDefault(uuid, Collections.emptySet());
        Set<String> readGlobal = readGlobalMails.getOrDefault(uuid, Collections.emptySet());

        // Personal mails
        List<Mail> personal = personalMails.get(uuid);
        if (personal != null) {
            for (Mail m : personal) {
                if (!m.isExpired()) {
                    combined.add(m);
                }
            }
        }

        // Global mails
        for (Mail m : globalMails) {
            if (!m.isExpired() && !cleared.contains(m.getId())) {
                boolean isRead = readGlobal.contains(m.getId());
                Mail copy = new Mail(m.getId(), m.getSenderName(), m.getSenderUuid(),
                        m.getTimestamp(), m.getExpireTimestamp(), m.getMessage(), isRead, true);
                combined.add(copy);
            }
        }

        // Sort descending by timestamp (newest first)
        combined.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return combined;
    }

    public int getUnreadCount(@NotNull UUID uuid) {
        int count = 0;
        List<Mail> personal = personalMails.get(uuid);
        if (personal != null) {
            for (Mail m : personal) {
                if (!m.isExpired() && !m.isRead()) {
                    count++;
                }
            }
        }

        Set<String> cleared = clearedGlobalMails.getOrDefault(uuid, Collections.emptySet());
        Set<String> readGlobal = readGlobalMails.getOrDefault(uuid, Collections.emptySet());
        for (Mail m : globalMails) {
            if (!m.isExpired() && !cleared.contains(m.getId()) && !readGlobal.contains(m.getId())) {
                count++;
            }
        }

        return count;
    }

    public void markAllRead(@NotNull UUID uuid) {
        List<Mail> personal = personalMails.get(uuid);
        if (personal != null) {
            for (Mail m : personal) {
                m.setRead(true);
            }
        }

        Set<String> readSet = readGlobalMails.computeIfAbsent(uuid, k -> new HashSet<>());
        for (Mail m : globalMails) {
            if (!m.isExpired()) {
                readSet.add(m.getId());
            }
        }

        saveAsync();
    }

    public boolean clearMail(@NotNull UUID uuid, int index1Based) {
        List<Mail> mails = getPlayerMails(uuid);
        if (index1Based < 1 || index1Based > mails.size()) {
            return false;
        }

        Mail target = mails.get(index1Based - 1);
        if (target.isGlobal()) {
            clearedGlobalMails.computeIfAbsent(uuid, k -> new HashSet<>()).add(target.getId());
        } else {
            List<Mail> personal = personalMails.get(uuid);
            if (personal != null) {
                personal.removeIf(m -> m.getId().equals(target.getId()));
            }
        }

        saveAsync();
        return true;
    }

    public void clearAll(@NotNull UUID uuid) {
        personalMails.remove(uuid);
        Set<String> cleared = clearedGlobalMails.computeIfAbsent(uuid, k -> new HashSet<>());
        for (Mail m : globalMails) {
            cleared.add(m.getId());
        }
        saveAsync();
    }

    public void clearPlayerMails(@NotNull UUID targetUuid) {
        clearAll(targetUuid);
    }
}
