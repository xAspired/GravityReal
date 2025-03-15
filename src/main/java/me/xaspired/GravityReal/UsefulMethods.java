package me.xaspired.GravityReal;


import org.bukkit.Bukkit;

import java.io.File;


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
             Reinitialize Game
    ********************************************** */
    public static void resetGame() {
        GameMethods.status = GameMethods.GameStatus.NOTYETSTARTED;
        GameMethods.countdownReverse = 0;
        GameMethods.isTimerStarted = false;
    }
}
