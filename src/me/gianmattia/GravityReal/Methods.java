package me.gianmattia.GravityReal;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Objects;

public class Methods {
    static boolean isGameStarted = false;

    public static void startGame() {
        if(!isGameStarted) {
            isGameStarted = true;

            /* **********************************************
                        Random Map Generation
             ********************************************** */

            //Number of maps that players will play in a Game (can be set in the config)
            int [] gameMaps = new int[Main.getInstance().config.getInt("maps-per-game")];
            System.out.println("gameMaps Length: " + gameMaps.length);

            //Number of Maps in the config
            int numberMaps = Objects.requireNonNull(Main.getInstance().config.getConfigurationSection("maps")).getKeys(false).size();

            //Check if someone inserted a wrong value to 'maps-per-game' in config
            //The code can't run if 'maps-per-game' are set to 5 but there are 3 maps set
            if(gameMaps.length < numberMaps) {
                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "There are too few maps to let the game starts. Check your 'maps-per-game' in config, or create new maps.");
                isGameStarted = false;
                return;
            }

            //Array based on the number of maps
            ArrayList<Integer> tempNumberList = new ArrayList<>(numberMaps);
            for (int i = 0; i <= 10; i++){
                tempNumberList.add(i);
            }
            for (int count = 0; count < gameMaps.length; count++){
                gameMaps[count] = tempNumberList.remove((int)(Math.random() * tempNumberList.size() + 1));
                System.out.println("gameMaps: " + gameMaps[count]);
            }


        }
    }
}
