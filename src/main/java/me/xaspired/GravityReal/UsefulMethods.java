package me.xaspired.GravityReal;


import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

import static org.bukkit.Bukkit.getServer;

public class UsefulMethods {

    /* **********************************************
             Check if min players are satisfied
    ********************************************** */
    public static boolean areMinPlayersOnline() {
        return Bukkit.getOnlinePlayers().size() >= Main.getInstance().config.getInt("min-players");
    }

    /* **********************************************
             Return Formatted Time
    ********************************************** */
    public static String returnTimeFormatted(int seconds) {
        int sec = seconds % 60;
        int min = (seconds / 60) % 60;

        String strSec = (sec < 10) ? "0" + sec : Integer.toString(sec);
        String strmin = (min < 10) ? "0" + min : Integer.toString(min);

        return (strmin + ":" + strSec);
    }

    /* **********************************************
             Teleport Player
    ********************************************** */
    public static void teleportPlayer(Player player, World map, double x, double y, double z, float yaw, float pitch) {
        player.teleport(new Location(map, x, y, z, yaw, pitch));
    }
}
