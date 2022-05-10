package me.gianmattia.GravityReal;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

import static org.bukkit.Bukkit.getServer;
@SuppressWarnings({"deprecation", "ConstantConditions"})
public class Methods {

    //Game Start/End
    public static boolean isGameStarted = false;
    public static boolean isGameEnded = false;
    public static Player winPlayer;

    //Timer
    public static boolean isTimerStarted = false;
    public static int countdownReverse = 0;

    //Maps
    public static HashMap<String, Integer> mapsIndex = new HashMap<>();
    public static HashMap<Integer, String> indexMaps = new HashMap<>();


    public static void startGame() {
        if(!isGameStarted) {
            isGameStarted = true;
            /* **********************************************
                        Random Map Generation
             ********************************************** */

            int numberMaps;

            //Number of maps players will play in a Game (can be set in the config)
            StringBuilder nameMapsConcatenated = new StringBuilder();

            //Check if the value of numberMaps is null (there aren't maps set in the config)
            try {
                //Number of Maps in the config
                numberMaps = Main.getInstance().config.getConfigurationSection("maps").getKeys(false).size();
            }
            catch (Exception e) {
                numberMaps = 0;
            }

            //Check if someone inserted a wrong value to 'maps-per-game' in config
            //The code can't run if 'maps-per-game' are set to 5 but there are 3 maps set
            if(numberMaps >= Main.getInstance().config.getInt("maps-per-game")) {
                //Array based on the number of maps
                ArrayList<Integer> tempNumberList = new ArrayList<>(numberMaps);
                for (int i = 0; i < numberMaps; i++) {
                    tempNumberList.add(i);
                }
                for (int count = 0; count < Main.getInstance().config.getInt("maps-per-game"); count++) {

                    //Randomized map
                    String nameMapFor = (String) Main.getInstance().config.getConfigurationSection("maps").getKeys(false).toArray()[tempNumberList.remove((int) (Math.random() * tempNumberList.size()))];
                    mapsIndex.put(nameMapFor, count);
                    indexMaps.put(count, nameMapFor);

                    //Different Color for Different Difficulty
                    //Try and catch for those who wrongly remove manually the "difficulty" string from config file
                    try {
                        if (Main.getInstance().getConfig().getString("maps." + indexMaps.get(count) + ".difficulty").equalsIgnoreCase("easy"))
                            nameMapsConcatenated.append(ChatColor.GREEN).append(indexMaps.get(count));
                        else if (Main.getInstance().getConfig().getString("maps." + indexMaps.get(count) + ".difficulty").equalsIgnoreCase("medium"))
                            nameMapsConcatenated.append(ChatColor.YELLOW).append(indexMaps.get(count));
                        else if (Main.getInstance().getConfig().getString("maps." + indexMaps.get(count) + ".difficulty").equalsIgnoreCase("hard"))
                            nameMapsConcatenated.append(ChatColor.RED).append(indexMaps.get(count));
                    }
                    catch (Exception e) {
                        nameMapsConcatenated.append(ChatColor.WHITE).append(indexMaps.get(count));
                    }

                    //Settings to don't add the minus at the end of the StringBuilder
                    if(count < Main.getInstance().config.getInt("maps-per-game") - 1)
                        nameMapsConcatenated.append(ChatColor.WHITE).append(" - ");
                }
            }
            else {
                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "There are too few maps to let the game starts. Check your 'maps-per-game' in config, or create new maps.");
                isGameStarted = false;
                return;
            }

            //Broadcasting that the minPlayers is satisfied
            Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "Minimum number of players reached!");

            //Sending to all players an actionbar that says which maps will be played
            for (Player player : getServer().getOnlinePlayers()) {
                new BukkitRunnable() {
                    int countdownStarter = 2;
                    public void run() {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(String.valueOf(nameMapsConcatenated)));

                        if (--countdownStarter < 0) {
                            cancel();
                        }
                    }
                }.runTaskTimer(Main.getInstance(), 20, 20);
            }

            /* **********************************************
                   Create the Object fot the First Map
             ********************************************** */
            World firstMap;

            //If some spanwpoints are not set:
            try {
                //Create a new virtual world
                firstMap = Bukkit.getServer().getWorld(Main.getInstance().getConfig().getString("maps." + indexMaps.get(0) + ".spawnpoint.world"));
                //System.out.println(firstMap);
            }
            catch (Exception e) {
                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "The game couldn't start because there are some maps without spawnpoint. Check in the config and set it with /gravity setmapspawn <map>");
                isGameStarted = false;
                return;
            }

            //Coords taken from the conf.yml file
            double x = Main.getInstance().getConfig().getDouble("maps." + indexMaps.get(0) + ".spawnpoint.x");
            double y = Main.getInstance().getConfig().getDouble("maps." + indexMaps.get(0) + ".spawnpoint.y");
            double z = Main.getInstance().getConfig().getDouble("maps." + indexMaps.get(0) + ".spawnpoint.z");
            double yaw = Main.getInstance().getConfig().getDouble("maps." + indexMaps.get(0) + ".spawnpoint.yaw");
            double pitch = Main.getInstance().getConfig().getDouble("maps." + indexMaps.get(0) + ".spawnpoint.pitch");


            /* **********************************************
                            10 Seconds' Countdown
             ********************************************** */


            Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "Starting " + ChatColor.RED + "countdown" + ChatColor.DARK_GRAY +  "...");
            new BukkitRunnable() {
                int countdownStarter = 10;
                public void run() {
                    Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + countdownStarter);

                    if (--countdownStarter < 0) {
                        /* **********************************************
                                Teleport All Players to First Map
                        ********************************************** */
                        for (Player player : getServer().getOnlinePlayers()) {
                            teleportPlayer(player, firstMap, x, y, z, (float) yaw, (float) pitch); //Teleport All

                            //Health Setup
                            player.setMaxHealth(6);
                            player.setHealthScale(6);


                            Main.getInstance().createBoard(player); //Creation of the Board
                            timerPlayers(); //Start timer

                        }
                        cancel();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 20, 20);

        }
    }

    public static void endGame(Player playerWin) {
        if(!isGameEnded) {
            isGameEnded = true;
            winPlayer = playerWin;
            Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.LIGHT_PURPLE + playerWin.getName() + ChatColor.YELLOW + " finished the game!");

            new BukkitRunnable() {
                int countdownStarter = 240;
                public void run() {
                    if(!isGameEnded)
                        cancel();

                    if(countdownStarter==240)
                        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "The game will stop in 240 seconds ");
                    else if(countdownStarter==180)
                        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "The game will stop in 180 seconds ");
                    else if(countdownStarter==120)
                        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "The game will stop in 120 seconds ");
                    else if(countdownStarter==60)
                        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "The game will stop in 60 seconds ");
                    else if(countdownStarter==3)
                        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "The game will stop in 3 seconds ");
                    else if(countdownStarter==2)
                        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "The game will stop in 2 seconds ");
                    else if(countdownStarter==1)
                        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "The game will stop in 1 seconds ");

                    if (--countdownStarter < 0) {
                        for (Player player : getServer().getOnlinePlayers()) {
                            player.performCommand("spawn");
                        }
                        cancel();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 20, 20);
        }
    }

    public static void teleportPlayer(Player player, World map, double x, double y, double z, float yaw, float pitch) {
        player.teleport(new Location(map, x, y, z, yaw, pitch));
    }

    public static int timerPlayers() {

        if (!(isTimerStarted)) {
            isTimerStarted = true;
        /* **********************************************
                                Timer
        ********************************************** */
            new BukkitRunnable() {
                public void run() {

                    if (++countdownReverse > Main.getInstance().getConfig().getInt("duration-time")) {
                        cancel();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 20, 20);

        }

        return countdownReverse;
    }

    public static String returnTimeFormatted(int seconds) {
        int sec = seconds % 60;
        int min = (seconds / 60)%60;

        String strSec=(sec<10)?"0"+ sec :Integer.toString(sec);
        String strmin=(min<10)?"0"+ min :Integer.toString(min);

        return(strmin + ":" + strSec);
    }


}
