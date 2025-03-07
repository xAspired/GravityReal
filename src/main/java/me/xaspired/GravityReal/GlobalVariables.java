package me.xaspired.GravityReal;

import org.bukkit.ChatColor;

import java.util.Objects;

public class GlobalVariables {
    public static String pluginPrefix = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Main.getInstance().getConfig().getString("game-prefix")));
    public static String joinMessage = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Main.getInstance().getConfig().getString("message-join")));
}
