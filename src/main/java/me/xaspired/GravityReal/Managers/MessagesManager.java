package me.xaspired.GravityReal.Managers;

import me.xaspired.GravityReal.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Objects;
import java.util.Map;

public class MessagesManager {
    public static String pluginPrefix;
    public static String joinMessage;
    public static String teleportingError;
    public static String greetingsPlaying;
    public static String queueTitle;
    public static String welcomeTitle;
    public static String kickReason1;
    public static String yourStats;
    public static String ranking;

    public static void init() {
        pluginPrefix = getOrFallback("messages.game-prefix", "&8|| &bGra&avity&7 &8| &R");
        joinMessage = getOrFallback("messages.message-join", "&7Welcome to our minigame!");
        teleportingError = getOrFallback("messages.teleporting-error", "&7There was a problem teleporting you into the correct spawnpoint. Please report it to a server admin.");
        greetingsPlaying = getOrFallback("messages.greetings-playing", "&bGra&avity");
        queueTitle = getOrFallback("messages.queue-title", "&fYou are now in &e&nqueue");
        welcomeTitle = getOrFallback("messages.welcome-title", "&fWelcome to &bGra&avity");
        kickReason1 = getOrFallback("messages.kick-reason-1", "Game is still in progress");
        yourStats = getOrFallback("messages.scoreboard.your-stats", "&e&lYour Stats:");
        ranking = getOrFallback("messages.scoreboard.ranking", "&3&lRanking:");
    }

    /* **********************************************
             Messages Checker
    ********************************************** */
    private static String getOrFallback(String path, String fallback) {
        String raw = Main.getInstance().getConfig().getString(path);
        if (raw == null) {
            Bukkit.getLogger().warning(pluginPrefix + " Missing config path: " + path + " (using fallback)");
            raw = fallback;
        }
        return ChatColor.translateAlternateColorCodes('&', raw);
    }



    public static String get(String path) {
        return ChatColor.translateAlternateColorCodes('&',
                Main.getInstance().getConfig().getString(path, ""));
    }

    public static String getFormatted(String path, Map<String, String> placeholders) {
        String raw = get(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            raw = raw.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return raw;
    }

}
