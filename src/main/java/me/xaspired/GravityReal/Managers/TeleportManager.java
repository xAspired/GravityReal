package me.xaspired.GravityReal.Managers;

import me.xaspired.GravityReal.GameMethods;
import me.xaspired.GravityReal.Main;
import me.xaspired.GravityReal.UsefulMethods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class TeleportManager {
    /* **********************************************
             Teleport Player
    ********************************************** */
    public static void teleportPlayer(Player player, Location map) {
        player.teleport(map);
    }

    /* **********************************************
             Lobby Teleport
    ********************************************** */
    public static Location getLobbySpawn() {
        try {
            //Create a new virtual object named world, that names is the same as the one in the config
            World Lobby = Bukkit.getServer().getWorld(Objects.requireNonNull(Main.getInstance().getConfig().getString("lobbyspawn.spawnpoint.world")));
            assert Lobby != null;
            Lobby.setPVP(false);

            //Coords taken from the conf.yml file
            double x = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.x");
            double y = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.y");
            double z = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.z");
            double yaw = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.yaw");
            double pitch = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.pitch");

            return new Location(Lobby, x, y, z, (float) yaw, (float) pitch);

        } catch (Exception e) {
            Bukkit.getLogger().warning(MessagesManager.pluginPrefix + "Error loading spawn lobby : " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    /* **********************************************
         Spawn -current- Map Teleport (on Death)
    ********************************************** */
    public static Location getSpawnMap(Player player) {
        return getMapSpawn(player, Main.getInstance().inGamePlayers.get(player).getActualMap());
    }

    /* **********************************************
                Get Next Spawn Map
    ********************************************** */
    public static Location getNextSpawnMap(Player player) {
        return getMapSpawn(player, Main.getInstance().inGamePlayers.get(player).getActualMap() + 1);
    }

    /* **********************************************
                Common Spawn Method
    ********************************************** */
    private static Location getMapSpawn(Player player, int mapIndex) {
        String mapName = GameMethods.indexMaps.get(mapIndex);
        String fileName = "plugins/GravityReal/maps/" + mapName + ".json";
        File fileMap = new File(fileName);

        // If the file doesn't exist
        if (!fileMap.exists()) {
            Bukkit.getLogger().warning(MessagesManager.pluginPrefix + "There was a problem finding " + mapName + " file.");
            return null;
        }

        try {
            // Read JSON file
            String content = new String(Files.readAllBytes(Paths.get(fileMap.getPath())));
            JSONObject mapData = new JSONObject(content);

            // If "INGAME" players are > of map spawnpoints (playersNumber)
            if (mapData.getInt("playersNumber") < Main.getInstance().inGamePlayers.values().stream().filter(p -> p.getStatus().equals(GameMethods.PlayerStatus.INGAME)).count()) {
                Bukkit.getLogger().warning(MessagesManager.pluginPrefix + "Can't properly check spawnpoints because "
                        + mapName + "'s player number is lower than players who are actually playing.");
                return null;
            }

            // Get spawnpoints
            JSONArray spawnPoints = mapData.optJSONArray("spawnpoints");
            if (spawnPoints == null || spawnPoints.isEmpty()) {
                Bukkit.getLogger().warning(MessagesManager.pluginPrefix + "No spawnpoints found in " + mapName);
                return null;
            }

            // Get player spawn index
            int playerIndex = UsefulMethods.getInGamePlayerIndex(player);
            if (playerIndex >= spawnPoints.length()) {
                Bukkit.getLogger().warning(MessagesManager.pluginPrefix + "Player index out of range in " + mapName + " spawnpoints.");
                return null;
            }

            JSONObject spawnData = spawnPoints.getJSONObject(playerIndex);
            World world = Bukkit.getServer().getWorld(spawnData.getString("world"));
            double x = spawnData.getDouble("x");
            double y = spawnData.getDouble("y");
            double z = spawnData.getDouble("z");
            float yaw = (float) spawnData.getDouble("yaw");
            float pitch = (float) spawnData.getDouble("pitch");

            return new Location(world, x, y, z, yaw, pitch);

        } catch (Exception e) {
            Bukkit.getLogger().warning(MessagesManager.pluginPrefix + "Error loading map " + mapName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
