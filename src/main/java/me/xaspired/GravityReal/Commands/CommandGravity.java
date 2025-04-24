package me.xaspired.GravityReal.Commands;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import me.xaspired.GravityReal.GameMethods;
import me.xaspired.GravityReal.Managers.MessagesManager;
import me.xaspired.GravityReal.Main;
import me.xaspired.GravityReal.Managers.MapsManager;
import me.xaspired.GravityReal.Managers.TeleportManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;


@SuppressWarnings("ConstantConditions")
public class CommandGravity implements CommandExecutor {

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
                if (Main.getInstance().inGamePlayers.get(player).getStatus() == GameMethods.PlayerStatus.NONE) {
                    TeleportManager.teleportPlayer(player, TeleportManager.getLobbySpawn());
                    return true;
                }
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

            //Create the maps directory
            File directory = new File("plugins/GravityReal/maps");
            if (!directory.exists())
                directory.mkdir();

            // Check that player has written the name of the map
            try {
                nameMap = args[1];
            } catch (Exception e) {
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "You must declare a name for your new map!");
                return true;
            }

            String fileName = "plugins/GravityReal/maps/" + nameMap + ".json";
            File fileMap = new File(fileName);

            // If the name of the file's map already exists
            if (fileMap.exists()) {
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "This map already exists!");
                return true;
            }

            // Verify the correct creation of the file
            try {
                fileMap.createNewFile();
            } catch (IOException e) {
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "Error while creating map file. Try asking to the Dev.");
                return true;
            }

            JSONObject mapData = MapsManager.initializeMap(nameMap, Main.getInstance().getConfig().getInt("max-players"));

            // JSON File writer
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(mapData.toString(4));
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "Map saved in " + ChatColor.YELLOW  + fileName + ChatColor.GRAY + "!");
            } catch (IOException e) {
                System.out.println("Error while creating map file: " + e.getMessage());
            }

            return true;
        }

        /* **********************************************
                  /gravity setmapspawn <map> - Command
        ********************************************** */
        else if (args[0].equalsIgnoreCase("setmapspawn")) {

            // Arguments < 3 so there isn't any spawnIndex set
            if (args.length < 3) {
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.RED + "Usage: /gravity setmapspawn <mapname> <spawnIndex>");
                return true;
            }

            // Get name Map from argument 1
            String nameMap = args[1];
            int spawnIndex;

            try {
                // Get spawn index from argument 2
                spawnIndex = Integer.parseInt(args[2]);

                // It must be positive
                if (spawnIndex < 1) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.RED + "Invalid spawn index! Must be higher than 0.");
                return true;
            }

            // Set Map Spawn for each index and verify everything is good
            if (!MapsManager.setMapSpawn(nameMap, spawnIndex, player)) {
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.RED + "Failed to set spawn point! Try to setup again the map or ask the dev");
            }
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
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "You must declare a name of a map!");
                return true;
            }

            // Check if the player inserted a valid difficulty
            try {
                diffMap = args[2];
                if (!(diffMap.equalsIgnoreCase("easy") || diffMap.equalsIgnoreCase("medium") || diffMap.equalsIgnoreCase("hard"))) {
                    player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "You must choose a difficulty between [EASY - MEDIUM - HARD]!");
                    return true;
                }
            } catch (Exception e) {
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "You must choose a difficulty between [" + ChatColor.GREEN + "EASY" + ChatColor.GRAY + " - " + ChatColor.YELLOW + "MEDIUM" + ChatColor.GRAY + " - " + ChatColor.RED + "HARD" + ChatColor.GRAY + "]!");
                return true;
            }

            String fileName = "plugins/GravityReal/maps/" + nameMap + ".json";
            File fileMap = new File(fileName);

            if (!fileMap.exists()) {
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "The map doesn't exist! Please be sure to create one first with /gravity createmap <map>");
                return true;
            }

            try {
                String content = new String(Files.readAllBytes(Paths.get(fileName)));
                JSONObject mapData = new JSONObject(content);
                mapData.put("difficulty", diffMap.toLowerCase());

                try (FileWriter fileWriter = new FileWriter(fileName)) {
                    fileWriter.write(mapData.toString(4)); // Pretty print JSON
                }

                ChatColor difficultyColor = diffMap.equalsIgnoreCase("easy") ? ChatColor.GREEN :
                        diffMap.equalsIgnoreCase("medium") ? ChatColor.YELLOW :
                                ChatColor.RED;

                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "Difficulty " + difficultyColor + diffMap.substring(0, 1).toUpperCase() + diffMap.substring(1) +
                        ChatColor.GRAY + " for map " + ChatColor.AQUA + nameMap + ChatColor.GRAY + " set!");

            } catch (IOException e) {
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.RED + "Error updating the map difficulty!");
                e.printStackTrace();
            }

            return true;
        }

        /* **********************************************
                     /gravity reload Command
        ********************************************** */

        else if (args[0].equalsIgnoreCase("reload")) {
            Main.getInstance().reloadConfig();

            // Re-initialize messages checker
            MessagesManager.init();

            player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "Config reloaded! You may have to restart your server.");
            return true;
        }

        /* **********************************************
                   /gravity listmaps Command
        ********************************************** */
        else if (args[0].equalsIgnoreCase("listmaps")) {
            File mapsFolder = new File("plugins/GravityReal/maps/");
            File[] files = mapsFolder.listFiles((dir, name) -> name.endsWith(".json"));

            if (files == null || files.length == 0) {
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.RED + "No maps found!");
                return true;
            }

            player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "Here is the list of maps:");
            player.sendMessage(ChatColor.DARK_GRAY + "|| ");

            for (int i = 0; i < files.length; i++) {
                String mapName = files[i].getName().replace(".json", ""); // Remove .json extension from map names
                player.sendMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.GRAY + (i + 1) + ChatColor.GRAY + ". " + ChatColor.AQUA + mapName);
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
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "You must declare a name of a map!");
                return true;
            }

            File mapFile = new File("plugins/GravityReal/maps/" + nameMap + ".json");

            if (!mapFile.exists()) {
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "The map " + ChatColor.AQUA + nameMap + ChatColor.GRAY + " does not exist!");
                return true;
            }

            if (mapFile.delete()) {
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "Map " + ChatColor.AQUA + nameMap + ChatColor.GRAY + " deleted successfully!");
            } else {
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.RED + "Error deleting the map " + ChatColor.AQUA + nameMap + ChatColor.RED + "!");
            }

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
            player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "Lobby Spawnpoint Set!");
            return true;
        }


        player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "Incorrect Syntax! Type /gravity for help");
        return true;
    }
}
