package com.harukiyu.clarityessentials.commands;

import com.harukiyu.clarityessentials.config.ConfigManager;
import com.harukiyu.clarityessentials.hook.LanguageHook;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class HealCommand implements BasicCommand {

    private final ConfigManager configManager;

    private static final List<PotionEffectType> NEGATIVE_EFFECTS = List.of(
            PotionEffectType.POISON,
            PotionEffectType.WITHER,
            PotionEffectType.HUNGER,
            PotionEffectType.DARKNESS,
            PotionEffectType.BLINDNESS,
            PotionEffectType.WEAKNESS,
            PotionEffectType.SLOWNESS,
            PotionEffectType.MINING_FATIGUE
    );

    public HealCommand(@NotNull ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) return;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                LanguageHook.sendMessage(sender, "heal.usage_console");
                return;
            }

            if (!player.hasPermission("clarityessentials.command.heal")) {
                LanguageHook.sendMessage(player, "general.no_permission");
                return;
            }

            healPlayer(player);
            LanguageHook.sendMessage(player, "heal.success_self");
            return;
        }

        // Target heal
        if (!sender.hasPermission("clarityessentials.command.heal.others")) {
            LanguageHook.sendMessage(sender, "heal.no_permission_others");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            LanguageHook.sendMessage(sender, "general.player_not_found", Placeholder.unparsed("player", args[0]));
            return;
        }

        healPlayer(target);
        LanguageHook.sendMessage(sender, "heal.success_target", Placeholder.unparsed("target", target.getName()));
        LanguageHook.sendMessage(target, "heal.target_healed", Placeholder.unparsed("sender", sender.getName()));
    }

    private void healPlayer(@NotNull Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = attr != null ? attr.getValue() : 20.0;
        player.setHealth(maxHealth);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setExhaustion(0.0f);
        player.setFireTicks(0);

        for (PotionEffectType effect : NEGATIVE_EFFECTS) {
            player.removePotionEffect(effect);
        }

        if (configManager.isSoundsEnabled()) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (args.length <= 1 && sender != null && sender.hasPermission("clarityessentials.command.heal.others")) {
            String prefix = (args.length == 0) ? "" : args[0].toLowerCase(Locale.ROOT);
            List<String> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    list.add(p.getName());
                }
            }
            return list;
        }
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "clarityessentials.command.heal";
    }
}
