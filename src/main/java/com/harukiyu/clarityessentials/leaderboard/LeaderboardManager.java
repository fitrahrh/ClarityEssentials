package com.harukiyu.clarityessentials.leaderboard;

import com.harukiyu.clarityessentials.ClarityEssentials;
import com.harukiyu.clarityessentials.hook.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.*;

public class LeaderboardManager {

    public record Entry(String name, String formattedValue, double rawValue) {}

    private final DecimalFormat countFormat = new DecimalFormat("#,##0");

    public LeaderboardManager(@NotNull ClarityEssentials plugin) {
    }

    @NotNull
    public List<Entry> getTopEntries(@NotNull String category, int limit) {
        List<Entry> results = new ArrayList<>();
        OfflinePlayer[] players = Bukkit.getOfflinePlayers();

        switch (category.toLowerCase(Locale.ROOT)) {
            case "money" -> {
                List<Map.Entry<String, Double>> list = new ArrayList<>();
                for (OfflinePlayer op : players) {
                    if (op.getName() == null) continue;
                    double bal = VaultHook.getBalance(op);
                    list.add(new AbstractMap.SimpleEntry<>(op.getName(), bal));
                }
                list.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
                for (int i = 0; i < Math.min(limit, list.size()); i++) {
                    var e = list.get(i);
                    results.add(new Entry(e.getKey(), "$ " + VaultHook.formatMoney(e.getValue()), e.getValue()));
                }
            }
            case "deaths" -> {
                List<Map.Entry<String, Integer>> list = new ArrayList<>();
                for (OfflinePlayer op : players) {
                    if (op.getName() == null) continue;
                    int deaths = op.getStatistic(Statistic.DEATHS);
                    list.add(new AbstractMap.SimpleEntry<>(op.getName(), deaths));
                }
                list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
                for (int i = 0; i < Math.min(limit, list.size()); i++) {
                    var e = list.get(i);
                    results.add(new Entry(e.getKey(), countFormat.format(e.getValue()), e.getValue()));
                }
            }
            case "playtime" -> {
                List<Map.Entry<String, Integer>> list = new ArrayList<>();
                for (OfflinePlayer op : players) {
                    if (op.getName() == null) continue;
                    int ticks = op.getStatistic(Statistic.PLAY_ONE_MINUTE);
                    list.add(new AbstractMap.SimpleEntry<>(op.getName(), ticks));
                }
                list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
                for (int i = 0; i < Math.min(limit, list.size()); i++) {
                    var e = list.get(i);
                    int hours = e.getValue() / 72000;
                    results.add(new Entry(e.getKey(), hours + "h", e.getValue()));
                }
            }
            case "blocks_broken" -> {
                List<Map.Entry<String, Integer>> list = new ArrayList<>();
                for (OfflinePlayer op : players) {
                    if (op.getName() == null) continue;
                    // Aggregate break block stat
                    int mined = 0;
                    try {
                        mined = op.getStatistic(Statistic.MINE_BLOCK, org.bukkit.Material.STONE);
                    } catch (IllegalArgumentException ignored) {}
                    list.add(new AbstractMap.SimpleEntry<>(op.getName(), mined));
                }
                list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
                for (int i = 0; i < Math.min(limit, list.size()); i++) {
                    var e = list.get(i);
                    results.add(new Entry(e.getKey(), countFormat.format(e.getValue()), e.getValue()));
                }
            }
            case "mobs_killed" -> {
                List<Map.Entry<String, Integer>> list = new ArrayList<>();
                for (OfflinePlayer op : players) {
                    if (op.getName() == null) continue;
                    int kills = op.getStatistic(Statistic.MOB_KILLS);
                    list.add(new AbstractMap.SimpleEntry<>(op.getName(), kills));
                }
                list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
                for (int i = 0; i < Math.min(limit, list.size()); i++) {
                    var e = list.get(i);
                    results.add(new Entry(e.getKey(), countFormat.format(e.getValue()), e.getValue()));
                }
            }
            default -> {
                // Blocks placed or generic
                List<Map.Entry<String, Integer>> list = new ArrayList<>();
                for (OfflinePlayer op : players) {
                    if (op.getName() == null) continue;
                    int kills = op.getStatistic(Statistic.PLAYER_KILLS);
                    list.add(new AbstractMap.SimpleEntry<>(op.getName(), kills));
                }
                list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
                for (int i = 0; i < Math.min(limit, list.size()); i++) {
                    var e = list.get(i);
                    results.add(new Entry(e.getKey(), countFormat.format(e.getValue()), e.getValue()));
                }
            }
        }

        if (results.isEmpty()) {
            results.add(new Entry("-", "0", 0));
        }

        return results;
    }
}
