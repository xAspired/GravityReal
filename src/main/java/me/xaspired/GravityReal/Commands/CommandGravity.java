package me.xaspired.GravityReal.Commands;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import me.xaspired.GravityReal.GameMethods;
import me.xaspired.GravityReal.GlobalVariables;
import me.xaspired.GravityReal.Main;
import me.xaspired.GravityReal.Managers.TeleportManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;


@SuppressWarnings("ConstantConditions")
public class CommandGravity implements CommandExecutor {

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        //If the command is sent by console e.g.
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is runnable only by players");
            return true;
        }

        //If the command doesn't have any arguments (is one and stop e.g /gravity, /setspawnlobby)
        if (args.length == 0) {
            /* **********************************************
                        Spawn Command (for the Lobby)
             ********************************************** */

            if (command.getName().equalsIgnoreCase("spawn")) {
                TeleportManager.teleportPlayer(player, TeleportManager.getLobbySpawn());
            }

            /* **********************************************
                    Debug Command
             ********************************************** */

            else if (command.getName().equalsIgnoreCase("debug")) {
                // create multimap to store key and values
                Multimap<String, String> playerMapTime = ArrayListMultimap.create();

                // put values into map for A
                playerMapTime.put(((Player) sender).getDisplayName(), ((Player) sender).getWorld().getName());
                playerMapTime.put(((Player) sender).getDisplayName(), GameMethods.countdownReverse + "");

                // get all the set of keys
                Set<String> keys = playerMapTime.keySet();

                // iterate through the key set and display key and values
                for (String key : keys) {
                    System.out.println("Key = " + key);
                    System.out.println("Values = " + playerMapTime.get(key).toArray()[0]);
                    System.out.println("Values = " + playerMapTime.get(key).toArray()[1]);
                }

                System.out.println(playerMapTime);
                System.out.println("Countdown: " + GameMethods.countdownReverse);
                return true;
            }


            /* **********************************************
                    Help List of Commands
             ********************************************** */

            else {
                // Command Gravity Admin
                player.sendMessage(ChatColor.GRAY + "|--------------------------------------------|");
                player.sendMessage(ChatColor.AQUA + "               Gra" + ChatColor.GREEN + "vity " + ChatColor.GRAY + "by xAspired");
                player.sendMessage(ChatColor.GRAY + "        Here are a list of command you can type");
                player.sendMessage(" ");
                player.sendMessage(ChatColor.AQUA + " /gravity reload" + ChatColor.GRAY + " - Reloads the plugin");
                player.sendMessage(ChatColor.AQUA + " /gravity setspawnlobby" + ChatColor.GRAY + " - Sets the spawn of the Lobby");
                player.sendMessage(ChatColor.AQUA + " /spawn" + ChatColor.GRAY + " - Teleports yourself to spawnpoint");
                player.sendMessage(ChatColor.AQUA + " /gravity createmap <name>" + ChatColor.GRAY + " - Lets you create a new Map");
                player.sendMessage(ChatColor.AQUA + " /gravity setmapspawn <map>" + ChatColor.GRAY + " - Lets you set the map spawnpoint");
                player.sendMessage(ChatColor.AQUA + " /gravity setmapdiff <map> <diff>" + ChatColor.GRAY + " - Sets the map difficulty");
                player.sendMessage(ChatColor.AQUA + " /gravity deletemap <name>" + ChatColor.GRAY + " - Remove an existent map");
                player.sendMessage(ChatColor.AQUA + " /gravity listmaps" + ChatColor.GRAY + " - Shows a list of maps");
                player.sendMessage(ChatColor.GRAY + "|--------------------------------------------|");
                return true;
            }
        }

        /* **********************************************
                 /gravity createmap <map> - Command
        ********************************************** */
        else if (args[0].equalsIgnoreCase("createmap")) {
            String nameMap;

            //Check that player has written the name of the map
            try {
                nameMap = args[1];
            } catch (Exception e) {
                player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "You must declare a name for your new map!");
                return true;
            }
            boolean isFor = false;


            //If the name of the map already exists
            try {
                if (Main.getInstance().getConfig().getConfigurationSection("maps").getKeys(false).contains(nameMap)) {

                    //Set isFor to true if the name of the map already exists
                    isFor = true;
                }
            } catch (Exception ignored) {}


            if (!isFor) {
                //Adding blank values under the name of the map. Then, when someone type /gravity setmapspawn <map> the values are overwritten
                //I need to add a number first, that's the header of the map (e.g. 3)


                Main.getInstance().getConfig().set("maps." + nameMap, nameMap);
                Main.getInstance().getConfig().set("maps." + nameMap + ".difficulty", 0);
                Main.getInstance().getConfig().set("maps." + nameMap + ".spawnpoint.world", 0);
                Main.getInstance().getConfig().set("maps." + nameMap + ".spawnpoint.x", 0);
                Main.getInstance().getConfig().set("maps." + nameMap + ".spawnpoint.y", 0);
                Main.getInstance().getConfig().set("maps." + nameMap + ".spawnpoint.z", 0);
                Main.getInstance().getConfig().set("maps." + nameMap + ".spawnpoint.pitch", 0);
                Main.getInstance().getConfig().set("maps." + nameMap + ".spawnpoint.yaw", 0);
                Main.getInstance().saveConfig();


                player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "Map " + ChatColor.AQUA + nameMap + ChatColor.GRAY + " created!");

            }
            else {
                player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "The map already exists!");
            }

            return true;
        }

        /* **********************************************
                  /gravity setmapspawn <map> - Command
        ********************************************** */
        else if (args[0].equalsIgnoreCase("setmapspawn")) {
            String nameMap;

            try {
                nameMap = args[1];
            } catch (Exception e) {
                player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "You must declare a name of a map!");
                return true;
            }
            boolean isFor = false;

            //If the name of the map already exists
            if (Main.getInstance().getConfig().getConfigurationSection("maps").getKeys(false).contains(nameMap)) {

                Main.getInstance().getConfig().set("maps." + nameMap + ".spawnpoint.world", player.getLocation().getWorld().getName());
                Main.getInstance().getConfig().set("maps." + nameMap + ".spawnpoint.x", player.getLocation().getX());
                Main.getInstance().getConfig().set("maps." + nameMap + ".spawnpoint.y", player.getLocation().getY());
                Main.getInstance().getConfig().set("maps." + nameMap + ".spawnpoint.z", player.getLocation().getZ());
                Main.getInstance().getConfig().set("maps." + nameMap + ".spawnpoint.pitch", player.getLocation().getPitch());
                Main.getInstance().getConfig().set("maps." + nameMap + ".spawnpoint.yaw", player.getLocation().getYaw());
                Main.getInstance().saveConfig();
                player.getLocation().getWorld().setGameRuleValue("doImmediateRespawn", "true");


                player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "Spawn for map " + ChatColor.AQUA + nameMap + ChatColor.GRAY + " set!");

                //Set isFor to true if the name of the map already exists
                isFor = true;
            }

            if (!isFor)
                player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "The map doesn't exist! Please be sure to create one first with /gravity createmap <map>");

            return true;
        }

        /* **********************************************
                  /gravity setmapdiff <map> - Command
        ********************************************** */
        else if (args[0].equalsIgnoreCase("setmapdiff")) {
            String nameMap;
            String diffMap;


            try {
                nameMap = args[1];
            } catch (Exception e) {
                player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "You must declare a name of a map!");
                return true;
            }

            //Check player inserted the right difficulty
            try {
                diffMap = args[2];
                if (!(diffMap.equalsIgnoreCase("easy") || diffMap.equalsIgnoreCase("medium") || diffMap.equalsIgnoreCase("hard"))) {
                    player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "You must choose a difficulty between [EASY - MEDIUM - HARD]!");
                    return true;
                }
            } catch (Exception e) {
                player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "You must choose a difficulty between [EASY - MEDIUM - HARD]!");
                return true;
            }


            boolean isFor = false;

            //If the name of the map already exists
            if (Main.getInstance().getConfig().getConfigurationSection("maps").getKeys(false).contains(nameMap)) {

                Main.getInstance().getConfig().set("maps." + nameMap + ".difficulty", diffMap);
                Main.getInstance().saveConfig();

                if (diffMap.equalsIgnoreCase("easy"))
                    player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "Difficulty " + ChatColor.GREEN + "Easy" + ChatColor.GRAY + " for map " + ChatColor.AQUA + nameMap + ChatColor.GRAY + " set!");
                else if (diffMap.equalsIgnoreCase("medium"))
                    player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "Difficulty " + ChatColor.YELLOW + "Medium" + ChatColor.GRAY + " for map " + ChatColor.AQUA + nameMap + ChatColor.GRAY + " set!");
                else
                    player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "Difficulty " + ChatColor.RED + "Hard" + ChatColor.GRAY + " for map " + ChatColor.AQUA + nameMap + ChatColor.GRAY + " set!");

                //Set isFor to true if the name of the map already exists
                isFor = true;
            }

            if (!isFor)
                player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "The map doesn't exist! Please be sure to create one first with /gravity createmap <map>");

            return true;
        }

        /* **********************************************
                     /gravity reload Command
        ********************************************** */

        else if (args[0].equalsIgnoreCase("reload")) {
            Main.getInstance().reloadConfig();
            player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "Config reloaded! You may have to restart your server.");
            return true;
        }

        /* **********************************************
                   /gravity listmaps Command
        ********************************************** */
        else if (args[0].equalsIgnoreCase("listmaps")) {

            int numberMaps;

            //Check if there are maps in the config
            try {
                numberMaps = Main.getInstance().getConfig().getConfigurationSection("maps").getKeys(false).size();
            } catch (Exception e) {
                numberMaps = 0;
            }

            if (numberMaps == 0)
                player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "There is no map set yet");
            else {
                player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "Here is the list of maps:");
                player.sendMessage(ChatColor.DARK_GRAY + "|| ");
            }

            for (int i = 0; i < numberMaps; i++) {
                player.sendMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.GRAY + (i + 1) + ChatColor.GRAY + ". " + ChatColor.AQUA + Main.getInstance().getConfig().getConfigurationSection("maps").getKeys(false).toArray()[i]);
            }

            return true;
        }

        /* **********************************************
                 /gravity deletemap <map> Command
        ********************************************** */
        else if (args[0].equalsIgnoreCase("deletemap")) {
            String nameMap;

            try {
                nameMap = args[1];
            } catch (Exception e) {
                player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "You must declare a name of a map!");
                return true;
            }
            boolean isFor = false;

            //If the name of the map already exists
            if (Main.getInstance().getConfig().getConfigurationSection("maps").getKeys(false).contains(nameMap)) {

                Main.getInstance().getConfig().set("maps." + nameMap, null);
                Main.getInstance().saveConfig();


                player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "Map " + ChatColor.AQUA + nameMap + ChatColor.GRAY + " deleted!");

                //Set isFor to true if the name of the map already exists
                isFor = true;
            }

            if (!isFor)
                player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "The map doesn't exist!");

            return true;
        }

        /* **********************************************
                SetSpawnLobby Command (for the Lobby tho)
        ********************************************** */

        else if (args[0].equalsIgnoreCase("setspawnlobby")) {

            Main.getInstance().getConfig().set("lobbyspawn.spawnpoint.world", player.getLocation().getWorld().getName());
            Main.getInstance().getConfig().set("lobbyspawn.spawnpoint.x", player.getLocation().getX());
            Main.getInstance().getConfig().set("lobbyspawn.spawnpoint.y", player.getLocation().getY());
            Main.getInstance().getConfig().set("lobbyspawn.spawnpoint.z", player.getLocation().getZ());
            Main.getInstance().getConfig().set("lobbyspawn.spawnpoint.pitch", player.getLocation().getPitch());
            Main.getInstance().getConfig().set("lobbyspawn.spawnpoint.yaw", player.getLocation().getYaw());
            Main.getInstance().saveConfig();
            player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "Lobby Spawnpoint Set!");
            return true;
        }


        player.sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "Incorrect Syntax! Type /gravity for help");
        return true;
    }
}
