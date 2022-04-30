package me.gianmattia.GravityReal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Methods {
    static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    static boolean isGameStarted = false;

    public static void startGame() {
        if(!isGameStarted) {
            isGameStarted = true;

            /* **********************************************
                        Random Map Generation
             ********************************************** */

            //Number of maps players will play in a Game (can be set in the config)
            int [] gameMaps = new int[Main.getInstance().config.getInt("maps-per-game")];

            //Number of Maps in the config
            int numberMaps = Main.getInstance().config.getConfigurationSection("maps").getKeys(false).size();

            //Check if someone inserted a wrong value to 'maps-per-game' in config
            //The code can't run if 'maps-per-game' are set to 5 but there are 3 maps set
            try {
                //Array based on the number of maps
                ArrayList<Integer> tempNumberList = new ArrayList<>(numberMaps);
                for (int i = 0; i <= 10; i++) {
                    tempNumberList.add(i);
                }
                for (int count = 0; count < gameMaps.length; count++) {
                    gameMaps[count] = tempNumberList.remove((int) (Math.random() * tempNumberList.size()));
                }
            }
            catch (Exception e) {
                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "There are too few maps to let the game starts. Check your 'maps-per-game' in config, or create new maps.");
                isGameStarted = false;
                return;
            }

            //Broadcasting that the minPlayers is satisfied
            Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "Minimum number of players reached!");
            Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "Starting " + ChatColor.RED + "countdown" + ChatColor.DARK_GRAY +  "...");


            /* **********************************************
                            10 Seconds' Countdown
             ********************************************** */
            final Runnable runnable = new Runnable() {
                int countdownStarter = 10;
                public void run() {

                    Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + countdownStarter);
                    countdownStarter--;

                    if (countdownStarter < 0) {
                        scheduler.shutdown();
                    }
                }
            };
            scheduler.scheduleAtFixedRate(runnable, 0, 1, SECONDS);

        }
    }
}
