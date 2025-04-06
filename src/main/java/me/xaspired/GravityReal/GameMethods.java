package me.xaspired.GravityReal;

import me.xaspired.GravityReal.Managers.BoardManager;
import me.xaspired.GravityReal.Managers.MessagesManager;
import me.xaspired.GravityReal.Managers.TeleportManager;
import me.xaspired.GravityReal.Objects.GravityPlayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        NONE,
        INGAME,
        FINISHED
    }

    //Timer
    public static boolean isTimerStarted = false;
    public static int countdownReverse = 0;

    //Maps and In-Game-Players Variables
    public static HashMap<String, Integer> mapsIndex = new HashMap<>();
    public static HashMap<Integer, String> indexMaps = new HashMap<>();

    static GameStatus status = GameStatus.NOTYETSTARTED;


    /* **********************************************
         Random Map Generation & Config Check
     ********************************************** */
    public static StringBuilder initializeGameAndMaps() {
        int numberMaps;
        File mapsFolder = new File("plugins/GravityReal/maps/");
        File[] mapsFiles = mapsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        StringBuilder nameMapsConcatenated = new StringBuilder();

        // Check if the value of numberMaps is null (there aren't maps set in the config)
        try {
            // Number of Maps set
            numberMaps = mapsFiles.length;
        } catch (Exception e) {
            numberMaps = 0;
        }

        if (mapsFiles == null || mapsFiles.length == 0) {
            Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "There are no available maps. Please create new maps.");
            status = GameStatus.NOTYETSTARTED;
            return null;
        }

        int mapsPerGame = Main.getInstance().config.getInt("maps-per-game");

        // Check if set maps are enough to let game starts
        if (numberMaps >= mapsPerGame) {
            // Creation of index array for the generation of the random maps
            ArrayList<Integer> tempNumberList = new ArrayList<>();
            for (int i = 0; i < numberMaps; i++) {
                tempNumberList.add(i);
            }

            for (int count = 0; count < mapsPerGame && !tempNumberList.isEmpty(); count++) {
                // Choose random index and remove it from the list
                int randomIndex = tempNumberList.remove((int) (Math.random() * tempNumberList.size()));

                // Verify the index is valid
                if (randomIndex < 0 || randomIndex >= mapsFiles.length) {
                    continue; // Salta se l'indice non è valido
                }

                // Get map's name without .json extension
                String nameMapFor = mapsFiles[randomIndex].getName().replace(".json", "");

                mapsIndex.put(nameMapFor, count);
                indexMaps.put(count, nameMapFor);

                // Choose color according the difficulty written on file
                File mapFile = new File("plugins/GravityReal/maps/" + nameMapFor + ".json");
                ChatColor mapColor = ChatColor.WHITE; // Default

                try {
                    String content = new String(Files.readAllBytes(Paths.get(mapFile.getPath())));
                    JSONObject mapData = new JSONObject(content);

                    String difficulty = mapData.optString("difficulty", "unknown").toLowerCase();
                    mapColor = switch (difficulty) {
                        case "easy" -> ChatColor.GREEN;
                        case "medium" -> ChatColor.YELLOW;
                        case "hard" -> ChatColor.RED;
                        default -> mapColor;
                    };

                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error reading map difficulty for " + nameMapFor);
                }

                // Add map with its color
                nameMapsConcatenated.append(mapColor).append(nameMapFor);

                // Add "-" only if the map it's the last one of the list
                if (count < mapsPerGame - 1) {
                    nameMapsConcatenated.append(ChatColor.WHITE).append(" - ");
                }
            }

        } else {
            Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "There are too few maps to let the game start. Check your 'maps-per-game' setting or create new maps.");
            status = GameStatus.NOTYETSTARTED;
            return null;
        }

        return nameMapsConcatenated;
    }

    /* **********************************************
           Create the Object for the First Map
     ********************************************** */
    public static Object[] firstMapSetup() {
        if (indexMaps.isEmpty() || indexMaps.get(0) == null) {
            Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.RED + "No maps selected. The game cannot start.");
            status = GameStatus.NOTYETSTARTED;
            return null;
        }

        String firstMapName = indexMaps.get(0);
        File mapFile = new File("plugins/GravityReal/maps/" + firstMapName + ".json");

        if (!mapFile.exists()) {
            Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.RED + "The game couldn't start because the map file for " + firstMapName + " is missing.");
            status = GameStatus.NOTYETSTARTED;
            return null;
        }

        try {
            // Get the JSON about the map
            String content = new String(Files.readAllBytes(Paths.get(mapFile.getPath())));
            JSONObject mapData = new JSONObject(content);

            // Get world first map
            World firstMap = Bukkit.getServer().getWorld(mapData.getJSONArray("spawnpoints").getJSONObject(0).getString("world"));
            if (firstMap == null) {
                Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.RED + "World " + mapData.getJSONArray("spawnpoints").getJSONObject(0).getString("world") + " is not loaded.");
                status = GameStatus.NOTYETSTARTED;
                return null;
            }

            // Get coords of the first map - @TODO: Credo sia sbagliato
            JSONObject spawnpoint = mapData.getJSONArray("spawnpoints").getJSONObject(0);
            double x = spawnpoint.getDouble("x");
            double y = spawnpoint.getDouble("y");
            double z = spawnpoint.getDouble("z");
            float yaw = (float) spawnpoint.getDouble("yaw");
            float pitch = (float) spawnpoint.getDouble("pitch");

            return new Object[]{firstMap, x, y, z, yaw, pitch};

        } catch (Exception e) {
            Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.RED + "Error loading the first map's spawn point. Check the map file.");
            e.printStackTrace();
            status = GameStatus.NOTYETSTARTED;
            return null;
        }
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
        Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "Minimum number of players reached!");
        Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "Starting " + ChatColor.RED + "countdown" + ChatColor.DARK_GRAY + "...");
        new BukkitRunnable() {
            int countdownStarter = 10;

            public void run() {

                Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + countdownStarter);

                // Countdown stopped if no minimum player online is more satisfied
                if (!UsefulMethods.areMinPlayersOnline()) {
                    Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "Minimum number of players no more satisfied. Countdown " + ChatColor.RED + "stopped" + ChatColor.GRAY + "!");
                    cancel();
                }
                if (--countdownStarter < 0) {
                    status = GameStatus.STARTED;

                    /* **********************************************
                            Teleport All Players to First Map
                    ********************************************** */
                    for (Player player : getServer().getOnlinePlayers()) {
                        // Take the return of the method about the generation of the 1st Map
                        Object[] firstMapObj = firstMapSetup();

                        // Check if the return method about the 1st Map gave an exception
                        if (firstMapObj == null) {
                            cancel();
                            return;
                        }

                        Location firstMap = new Location((World) firstMapObj[0], (Double) firstMapObj[1], (Double) firstMapObj[2], (Double) firstMapObj[3], (Float) firstMapObj[4], (Float) firstMapObj[5]);

                        TeleportManager.teleportPlayer(player, firstMap); //Teleport All

                        // Player Setup
                        player.setMaxHealth(6);
                        player.setHealthScale(6);
                        player.setGameMode(GameMode.ADVENTURE);

                        // Update Player status
                        GravityPlayer playerObj = Main.getInstance().inGamePlayers.get(player);
                        playerObj.setStatus(PlayerStatus.INGAME);
                        Main.getInstance().inGamePlayers.put(player, playerObj);

                        BoardManager.scorePlayer[0] = null;
                        BoardManager.scorePlayer[1] = null;
                        BoardManager.scorePlayer[2] = null;
                        BoardManager.scorePlayer[3] = null;
                        BoardManager.scorePlayer[4] = null;
                        BoardManager.createBoard(player); // Creation of the Board
                        timerPlayers(); // Start board timer

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

    public static void endGame() {
        status = GameStatus.ENDING;

        new BukkitRunnable() {
            int countdownStarter = 240;

            public void run() {
                if (!(status == GameStatus.ENDING))
                    cancel();

                if (countdownStarter == 240)
                    Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "The game will stop in 240 seconds ");
                else if (countdownStarter == 180)
                    Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "The game will stop in 180 seconds ");
                else if (countdownStarter == 120)
                    Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "The game will stop in 120 seconds ");
                else if (countdownStarter == 60)
                    Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "The game will stop in 60 seconds ");
                else if (countdownStarter == 3)
                    Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "The game will stop in 3 seconds ");
                else if (countdownStarter == 2)
                    Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "The game will stop in 2 seconds ");
                else if (countdownStarter == 1)
                    Bukkit.broadcastMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "The game will stop in 1 seconds ");

                if (--countdownStarter < 0) {
                    for (Player player : getServer().getOnlinePlayers()) {
                        TeleportManager.teleportPlayer(player, TeleportManager.getLobbySpawn());
                    }
                    UsefulMethods.resetGame();
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 20, 20);
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
