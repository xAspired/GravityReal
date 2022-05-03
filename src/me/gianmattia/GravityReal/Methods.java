package me.gianmattia.GravityReal;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

import static org.bukkit.Bukkit.getServer;
import static org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH;

public class Methods {
    static public boolean isGameStarted = false;

    public static void startGame() {
        if(!isGameStarted) {
            isGameStarted = true;

            /* **********************************************
                        Random Map Generation
             ********************************************** */

            int numberMaps;

            //Number of maps players will play in a Game (can be set in the config)
            String [] nameMaps = new String[Main.getInstance().config.getInt("maps-per-game")];
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
                    nameMapsConcatenated.append(nameMaps[count]);
                    if(count < nameMaps.length - 1)
                        nameMapsConcatenated.append(" - ");
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
                            teleportAll(player, firstMap, x, y, z, (float) yaw, (float) pitch);
                            player.setMaxHealth(2);
                            player.setHealthScale(2);
                        }
                        cancel();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 20, 20);

        }
    }
    public static void teleportAll(Player player, World map, double x, double y, double z, float yaw, float pitch) {
        player.teleport(new Location(map, x, y, z, yaw, pitch));
    }


}
