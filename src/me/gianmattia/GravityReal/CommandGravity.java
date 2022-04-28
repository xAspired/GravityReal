package me.gianmattia.GravityReal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;


public class CommandGravity implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //If the command is sent by console e.g.
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is runnable only by players");
            return true;
        }

        Player player = (Player) sender;

        //If the command doesn't have any arguments (is one and stop e.g /gravity, /setspawnlobby)
        if (args.length == 0) {

            /* **********************************************
                SetSpawnLobby Command (for the Lobby tho)
             ********************************************** */

            if(command.getName().equalsIgnoreCase("setspawnlobby")) {

                Main.getInstance().getConfig().set("lobbyspawn.spawnpoint.world", player.getLocation().getWorld().getName());
                Main.getInstance().getConfig().set("lobbyspawn.spawnpoint.x", player.getLocation().getX());
                Main.getInstance().getConfig().set("lobbyspawn.spawnpoint.y", player.getLocation().getY());
                Main.getInstance().getConfig().set("lobbyspawn.spawnpoint.z", player.getLocation().getZ());
                Main.getInstance().getConfig().set("lobbyspawn.spawnpoint.pitch", player.getLocation().getPitch());
                Main.getInstance().getConfig().set("lobbyspawn.spawnpoint.yaw", player.getLocation().getYaw());
                Main.getInstance().saveDefaultConfig();
                player.sendMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + ": Lobby Spawnpoint Set!");
                return true;
            }

            /* **********************************************
                        Spawn Command (for the Lobby)
             ********************************************** */

            else if(command.getName().equalsIgnoreCase("spawn")) {

                //If the spawn is not set
                if(Objects.equals(Main.getInstance().getConfig().getString("lobbyspawn.spawnpoint.world"), "0")) {
                    player.sendMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + ": Spawn is not set!");
                }
                else {

                    //Create a new virtual object named world, that names is the same as the one in the config
                    World Lobby = Bukkit.getServer().getWorld(Main.getInstance().getConfig().getString("lobbyspawn.spawnpoint.world"));

                    //Coords taken from the conf.yml file
                    double x = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.x");
                    double y = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.y");
                    double z = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.z");
                    double yaw = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.yaw");
                    double pitch = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.pitch");
                    player.teleport(new Location(Lobby, x, y, z, (float) yaw, (float) pitch));
                }
                return true;

            }

            /* **********************************************
                    Help List of Commands
             ********************************************** */

            else {
                // Command Gravity Admin
                player.sendMessage(ChatColor.GRAY + "|--------------------------------------------|");
                player.sendMessage(ChatColor.AQUA + "               Gra" + ChatColor.GREEN + "vity " + ChatColor.GRAY + "by Gianmattia");
                player.sendMessage(ChatColor.GRAY + "        Here are a list of command you can type");
                player.sendMessage(" ");
                player.sendMessage(ChatColor.AQUA + " /gravity reload" + ChatColor.GRAY + " - Reloads the plugin");
                player.sendMessage(ChatColor.AQUA + " /setspawnlobby" + ChatColor.GRAY + " - Sets the spawn of the Lobby");
                player.sendMessage(ChatColor.AQUA + " /gravity createmap <name>" + ChatColor.GRAY + " - Lets you create a new Map");
                player.sendMessage(ChatColor.AQUA + " /gravity setmapspawn <map>" + ChatColor.GRAY + " - Lets you set the map spawnpoint");
                player.sendMessage(ChatColor.AQUA + " /gravity deletemap <name>" + ChatColor.GRAY + " - Remove an existent map");
                player.sendMessage(ChatColor.GRAY + "|--------------------------------------------|");
                return true;
            }
        }

        /* **********************************************
                     /gravity createmap <map> - Command
        ********************************************** */
        else if (args[0].equalsIgnoreCase("createmap")) {
            String nameMap;
            int numberMaps;

            //Check that player have wrote the name of the map
            try {
                nameMap = args[1];
            }
            catch (Exception e) {
                player.sendMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + ": You must declare a name for your new map!");
                return true;
            }
            boolean isFor = false;

            //Check if there are maps in the config
            try {
                numberMaps = Main.getInstance().config.getConfigurationSection("maps").getKeys(false).size();
            }
            catch (Exception e) {
                numberMaps = 0;
            }

            System.out.println("Number of already created Maps: " + numberMaps);

            int numberMap = 0;
            for(int i = 1; i <= numberMaps; i++) {

                /*
                 * sender.sendMessage("Nome della mappa: " + Main.getInstance().getConfig().getString("maps." + i + ".name")); Print maps name
                 * sender.sendMessage("Prova valore: " + Main.getInstance().config.getConfigurationSection("maps").getKeys(false)); Print only the title values
                 * sender.sendMessage("Prova valore: " + Main.getInstance().config.getConfigurationSection("maps").getKeys(true)); Print all values
                 */

                //If the name of the map already exists
                try {
                    if (Main.getInstance().getConfig().getString("maps." + i + ".name").equalsIgnoreCase(nameMap)) {

                        //Set isFor to true if the name of the map already exists
                        isFor = true;
                    }
                } catch (Exception e) {}
                numberMap = i;
            }


            if(!isFor) {
                System.out.println("The name of the map isn't already taken: " + nameMap);
                numberMap++;

                //Adding blank values under the name of the map. Then, when someone type /gravity setmapspawn <map> the values are overwritten
                //I need to add a number first, that's the header of the map (e.g. 3)


                Main.getInstance().getConfig().set("maps." + numberMap + ".name", nameMap);
                Main.getInstance().getConfig().set("maps." + numberMap + ".spawnpoint.world", 0);
                Main.getInstance().getConfig().set("maps." + numberMap + ".spawnpoint.x", 0);
                Main.getInstance().getConfig().set("maps." + numberMap + ".spawnpoint.y", 0);
                Main.getInstance().getConfig().set("maps." + numberMap + ".spawnpoint.z", 0);
                Main.getInstance().getConfig().set("maps." + numberMap + ".spawnpoint.pitch", 0);
                Main.getInstance().getConfig().set("maps." + numberMap + ".spawnpoint.yaw", 0);
                Main.getInstance().saveConfig();
                player.sendMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + ": Map " + ChatColor.AQUA + nameMap + ChatColor.GRAY +  " created!");

            }
            else {
                player.sendMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + ": The map already exists!");
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
            }
            catch (Exception e) {
                player.sendMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + ": You must declare a name of a map!");
                return true;
            }
            boolean isFor = false;

            int numberMaps = Main.getInstance().config.getConfigurationSection("maps").getKeys(false).size();

            for(int i = 1; i <= numberMaps; i++) {

                //If the name of the map already exists
                if(Main.getInstance().getConfig().getString("maps." + i + ".name").equalsIgnoreCase(nameMap)) {

                    Main.getInstance().getConfig().set("maps." + i + ".spawnpoint.world", player.getLocation().getWorld().getName());
                    Main.getInstance().getConfig().set("maps." + i + ".spawnpoint.x", player.getLocation().getX());
                    Main.getInstance().getConfig().set("maps." + i + ".spawnpoint.y", player.getLocation().getY());
                    Main.getInstance().getConfig().set("maps." + i + ".spawnpoint.z", player.getLocation().getZ());
                    Main.getInstance().getConfig().set("maps." + i + ".spawnpoint.pitch", player.getLocation().getPitch());
                    Main.getInstance().getConfig().set("maps." + i + ".spawnpoint.yaw", player.getLocation().getYaw());
                    Main.getInstance().saveConfig();

                    player.sendMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + ": Spawn for map " + ChatColor.AQUA + nameMap + ChatColor.GRAY + " set!");

                    //Set isFor to true if the name of the map already exists
                    isFor = true;
                }
            }

            if(!isFor)
                player.sendMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + ": The map doesn't exist! Please be sure to create one first with /gravity createmap <map>");

            return true;
        }

        /* **********************************************
                     /gravity reload Command
        ********************************************** */

        else if(args[0].equalsIgnoreCase("reload")) {
            Main.getInstance().reloadConfig();
            player.sendMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + ": Config reloaded!");
            return true;
        }

        player.sendMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + ": Incorrect Syntax! Type /gravity for help");
        return true;
    }
}
