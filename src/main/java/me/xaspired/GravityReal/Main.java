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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;


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
        File configTemplateFile = new File(getDataFolder(), "config_template.yml");
        if (!configTemplateFile.exists())
            saveResource("config_template.yml", true);


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
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + " by xAspired -  " + "Plugin Enabled Successfully!");

    }

    public static Main getInstance() {
        return mainInstance;
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Gra" + ChatColor.GREEN + "vity" + ChatColor.GRAY + " by xAspired - " + "Plugin Disabled Successfully!");
    }

    Player[] scorePlayer = {null, null, null, null, null};
    int[] scoreIndex = {0, 0, 0, 0, 0};
    public void createBoard(Player player) {

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("GravityScore", "forDummy", ChatColor.translateAlternateColorCodes('&', "&a&lGra&b&lvity"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score score11 = obj.getScore(ChatColor.DARK_GRAY + " ");
        score11.setScore(11);
        Score score10 = obj.getScore(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Your Stats:");
        score10.setScore(10);
        Score score9 = obj.getScore("  " + ChatColor.WHITE + player.getName());
        score9.setScore(9);
        Score score8 = obj.getScore("   ");
        score8.setScore(8);
        Score score7 = obj.getScore("  ");
        score7.setScore(7);
        Score score6 = obj.getScore(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Ranking:");
        score6.setScore(6);

        for (Player playerInFor : getServer().getOnlinePlayers()) {

            // Check if the player pass 1st map(index=0)
            Integer playerMapValue = playerMap.get(playerInFor); // Retrieve the value

            if (playerMapValue == null || playerMapValue < 1) {
                continue; // If null or less than 1, skip this player
            }

            // If the player was in scoreboard, update the index
            boolean flag = false;
            for (int j = 0; j < 5; ++j) {
                if (scorePlayer[j] == playerInFor) {
                    scoreIndex[j] = playerMapValue; // Use the safe value
                    flag = true;
                    break;
                }
            }

            // Otherwise, add place player to the last position if they have a map > last map player
            if (!flag) {
                if (playerMapValue > scoreIndex[4] || scorePlayer[4] == null) {
                    scorePlayer[4] = playerInFor;
                    scoreIndex[4] = playerMapValue;
                }
            }
        }

        for (int i = 0; i < 4; ++i) {
            boolean swapped = false;

            for (int j = 0; j < 4 - i; ++j) {
                if (scorePlayer[j + 1] == null)
                    continue;

                boolean swapNeeded = false;

                // Same index, check time
                if (scoreIndex[j + 1] == scoreIndex[j] && playerTime.get(scorePlayer[j + 1]) < playerTime.get(scorePlayer[j])) {
                    swapNeeded = true;
                }

                else if (scoreIndex[j + 1] > scoreIndex[j] || scorePlayer[j] == null) {
                    swapNeeded = true;
                }

                if (swapNeeded) {
                    // Swap using temp
                    int tempScore = scoreIndex[j];
                    scoreIndex[j] = scoreIndex[j + 1];
                    scoreIndex[j + 1] = tempScore;

                    Player tempPlayer = scorePlayer[j];
                    scorePlayer[j] = scorePlayer[j + 1];
                    scorePlayer[j + 1] = tempPlayer;

                    swapped = true;
                }
            }

            // If no swap has been done, the list is ordered
            if (!swapped)
                break;
        }


        // Set up scoreboard 5 rows
        Score scores[] = new Score[5];
        for (int i = 0; i < 5; ++i) {

            // Check if player is null (not arrived)
            if (scorePlayer[i] == null) {
                scores[i] = obj.getScore(ChatColor.WHITE + String.valueOf(i + 1) + "#  Waiting...");
            }
            else {
                // Check if player has finished
                if (playerMap.get(scorePlayer[i]) == Main.getInstance().config.getInt("maps-per-game")) {
                    int realpos = i+1;
                    if (realpos == 1) {
                        scores[i] = obj.getScore(ChatColor.GOLD + "1#  " + scorePlayer[i].getName() + " " + UsefulMethods.returnTimeFormatted(playerTime.get(scorePlayer[i])));
                    }
                    else if (realpos == 2) {
                        scores[i] = obj.getScore(ChatColor.GRAY + "2#  " + scorePlayer[i].getName() + " " + UsefulMethods.returnTimeFormatted(playerTime.get(scorePlayer[i])));
                    }
                    else if (realpos == 3) {
                        scores[i] = obj.getScore(ChatColor.DARK_RED + "3#  " + scorePlayer[i].getName() + " " + UsefulMethods.returnTimeFormatted(playerTime.get(scorePlayer[i])));
                    }
                    else {
                        scores[i] = obj.getScore(ChatColor.GREEN + String.valueOf(realpos) + "#  " + scorePlayer[i].getName() + " " + ChatColor.GRAY + UsefulMethods.returnTimeFormatted(playerTime.get(scorePlayer[i])));
                    }
                }
                else {
                    scores[i] = obj.getScore(ChatColor.WHITE + String.valueOf(i + 1) + "#  " + scorePlayer[i].getName());
                }
            }

            // Add row to scoreboard
            scores[i].setScore(5 - i);
        }

        player.setScoreboard(board);
    }

    //@TODO: Se il game è ancora in progress non fare entrare il player dentro al server
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int maxPlayers = config.getInt("max-players");

        Player player = event.getPlayer();

        //Remove basic "Player joined the game" message
        event.setJoinMessage(null);


        /* **********************************************
                Teleport Player to 'Lobby Spawn'
         ********************************************** */

        //If the spawnpoint is set
        if (!(Objects.equals(getConfig().get("lobbyspawn.spawnpoint.world"), 0))) {

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
        if (UsefulMethods.areMinPlayersOnline() && (Methods.status == Methods.GameStatus.NOTYETSTARTED || Methods.status == Methods.GameStatus.STARTEDCOUNTDOWN)) {
            Methods.startGameCountdown();
            playerMap.put(event.getPlayer(), 0);
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        //Remove basic "Player joined the game" message
        event.setQuitMessage(null);
        playerMap.put(event.getPlayer(), 0);
        playerTime.put(event.getPlayer(), 0);

        //If there is no one on the Server, Game will stop
        if (Bukkit.getOnlinePlayers().size() <= 1) {
            Methods.status = Methods.GameStatus.NOTYETSTARTED;
            Methods.countdownReverse = 0;
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (Methods.status == Methods.GameStatus.STARTED || Methods.status == Methods.GameStatus.ENDING) {
            if (event.getTo().getBlock().getType() == Material.NETHER_PORTAL) {
                event.setCancelled(true);

                // Check if the player exists in playerMap and increment its value
                Integer currentValue = playerMap.get(event.getPlayer());
                if (currentValue == null) {
                    currentValue = 0; // Set a default value if the player is not in the map
                }
                playerMap.put(event.getPlayer(), currentValue + 1);
                playerTime.put(event.getPlayer(), Methods.countdownReverse);

                for (Player betweenAllPlayer : getServer().getOnlinePlayers()) {
                    createBoard(betweenAllPlayer);
                }

                // Check if the map of the player is equal to the last map
                if (Methods.mapsIndex.get(event.getPlayer().getWorld().getName()).equals(Main.getInstance().config.getInt("maps-per-game") - 1)) {
                    Methods.endGame(event.getPlayer());

                    World Lobby = Bukkit.getServer().getWorld(getConfig().getString("lobbyspawn.spawnpoint.world"));
                    double x = getConfig().getDouble("lobbyspawn.spawnpoint.x");
                    double y = getConfig().getDouble("lobbyspawn.spawnpoint.y");
                    double z = getConfig().getDouble("lobbyspawn.spawnpoint.z");
                    double yaw = getConfig().getDouble("lobbyspawn.spawnpoint.yaw");
                    double pitch = getConfig().getDouble("lobbyspawn.spawnpoint.pitch");
                    UsefulMethods.teleportPlayer(event.getPlayer(), Lobby, x, y, z, (float) yaw, (float) pitch);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            UsefulMethods.teleportPlayer(event.getPlayer(), Lobby, x, y, z, (float) yaw, (float) pitch);
                        }
                    }.runTaskLater(this, 10L);
                }

                // Verify that the result of the next map is not null
                else if (!(Methods.indexMaps.get((Methods.mapsIndex.get(event.getPlayer().getWorld().getName()) + 1)).isEmpty())) {
                    World map = Bukkit.getServer().getWorld(Main.getInstance().getConfig().getString("maps." + Methods.indexMaps.get((Methods.mapsIndex.get(event.getPlayer().getWorld().getName()) + 1)) + ".spawnpoint.world"));
                    double x = Main.getInstance().getConfig().getDouble("maps." + Methods.indexMaps.get((Methods.mapsIndex.get(event.getPlayer().getWorld().getName()) + 1)) + ".spawnpoint.x");
                    double y = Main.getInstance().getConfig().getDouble("maps." + Methods.indexMaps.get((Methods.mapsIndex.get(event.getPlayer().getWorld().getName()) + 1)) + ".spawnpoint.y");
                    double z = Main.getInstance().getConfig().getDouble("maps." + Methods.indexMaps.get((Methods.mapsIndex.get(event.getPlayer().getWorld().getName()) + 1)) + ".spawnpoint.z");
                    double yaw = Main.getInstance().getConfig().getDouble("maps." + Methods.indexMaps.get((Methods.mapsIndex.get(event.getPlayer().getWorld().getName()) + 1)) + ".spawnpoint.yaw");
                    double pitch = Main.getInstance().getConfig().getDouble("maps." + Methods.indexMaps.get((Methods.mapsIndex.get(event.getPlayer().getWorld().getName()) + 1)) + ".spawnpoint.pitch");
                    UsefulMethods.teleportPlayer(event.getPlayer(), map, x, y, z, (float) yaw, (float) pitch);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            UsefulMethods.teleportPlayer(event.getPlayer(), map, x, y, z, (float) yaw, (float) pitch);
                        }
                    }.runTaskLater(this, 10L);
                }
            }
        }
    }


}

