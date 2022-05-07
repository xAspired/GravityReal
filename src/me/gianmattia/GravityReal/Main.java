package me.gianmattia.GravityReal;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;

@SuppressWarnings("ConstantConditions")
public class Main extends JavaPlugin implements Listener {
    FileConfiguration config = getConfig();
    public static Main mainInstance;
    public HashMap<Player, Integer> maps = new HashMap<>();


    @Override
    public void onEnable() {
        mainInstance = this;

        // Save a copy of the default config.yml if one is not there
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();


        //New command - Gravity
        getCommand("gravity").setExecutor(new CommandGravity());
        getCommand("spawn").setExecutor(new CommandGravity());
        getCommand("debug").setExecutor(new CommandGravity());

        //New command - Utility
        getCommand("coords").setExecutor(new CommandUtility());
        getCommand("gmc").setExecutor(new CommandUtility());

        // Enable our class to check for new players using onPlayerJoin()
        getServer().getPluginManager().registerEvents(this, this);


        //Send a message that shows that the plugin was enabled successfully
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + " by Gianmattia -  " + "Plugin Enabled Successfully!");

    }

    public static Main getInstance() {
        return mainInstance;
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + " by Gianmattia - " +  "Plugin Disabled Successfully!");
    }

    // This method checks for incoming players and sends them a message
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int maxPlayers = config.getInt("max-players");
        int minPlayers = config.getInt("min-players");


        Player player = event.getPlayer();

        //Remove basic "Player joined the game" message
        event.setJoinMessage(null);


        /* **********************************************
                Teleport Player to 'Lobby Spawn'
             ********************************************** */

        //If the spawnpoint is set
        if(!(Objects.equals(getConfig().get("lobbyspawn.spawnpoint.world"), 0))) {

            //Create a new virtual object named world, that names is the same as the one in the config
            World Lobby = Bukkit.getServer().getWorld(getConfig().getString("lobbyspawn.spawnpoint.world"));

            //Coords taken from the conf.yml file
            double x = getConfig().getDouble("lobbyspawn.spawnpoint.x");
            double y = getConfig().getDouble("lobbyspawn.spawnpoint.y");
            double z = getConfig().getDouble("lobbyspawn.spawnpoint.z");
            double yaw = getConfig().getDouble("lobbyspawn.spawnpoint.yaw");
            double pitch = getConfig().getDouble("lobbyspawn.spawnpoint.pitch");
            player.teleport(new Location(Lobby, x, y, z, (float) yaw, (float) pitch));
        }

        //Send the custom message write in the config in "message-join"
        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("message-join")));
        event.getPlayer().sendTitle("§fWelcome to §bGra§avity", "§fYou are now in §e§nqueue", 10, 80, 10);

        //Send the join message
        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.LIGHT_PURPLE + event.getPlayer().getName() + ChatColor.YELLOW + " joined the game " + ChatColor.RED + "(" + Bukkit.getOnlinePlayers().size() + "/" + maxPlayers + ")");

        //If the min of players are the ones inserted in the config
        if(Bukkit.getOnlinePlayers().size() == minPlayers)
            Methods.startGame();

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        //Remove basic "Player joined the game" message
        event.setQuitMessage(null);

        //If there is no one on the Server, Game will stop
        if(Bukkit.getOnlinePlayers().size() == 1) {
            Methods.isGameStarted = false;
            Methods.isGameEnded = false;
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(Methods.isGameStarted) {
            if (event.getTo().getBlock().getType() == Material.NETHER_PORTAL) {
                event.setCancelled(true);

                //Check if the map of the player is equal to the last map
                //I verify that the index of the given maps (I take the world of event player) is equal to the number written in config minus one
                if (Methods.mapsIndex.get(event.getPlayer().getWorld().getName()).equals(Main.getInstance().config.getInt("maps-per-game") - 1)) {
                    Methods.endGame(event.getPlayer().getName());

                    //Teleport him on spawn
                    //Coords taken from the conf.yml file
                    World Lobby = Bukkit.getServer().getWorld(getConfig().getString("lobbyspawn.spawnpoint.world"));
                    double x = getConfig().getDouble("lobbyspawn.spawnpoint.x");
                    double y = getConfig().getDouble("lobbyspawn.spawnpoint.y");
                    double z = getConfig().getDouble("lobbyspawn.spawnpoint.z");
                    double yaw = getConfig().getDouble("lobbyspawn.spawnpoint.yaw");
                    double pitch = getConfig().getDouble("lobbyspawn.spawnpoint.pitch");
                    Methods.teleportPlayer(event.getPlayer(), Lobby, x, y, z, (float) yaw, (float) pitch);

                    //Ten Ticks delay that allows right teleport
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Methods.teleportPlayer(event.getPlayer(), Lobby, x, y, z, (float) yaw, (float) pitch);
                        }
                    }.runTaskLater(this, 10L);
                }
                //Verify that the result of the next map is not null
                else if(!(Methods.indexMaps.get((Methods.mapsIndex.get(event.getPlayer().getWorld().getName()) + 1)).isEmpty())){
                    World map = Bukkit.getServer().getWorld(Main.getInstance().getConfig().getString("maps." + Methods.indexMaps.get((Methods.mapsIndex.get(event.getPlayer().getWorld().getName()) + 1)) + ".spawnpoint.world"));
                    double x = Main.getInstance().getConfig().getDouble("maps." + Methods.indexMaps.get((Methods.mapsIndex.get(event.getPlayer().getWorld().getName()) + 1)) + ".spawnpoint.x");
                    double y = Main.getInstance().getConfig().getDouble("maps." + Methods.indexMaps.get((Methods.mapsIndex.get(event.getPlayer().getWorld().getName()) + 1)) + ".spawnpoint.y");
                    double z = Main.getInstance().getConfig().getDouble("maps." + Methods.indexMaps.get((Methods.mapsIndex.get(event.getPlayer().getWorld().getName()) + 1)) + ".spawnpoint.z");
                    double yaw = Main.getInstance().getConfig().getDouble("maps." + Methods.indexMaps.get((Methods.mapsIndex.get(event.getPlayer().getWorld().getName()) + 1)) + ".spawnpoint.yaw");
                    double pitch = Main.getInstance().getConfig().getDouble("maps." + Methods.indexMaps.get((Methods.mapsIndex.get(event.getPlayer().getWorld().getName()) + 1)) + ".spawnpoint.pitch");
                    Methods.teleportPlayer(event.getPlayer(), map, x, y, z, (float) yaw, (float) pitch);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Methods.teleportPlayer(event.getPlayer(), map, x, y, z, (float) yaw, (float) pitch);
                        }
                    }.runTaskLater(this, 10L);
                }
            }
        }
    }



}

