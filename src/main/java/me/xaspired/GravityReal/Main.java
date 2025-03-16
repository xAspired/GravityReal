package me.xaspired.GravityReal;

import me.xaspired.GravityReal.Commands.CommandGravity;
import me.xaspired.GravityReal.Commands.PlayerUtilitiesCommand;
import me.xaspired.GravityReal.Managers.BoardManager;
import me.xaspired.GravityReal.Managers.TeleportManager;
import me.xaspired.GravityReal.Objects.GravityPlayer;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;


@SuppressWarnings("ConstantConditions")
public class Main extends JavaPlugin implements Listener {
    FileConfiguration config = getConfig();
    public static Main mainInstance;


    /* **********************************************
                  Board Variables
    ********************************************** */
    public HashMap<Player, GravityPlayer> inGamePlayers = new HashMap<>();

    @Override
    public void onEnable() {
        mainInstance = this;

        // Save a copy of the default config.yml if one is not there
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // Load a config template only if it doesn't exist
        File configTemplateFile = new File(getDataFolder(), "config_backup.yml");
        if (!configTemplateFile.exists())
            saveResource("config_backup.yml", true);


        //New command - Gravity
        getCommand("gravity").setExecutor(new CommandGravity());
        getCommand("spawn").setExecutor(new CommandGravity());
        getCommand("debug").setExecutor(new CommandGravity());

        //New command - Utility
        getCommand("coords").setExecutor(new PlayerUtilitiesCommand());
        getCommand("gmc").setExecutor(new PlayerUtilitiesCommand());

        // Enable our class to check for new players using onPlayerJoin()
        getServer().getPluginManager().registerEvents(this, this);


        //Send a message that shows that the plugin was enabled successfully
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + " by xAspired -  " + "Plugin Enabled Successfully!");
    }

    public static Main getInstance() {
        return mainInstance;
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + " by xAspired - " + "Plugin Disabled Successfully!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (GameMethods.status == GameMethods.GameStatus.STARTED || GameMethods.status == GameMethods.GameStatus.ENDING) {
            event.getPlayer().kickPlayer("Game is still in progress");
            return;
        }

        int maxPlayers = config.getInt("max-players");

        Player player = event.getPlayer();
        GravityPlayer playerObj = new GravityPlayer(player, GameMethods.PlayerStatus.NONE, 0, 0, 0, 0, 0);
        inGamePlayers.put(player, playerObj);

        //Remove basic "Player joined the game" message
        event.setJoinMessage(null);

        //Teleport player to Lobby Spawn
        TeleportManager.teleportPlayer(player, TeleportManager.getLobbySpawn());

        //Send the custom message write in the config in "message-join"
        event.getPlayer().sendMessage(GlobalVariables.pluginPrefix + GlobalVariables.joinMessage);
        event.getPlayer().sendTitle("§fWelcome to §bGra§avity", "§fYou are now in §e§nqueue", 10, 80, 10);

        //Send the join message
        Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.LIGHT_PURPLE + event.getPlayer().getName() + ChatColor.YELLOW + " joined the game " + ChatColor.RED + "(" + Bukkit.getOnlinePlayers().size() + "/" + maxPlayers + ")");

        //If the min of players is the one inserted in the config
        if (UsefulMethods.areMinPlayersOnline() && (GameMethods.status == GameMethods.GameStatus.NOTYETSTARTED || GameMethods.status == GameMethods.GameStatus.STARTEDCOUNTDOWN)) {
            GameMethods.startGameCountdown();
            playerObj.setActualMap(0);
            inGamePlayers.put(player, playerObj);
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        //Remove basic "Player joined the game" message
        event.setQuitMessage(null);

        //If there is no one on the Server, Game will be reset
        if (Bukkit.getOnlinePlayers().size() <= 1)
            UsefulMethods.resetGame();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);

        // Check if the player is playing
        if (inGamePlayers.get(event.getEntity().getPlayer()).getStatus() == GameMethods.PlayerStatus.INGAME) {

            // Increment his map fails by 1
            GravityPlayer playerObj = inGamePlayers.get(event.getEntity().getPlayer());
            playerObj.setFailsMap(playerObj.getFailsMap() + 1);
            inGamePlayers.put(event.getEntity().getPlayer(), playerObj);
        }
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (GameMethods.status == GameMethods.GameStatus.STARTED || GameMethods.status == GameMethods.GameStatus.ENDING) {
            if (event.getTo().getBlock().getType() == Material.NETHER_PORTAL) {
                event.setCancelled(true);

                // Set next map and update player time
                GravityPlayer playerObj = inGamePlayers.get(event.getPlayer());
                playerObj.setActualMap(playerObj.getActualMap() + 1);
                playerObj.setGameTime(GameMethods.countdownReverse);
                inGamePlayers.put(event.getPlayer(), playerObj);


                for (Player betweenAllPlayer : getServer().getOnlinePlayers()) {
                    BoardManager.createBoard(betweenAllPlayer);
                }

                // Check if the map of the player is equal to the last map
                if (GameMethods.mapsIndex.get(player.getWorld().getName()).equals(Main.getInstance().config.getInt("maps-per-game") - 1)) {

                    // If everyone has finished
                    if (inGamePlayers.values().stream().noneMatch(p -> p.getStatus().equals(GameMethods.PlayerStatus.INGAME))) {
                        event.getPlayer().sendTitle("§fThanks for playing!", "§7The winner is §e§n" + BoardManager.scorePlayer[0].getName() + "§R§7 with " + inGamePlayers.get(BoardManager.scorePlayer[0]).getGameTime() + "§7s ", 10, 80, 10);
                        UsefulMethods.resetGame();
                    } else if (!(GameMethods.status == GameMethods.GameStatus.ENDING)) {
                        GameMethods.endGame();
                        Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.YELLOW + " finished the game!");
                    }

                    // Teleport player to mainLobby after 0.5 seconds for a better optimization
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            TeleportManager.teleportPlayer(player, TeleportManager.getLobbySpawn());
                        }
                    }.runTaskLater(this, 1L);


                }

                // Verify that the result of the next map is not null
                else if (!(GameMethods.indexMaps.get((GameMethods.mapsIndex.get(player.getWorld().getName()) + 1)).isEmpty())) {

                    // Set player status and update the list
                    playerObj.setStatus(GameMethods.PlayerStatus.FINISHED);
                    inGamePlayers.put(event.getPlayer(), playerObj);

                    new BukkitRunnable() { // Teleport player to nextMap after 0.5 seconds for a better optimization
                        @Override
                        public void run() {
                            TeleportManager.teleportPlayer(player, TeleportManager.getNextSpawnMap(player));
                        }
                    }.runTaskLater(this, 1L);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location placeToTeleport;

        //Check where the player is (lobby or map) and set his respawn-point
        if (player.getWorld().getName().equals(getConfig().getString("lobbyspawn.spawnpoint.world"))) {
            placeToTeleport = TeleportManager.getLobbySpawn();
        }
        else
            placeToTeleport = TeleportManager.getSpawnMap(player);

        event.setRespawnLocation(placeToTeleport);

        // Force teleport after 1 tick for a better optimization
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            TeleportManager.teleportPlayer(player, placeToTeleport);
        }, 1L);
    }

}

