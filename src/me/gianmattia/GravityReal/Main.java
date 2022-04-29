package me.gianmattia.GravityReal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin implements Listener {
    FileConfiguration config = getConfig();
    public static Main mainInstance;


    @Override
    public void onEnable() {
        mainInstance = this;

        // Save a copy of the default config.yml if one is not there
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();


        //New command
        getCommand("gravity").setExecutor(new CommandGravity());
        getCommand("setspawnlobby").setExecutor(new CommandGravity());
        getCommand("spawn").setExecutor(new CommandGravity());
        getCommand("debug").setExecutor(new CommandGravity());

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
        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "|| " + ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity " + ChatColor.DARK_GRAY + "| " + ChatColor.GRAY + event.getPlayer().getName() + ChatColor.YELLOW + " joined the game " + ChatColor.RED + "(" + Bukkit.getOnlinePlayers().size() + "/" + maxPlayers + ")");

        //If the min of players are the ones inserted in the config
        if(Bukkit.getOnlinePlayers().size() == minPlayers)
            Methods.startGame();

    }

    public void onPlayerQuit(PlayerQuitEvent event) {
        //Remove basic "Player joined the game" message
        event.setQuitMessage(null);
    }
}

