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
    static public boolean isGameStarted = false;
    static public boolean isGameEnded = false;
    public static int playerInGame = 0;
    public static String [] nameMaps;
    public static HashMap<String, Integer> map = new HashMap<>();


    public static void startGame() {
        if(!isGameStarted) {
            isGameStarted = true;

            /* **********************************************
                        Random Map Generation
             ********************************************** */

            int numberMaps;

            //Number of maps players will play in a Game (can be set in the config)
            nameMaps = new String[Main.getInstance().config.getInt("maps-per-game")];
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
            if(numberMaps >= nameMaps.length) {
                //Array based on the number of maps
                ArrayList<Integer> tempNumberList = new ArrayList<>(numberMaps);
                for (int i = 0; i < numberMaps; i++) {
                    tempNumberList.add(i);
                }
                for (int count = 0; count < nameMaps.length; count++) {
                    nameMaps[count] = (String) Main.getInstance().config.getConfigurationSection("maps").getKeys(false).toArray()[tempNumberList.remove((int) (Math.random() * tempNumberList.size()))];
                    System.out.println("nameMaps[" + count + "]: " + nameMaps[count]);

                    //Different Color for Different Difficulty
                    //Try and catch for those who wrongly remove manually the "difficulty" string from config file
                    try {
                        if (Main.getInstance().getConfig().getString("maps." + nameMaps[count] + ".difficulty").equalsIgnoreCase("easy"))
                            nameMapsConcatenated.append(ChatColor.GREEN).append(nameMaps[count]);
                        else if (Main.getInstance().getConfig().getString("maps." + nameMaps[count] + ".difficulty").equalsIgnoreCase("medium"))
                            nameMapsConcatenated.append(ChatColor.YELLOW).append(nameMaps[count]);
                        else if (Main.getInstance().getConfig().getString("maps." + nameMaps[count] + ".difficulty").equalsIgnoreCase("hard"))
                            nameMapsConcatenated.append(ChatColor.RED).append(nameMaps[count]);
                    }
                    catch (Exception e) {
                        nameMapsConcatenated.append(ChatColor.WHITE).append(nameMaps[count]);
                    }

                    //Settings to don't add the minus at the end of the StringBuilder
                    if(count < nameMaps.length - 1)
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
                        playerInGame++;

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
                firstMap = Bukkit.getServer().getWorld(Main.getInstance().getConfig().getString("maps." + nameMaps[0] + ".spawnpoint.world"));
                System.out.println(firstMap);
            }
            catch (Exception e) {
                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "The game couldn't start because there are some maps without spawnpoint. Check in the config and set it with /gravity setmapspawn <map>");
                isGameStarted = false;
                return;
            }

            //Coords taken from the conf.yml file
            double x = Main.getInstance().getConfig().getDouble("maps." + nameMaps[0] + ".spawnpoint.x");
            double y = Main.getInstance().getConfig().getDouble("maps." + nameMaps[0] + ".spawnpoint.y");
            double z = Main.getInstance().getConfig().getDouble("maps." + nameMaps[0] + ".spawnpoint.z");
            double yaw = Main.getInstance().getConfig().getDouble("maps." + nameMaps[0] + ".spawnpoint.yaw");
            double pitch = Main.getInstance().getConfig().getDouble("maps." + nameMaps[0] + ".spawnpoint.pitch");


            /* **********************************************
                            10 Seconds' Countdown
             ********************************************** */


            Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + "Starting " + ChatColor.RED + "countdown" + ChatColor.DARK_GRAY +  "...");
            new BukkitRunnable() {
                int countdownStarter = 10;
                public void run() {
                    Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + countdownStarter);

                    if (--countdownStarter < 0) {
                        //Teleport all players to the first map spawn
                        for (Player player : getServer().getOnlinePlayers()) {
                            teleportPlayer(player, firstMap, x, y, z, (float) yaw, (float) pitch);
                            player.setMaxHealth(6);
                            player.setHealthScale(6);
                        }
                        cancel();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 20, 20);

        }
    }

    public static void endGame(String playerWin) {
        if(!isGameEnded) {
            isGameEnded = true;
            Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.LIGHT_PURPLE + playerWin + ChatColor.YELLOW + " finished the game!");

            new BukkitRunnable() {
                int countdownStarter = 240;
                public void run() {
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


}
