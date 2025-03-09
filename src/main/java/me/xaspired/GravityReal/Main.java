package me.xaspired.GravityReal;

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
    public static HashMap<Player, Integer> playerMap = new HashMap<>();
    public static HashMap<Player, Integer> playerTime = new HashMap<>();

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
            playerMap.put(event.getPlayer(), 0);
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        //Remove basic "Player joined the game" message
        event.setQuitMessage(null);

        // @TODO invece di impostarli a 0, rimuoverli direttamente per liberare anche memoria
        playerMap.put(event.getPlayer(), 0);
        playerTime.put(event.getPlayer(), 0);

        //If there is no one on the Server, Game will be reset
        if (Bukkit.getOnlinePlayers().size() <= 1)
            UsefulMethods.resetGame();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (GameMethods.status == GameMethods.GameStatus.STARTED || GameMethods.status == GameMethods.GameStatus.ENDING) {
            if (event.getTo().getBlock().getType() == Material.NETHER_PORTAL) {
                event.setCancelled(true);

                // Check if the player exists in playerMap and increment its value
                Integer currentValue = playerMap.get(player);
                if (currentValue == null) {
                    currentValue = 0; // Set a default value if the player is not in the map
                }
                playerMap.put(player, currentValue + 1);
                playerTime.put(player, GameMethods.countdownReverse);

                for (Player betweenAllPlayer : getServer().getOnlinePlayers()) {
                    BoardManager.createBoard(betweenAllPlayer);
                }

                // Check if the map of the player is equal to the last map
                if (GameMethods.mapsIndex.get(player.getWorld().getName()).equals(Main.getInstance().config.getInt("maps-per-game") - 1)) {
                    GameMethods.endGame(player);

                    //Everyone has finished
                    if (!GameMethods.playerStatus.containsValue(GameMethods.PlayerStatus.INGAME)) {
                        event.getPlayer().sendTitle("§fThanks for playing!", "§7The winner is §e§n" + BoardManager.scorePlayer[0] + " §7with " + playerTime.get(BoardManager.scorePlayer[0]), 10, 80, 10);
                        UsefulMethods.resetGame();
                    }
                    else {
                        // Teleport player to mainLobby after 0.5 seconds for a better optimization
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                TeleportManager.teleportPlayer(player, TeleportManager.getLobbySpawn());
                            }
                        }.runTaskLater(this, 1L);
                    }

                }

                // Verify that the result of the next map is not null
                else if (!(GameMethods.indexMaps.get((GameMethods.mapsIndex.get(player.getWorld().getName()) + 1)).isEmpty())) {
                    GameMethods.playerStatus.put(player, GameMethods.PlayerStatus.FINISHED);

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

