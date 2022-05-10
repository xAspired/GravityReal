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
import org.bukkit.scoreboard.*;

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
    Player firstOne;


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

    public void createBoard(Player player) {
        boolean isPMEmpty = false;
        //Verify that someone has entered in the first portal
        if (playerMap.isEmpty()) {
            playerMap.put(player, 0); //Setting all maps to 0 to all players
            isPMEmpty = true;
        }


        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("GravityScore", "forDummy", ChatColor.translateAlternateColorCodes('&', "&a&lGra&b&lvity"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score score11 = obj.getScore(ChatColor.DARK_GRAY + " ");
        score11.setScore(11);
        Score score10 = obj.getScore(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Your Stats:" );
        score10.setScore(10);
        Score score9 = obj.getScore("  " + ChatColor.WHITE + player.getName());
        score9.setScore(9);
        Score score8 = obj.getScore("   ");
        score8.setScore(8);
        Score score7 = obj.getScore("  ");
        score7.setScore(7);
        Score score6 = obj.getScore(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Ranking:" );
        score6.setScore(6);

        //Checks if the variable is not inizialized ==> There are no players
        if(!(isPMEmpty)) {
            System.out.println("firstOne " + firstOne);


            //Checks it for each player on the server
            for (Player playerInFor : getServer().getOnlinePlayers()) {
                //If the map of 'playerInFor' is higher than firstOne, than surely 'playerInFor' is now the 'firstOne'
                if (playerMap.get(playerInFor) > playerMap.get(firstOne))
                    firstOne = playerInFor;
            }

            Score score5;
            if (playerMap.get(firstOne) == Main.getInstance().config.getInt("maps-per-game"))
                score5 = obj.getScore(ChatColor.GREEN + "1#  " + firstOne.getName() + " " + ChatColor.GRAY + Methods.returnTimeFormatted(playerTime.get(firstOne)));
            else
                score5 = obj.getScore(ChatColor.WHITE + "1#  " + firstOne.getName());

            score5.setScore(5);
            Score score4 = obj.getScore(ChatColor.WHITE + "2#  Waiting...");
            score4.setScore(4);
            Score score3 = obj.getScore(ChatColor.WHITE + "3#  Waiting...");
            score3.setScore(3);
            Score score2 = obj.getScore(ChatColor.WHITE + "4#  Waiting...");
            score2.setScore(2);
            Score score1 = obj.getScore(ChatColor.WHITE + "5#  Waiting...");
            score1.setScore(1);


        }
        else {
            Score score5 = obj.getScore(ChatColor.WHITE + "1#  Waiting...");
            score5.setScore(5);
            Score score4 = obj.getScore(ChatColor.WHITE + "2#  Waiting...");
            score4.setScore(4);
            Score score3 = obj.getScore(ChatColor.WHITE + "3#  Waiting...");
            score3.setScore(3);
            Score score2 = obj.getScore(ChatColor.WHITE + "4#  Waiting...");
            score2.setScore(2);
            Score score1 = obj.getScore(ChatColor.WHITE + "5#  Waiting...");
            score1.setScore(1);
        }


        player.setScoreboard(board);
    }

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
        if(Bukkit.getOnlinePlayers().size() == minPlayers) {
            Methods.startGame();

            firstOne = (Player) getServer().getOnlinePlayers().toArray()[0]; //Take the first one player, just for filling the variable
        }

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

                //Put time and Map for Player
                playerMap.put(event.getPlayer(), playerMap.get(event.getPlayer()) + 1);
                playerTime.put(event.getPlayer(), Methods.countdownReverse);

                for (Player betweenAllPlayer : getServer().getOnlinePlayers()) {
                    createBoard(betweenAllPlayer);
                }

                //Check if the map of the player is equal to the last map
                //I verify that the index of the given maps (I take the world of event player) is equal to the number written in config minus one
                if (Methods.mapsIndex.get(event.getPlayer().getWorld().getName()).equals(Main.getInstance().config.getInt("maps-per-game") - 1)) {
                    Methods.endGame(event.getPlayer());

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

