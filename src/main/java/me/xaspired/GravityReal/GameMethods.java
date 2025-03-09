package me.xaspired.GravityReal;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

import static org.bukkit.Bukkit.getServer;

@SuppressWarnings({"deprecation", "ConstantConditions"})
public class GameMethods {

    /* **********************************************
            Game Variables Declaration
     ********************************************** */
    public enum GameStatus {
        NOTYETSTARTED,
        STARTEDCOUNTDOWN,
        STARTED,
        ENDING
    }

    public enum PlayerStatus {
        INGAME,
        FINISHED
    }

    //Timer
    public static boolean isTimerStarted = false;
    public static int countdownReverse = 0;

    //Maps and In-Game-Players Variables
    public static HashMap<String, Integer> mapsIndex = new HashMap<>();
    public static HashMap<Integer, String> indexMaps = new HashMap<>();
    public static HashMap<Player, PlayerStatus> playerStatus = new HashMap<>();

    static GameStatus status = GameStatus.NOTYETSTARTED;


    /* **********************************************
         Random Map Generation & Config Check
     ********************************************** */
    public static StringBuilder initializeGameAndMaps() {
        int numberMaps;

        //Number of maps players will play in a Game (can be set in the config)
        StringBuilder nameMapsConcatenated = new StringBuilder();

        //Check if the value of numberMaps is null (there aren't maps set in the config)
        try {
            //Number of Maps in the config
            numberMaps = Main.getInstance().config.getConfigurationSection("maps").getKeys(false).size();
        } catch (Exception e) {
            numberMaps = 0;
        }

        //Check if someone inserted a wrong value to 'maps-per-game' in config
        //The code can't run if 'maps-per-game' are set to 5 but there are 3 maps set
        if (numberMaps >= Main.getInstance().config.getInt("maps-per-game")) {
            //Array based on the number of maps
            ArrayList<Integer> tempNumberList = new ArrayList<>(numberMaps);
            for (int i = 0; i < numberMaps; i++) {
                tempNumberList.add(i);
            }
            for (int count = 0; count < Main.getInstance().config.getInt("maps-per-game"); count++)
            {

                //Randomized map
                String nameMapFor = (String) Main.getInstance().config.getConfigurationSection("maps").getKeys(false).toArray()[tempNumberList.remove((int) (Math.random() * tempNumberList.size()))];
                mapsIndex.put(nameMapFor, count);
                indexMaps.put(count, nameMapFor);

                //Different Color for Different Difficulty
                //Try and catch for those who wrongly remove manually the "difficulty" string from config file
                try {
                    if (Main.getInstance().getConfig().getString("maps." + indexMaps.get(count) + ".difficulty").equalsIgnoreCase("easy"))
                        nameMapsConcatenated.append(ChatColor.GREEN).append(indexMaps.get(count));
                    else if (Main.getInstance().getConfig().getString("maps." + indexMaps.get(count) + ".difficulty").equalsIgnoreCase("medium"))
                        nameMapsConcatenated.append(ChatColor.YELLOW).append(indexMaps.get(count));
                    else if (Main.getInstance().getConfig().getString("maps." + indexMaps.get(count) + ".difficulty").equalsIgnoreCase("hard"))
                        nameMapsConcatenated.append(ChatColor.RED).append(indexMaps.get(count));
                } catch (Exception e) {
                    nameMapsConcatenated.append(ChatColor.WHITE).append(indexMaps.get(count));
                }

                //Settings to don't add the minus at the end of the StringBuilder
                if (count < Main.getInstance().config.getInt("maps-per-game") - 1)
                    nameMapsConcatenated.append(ChatColor.WHITE).append(" - ");
            }
        }
        else {
            Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "There are too few maps to let the game starts. Check your 'maps-per-game' in config, or create new maps.");
            status = GameStatus.NOTYETSTARTED;
            return null;
        }

        if (status == GameStatus.NOTYETSTARTED)
            startGameCountdown();

        return nameMapsConcatenated;
    }

    /* **********************************************
           Create the Object for the First Map
     ********************************************** */
    public static Object[] firstMapSetup() {
        World firstMap;

        //If some spanwpoints are not set:
        try {
            //Create a new virtual world
            firstMap = Bukkit.getServer().getWorld(Main.getInstance().getConfig().getString("maps." + indexMaps.get(0) + ".spawnpoint.world"));
        } catch (Exception e) {
            Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "The game couldn't start because there are some maps without spawnpoint. Check in the config and set it with /gravity setmapspawn <map>");
            status = GameStatus.NOTYETSTARTED;
            return null;
        }

        //Coords taken from the conf.yml file
        double x = Main.getInstance().getConfig().getDouble("maps." + indexMaps.get(0) + ".spawnpoint.x");
        double y = Main.getInstance().getConfig().getDouble("maps." + indexMaps.get(0) + ".spawnpoint.y");
        double z = Main.getInstance().getConfig().getDouble("maps." + indexMaps.get(0) + ".spawnpoint.z");
        float yaw = (float) Main.getInstance().getConfig().getDouble("maps." + indexMaps.get(0) + ".spawnpoint.yaw");
        float pitch = (float) Main.getInstance().getConfig().getDouble("maps." + indexMaps.get(0) + ".spawnpoint.pitch");

        return new Object[] {firstMap, x, y, z, yaw, pitch};
    }


    /* **********************************************
                10 Seconds' Countdown
     ********************************************** */
    public static void startGameCountdown() {
        status = GameStatus.STARTEDCOUNTDOWN;

        //Nested methods -> Call an actionbar about the initialized random maps
        actionbarMaps(initializeGameAndMaps());

        //If for some reason Maps above are not correctly initialized and then also actionbar
        //status will become NOTYETSTARTED again, and then we should go outside this method
        if (status == GameStatus.NOTYETSTARTED)
            return;

        //Broadcasting that the minPlayers is satisfied
        Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "Minimum number of players reached!");
        Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "Starting " + ChatColor.RED + "countdown" + ChatColor.DARK_GRAY + "...");
        new BukkitRunnable() {
            int countdownStarter = 10;

            public void run() {

                Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + countdownStarter);

                //Countdown stopped if no minimum player online is more satisfied
                if (!UsefulMethods.areMinPlayersOnline()) {
                    Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "Minimum number of players no more satisfied. Countdown " + ChatColor.RED + "stopped" + ChatColor.GRAY + "!");
                    cancel();
                }
                if (--countdownStarter < 0) {
                    status = GameStatus.STARTED;

                    /* **********************************************
                            Teleport All Players to First Map
                    ********************************************** */
                    for (Player player : getServer().getOnlinePlayers()) {
                        //Take the return of the method about the generation of the 1st Map
                        Object[] firstMapObj = firstMapSetup();

                        //Check if the return method about the 1st Map gave an exception
                        if (firstMapObj == null) {
                            cancel();
                            return;
                        }

                        Location firstMap = new Location((World) firstMapObj[0], (Double) firstMapObj[1], (Double) firstMapObj[2], (Double) firstMapObj[3], (Float) firstMapObj[4], (Float) firstMapObj[5]);

                        TeleportManager.teleportPlayer(player, firstMap); //Teleport All

                        //Player Setup
                        player.setMaxHealth(6);
                        player.setHealthScale(6);
                        player.setGameMode(GameMode.ADVENTURE);
                        playerStatus.put(player, PlayerStatus.INGAME);

                        BoardManager.scorePlayer[0] = null;
                        BoardManager.scorePlayer[1] = null;
                        BoardManager.scorePlayer[2] = null;
                        BoardManager.scorePlayer[3] = null;
                        BoardManager.scorePlayer[4] = null;
                        BoardManager.createBoard(player); //Creation of the Board
                        timerPlayers(); //Start board timer

                    }
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 20, 20);

    }

    /* **********************************************
                     Action Bar Maps
     ********************************************** */
    public static void actionbarMaps(StringBuilder nameMapsConcatenated) {

        if (nameMapsConcatenated == null) {
            status = GameStatus.NOTYETSTARTED;
            return;
        }

        //Sending to all players an actionbar that says which maps will be played
        for (Player player : getServer().getOnlinePlayers()) {
            new BukkitRunnable() {
                int countdownStarter = 2;

                public void run() {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(String.valueOf(nameMapsConcatenated)));

                    if (--countdownStarter < 0) {
                        cancel();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 20, 20);
        }
    }

    public static void endGame(Player playerWin) {
        if (!(status == GameStatus.ENDING)) {
            status = GameStatus.ENDING;
            Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.LIGHT_PURPLE + playerWin.getName() + ChatColor.YELLOW + " finished the game!");

            new BukkitRunnable() {
                int countdownStarter = 240;

                public void run() {
                    if (!(status == GameStatus.ENDING))
                        cancel();

                    // @TODO testare lo switch
                    switch (countdownStarter) {
                        case 240:
                            Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "The game will stop in 240 seconds ");
                        case 180:
                            Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "The game will stop in 180 seconds ");
                        case 120:
                            Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "The game will stop in 120 seconds ");
                        case 60:
                            Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "The game will stop in 60 seconds ");
                        case 3:
                            Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "The game will stop in 3 seconds ");
                        case 2:
                            Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "The game will stop in 2 seconds ");
                        case 1:
                            Bukkit.broadcastMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "The game will stop in 1 seconds ");
                    }

                    if (--countdownStarter < 0) {
                        for (Player player : getServer().getOnlinePlayers()) {
                            player.performCommand("spawn");
                        }
                        UsefulMethods.resetGame();
                        cancel();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 20, 20);
        }
    }

    public static void timerPlayers() {

        if (!(isTimerStarted)) {
            isTimerStarted = true;
        /* **********************************************
                       Scoreboard Timer
        ********************************************** */
            new BukkitRunnable() {
                public void run() {

                    if (++countdownReverse > Main.getInstance().getConfig().getInt("duration-time") || Bukkit.getOnlinePlayers().isEmpty()) {
                        cancel();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 20, 20);

        }

    }

}
