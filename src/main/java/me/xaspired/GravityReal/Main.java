package me.xaspired.GravityReal;

import me.xaspired.GravityReal.Commands.CommandGravity;
import me.xaspired.GravityReal.Commands.PlayerUtilitiesCommand;
import me.xaspired.GravityReal.Connections.DatabaseConnection;
import me.xaspired.GravityReal.Managers.BoardManager;
import me.xaspired.GravityReal.Managers.MessagesManager;
import me.xaspired.GravityReal.Managers.TeleportManager;
import me.xaspired.GravityReal.Objects.GravityPlayer;
import me.xaspired.Shared.GravityCoinsAPI;
import me.xaspired.Shared.GravityStatsAPI;
import net.md_5.bungee.api.ChatMessageType;
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
import java.util.Map;


@SuppressWarnings("ConstantConditions")
public class Main extends JavaPlugin implements Listener {
    FileConfiguration config = getConfig();
    public static Main mainInstance;

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

        // Enable the API Coins Setup
        me.xaspired.Shared.GravityCoinsAPI.setup();

        // Database Setup
        DatabaseConnection.createTables();

        // Initialize messages checker
        MessagesManager.init();

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
            event.getPlayer().kickPlayer(MessagesManager.kickReason1);
            return;
        }

        int maxPlayers = config.getInt("max-players");

        Player player = event.getPlayer();
        GravityPlayer playerObj = new GravityPlayer(GameMethods.PlayerStatus.NONE, 0, 0, 0);
        inGamePlayers.put(player, playerObj);

        //Remove basic "Player joined the game" message
        event.setJoinMessage(null);

        //Teleport player to Lobby Spawn
        TeleportManager.teleportPlayer(player, TeleportManager.getLobbySpawn());

        //Send the custom message written in config under "message-join"
        event.getPlayer().sendMessage(MessagesManager.pluginPrefix + MessagesManager.joinMessage);
        event.getPlayer().sendTitle(MessagesManager.welcomeTitle, MessagesManager.queueTitle, 10, 80, 10);

        //Send the join message
        Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.LIGHT_PURPLE + event.getPlayer().getName() + ChatColor.YELLOW + " joined the game " + ChatColor.RED + "(" + Bukkit.getOnlinePlayers().size() + "/" + maxPlayers + ")");

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
            playerObj.setFailsGame(playerObj.getFailsGame() + 1);
            GravityStatsAPI.incrementFailsTotal(event.getEntity().getPlayer().getUniqueId());
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

                // Create board for all players inside the server
                for (Player betweenAllPlayer : getServer().getOnlinePlayers()) {
                    BoardManager.createBoard(betweenAllPlayer);
                }

                // Check if the map of the player is equal to the last map
                if (GameMethods.mapsIndex.get(player.getWorld().getName()).equals(Main.getInstance().config.getInt("maps-per-game") - 1)) {

                    // Set player status and update the list
                    playerObj.setStatus(GameMethods.PlayerStatus.FINISHED);
                    inGamePlayers.put(event.getPlayer(), playerObj);

                    // If everyone has finished
                    if (inGamePlayers.values().stream().noneMatch(p -> p.getStatus().equals(GameMethods.PlayerStatus.INGAME))) {

                        // Custom title message including placeholder variables set in the config
                        String gameWinnerMessage = MessagesManager.getFormatted("messages.game-winner", Map.of(
                                "PLAYER", BoardManager.scorePlayer[0].getName(),
                                "TIME", UsefulMethods.returnTimeFormatted(inGamePlayers.get(BoardManager.scorePlayer[0]).getGameTime())
                        ));
                        event.getPlayer().sendTitle(MessagesManager.greetingsPlaying, gameWinnerMessage, 10, 80, 10);

                        UsefulMethods.resetGame();
                    }
                    // Otherwise if the first player has finished
                    else if (!(GameMethods.status == GameMethods.GameStatus.ENDING)) {
                        GameMethods.endGame();

                        // Winner message set in the config
                        String winnerMessage = MessagesManager.getFormatted("messages.winner-message", Map.of(
                                "PLAYER", player.getName()
                        ));
                        Bukkit.broadcastMessage(MessagesManager.pluginPrefix + winnerMessage);

                        // If coins methods are not available, the plugin will skip it and doesn't assign anything
                        if (!GravityCoinsAPI.isCoinsAvailable()) {
                            Bukkit.getLogger().warning(ChatColor.RED + "Coins not available at the moment. Check carefully plugin settings.");
                            return;
                        }

                        // Add coins to Player and send him a message (check coins config)
                        GravityCoinsAPI.addCoins(player.getUniqueId(), getConfig().getInt("coins-per-win"));

                        // Coins Message
                        String coinsMessage = MessagesManager.getFormatted("messages.winner-message", Map.of(
                                "COINS", String.valueOf(GravityCoinsAPI.getCoins(player.getUniqueId()))
                        ));
                        player.sendMessage(coinsMessage);

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

                    Location placeToTeleport = TeleportManager.getNextSpawnMap(player);

                    // If something went wrong for some reason
                    if (placeToTeleport == null) {
                        player.spigot().sendMessage(ChatMessageType.valueOf(MessagesManager.pluginPrefix + MessagesManager.teleportingError));
                        return;
                    }

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

        // If something went wrong for some reason
        if (placeToTeleport == null) {
            player.spigot().sendMessage(ChatMessageType.valueOf(MessagesManager.pluginPrefix + MessagesManager.teleportingError));
            return;
        }

        event.setRespawnLocation(placeToTeleport);

        // Force teleport after 1 tick for a better optimization
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> TeleportManager.teleportPlayer(player, placeToTeleport), 1L);
    }

}

