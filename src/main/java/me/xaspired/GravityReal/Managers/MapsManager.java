package me.xaspired.GravityReal.Managers;

import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MapsManager {
    // Custom Variable -> Meaning a map is not yet saved because of spawnpoints
    private static final Map<String, JSONObject> pendingMapUpdates = new HashMap<>();

    /* **********************************************
            Initialize Map Json
    ********************************************** */
    public static JSONObject initializeMap(String nameMap, int configMaxPlayers) {
        JSONObject mapData = new JSONObject();
        mapData.put("name", nameMap);
        mapData.put("difficulty", "default");
        mapData.put("playersNumber", configMaxPlayers);

        // Initialize Spawnpoints to 0 for each one according to the config maxPlayers' number
        JSONArray spawnpoints = new JSONArray();
        for (int i = 0; i < configMaxPlayers; i++) {
            JSONObject spawnpoint = new JSONObject(); // Equals for something inside { }
            spawnpoint.put("world", "default");
            spawnpoint.put("x", 0.0);
            spawnpoint.put("y", 0.0);
            spawnpoint.put("z", 0.0);
            spawnpoint.put("pitch", 0.0);
            spawnpoint.put("yaw", 0.0);
            spawnpoints.put(spawnpoint);
        }
        mapData.put("spawnpoints", spawnpoints);
        return mapData;
    }

    /* **********************************************
            Set Spawn for Each Given Index
    ********************************************** */
    public static boolean setMapSpawn(String nameMap, int spawnIndex, Player player) {
        String fileName = "plugins/GravityReal/maps/" + nameMap + ".json";
        File fileMap = new File(fileName);

        if (!fileMap.exists()) {
            player.sendMessage(MessagesManager.pluginPrefix + ChatColor.RED + "This map doesn't exist! Create it with /gravity createmap <mapname>");
            return false;
        }

        try {
            JSONObject mapData;
            if (pendingMapUpdates.containsKey(nameMap)) {
                mapData = pendingMapUpdates.get(nameMap);
            } else {
                String content = new String(Files.readAllBytes(Paths.get(fileName)));
                mapData = new JSONObject(content);
                pendingMapUpdates.put(nameMap, mapData);
            }

            JSONArray spawnpoints = mapData.optJSONArray("spawnpoints");
            int configMaxPlayers = mapData.getInt("playersNumber");

            if (spawnpoints == null || spawnIndex > configMaxPlayers) {
                player.sendMessage(ChatColor.RED + "Invalid spawn index!");
                return false;
            }

            JSONObject spawnpoint = spawnpoints.getJSONObject(spawnIndex - 1);
            spawnpoint.put("world", Objects.requireNonNull(player.getLocation().getWorld()).getName());
            spawnpoint.put("x", player.getLocation().getX());
            spawnpoint.put("y", player.getLocation().getY());
            spawnpoint.put("z", player.getLocation().getZ());
            spawnpoint.put("pitch", player.getLocation().getPitch());
            spawnpoint.put("yaw", player.getLocation().getYaw());

            player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "Spawn point " + spawnIndex + " for map " + ChatColor.AQUA + nameMap + ChatColor.GRAY + " set! " + spawnIndex + "/" + configMaxPlayers);

            // Check if every spawnpoint are set
            if (allSpawnsSet(spawnpoints, configMaxPlayers)) {
                saveMapToFile(nameMap);
                player.sendMessage(MessagesManager.pluginPrefix + ChatColor.GRAY + "All spawn points set for map " + ChatColor.AQUA + nameMap + ChatColor.GRAY + "! Map saved.");
            }
            return true;

        } catch (IOException e) {
            player.sendMessage(MessagesManager.pluginPrefix + ChatColor.RED + "Error updating the map file! Try again or ask dev.");
            e.printStackTrace();
            return false;
        }
    }

    private static boolean allSpawnsSet(JSONArray spawnpoints, int configMaxPlayers) {
        for (int i = 0; i < configMaxPlayers; i++) {
            JSONObject spawnpoint = spawnpoints.getJSONObject(i);

            // Verify if some spawnpoints is equal to default, meaning there is something not yet set
            if (spawnpoint.getString("world").equals("default")) {
                return false;
            }
        }
        return true;
    }

    /* **********************************************
            Save All Map's Spawnpoints to File
    ********************************************** */
    private static void saveMapToFile(String nameMap) {
        String fileName = "plugins/GravityReal/maps/" + nameMap + ".json";

        // If the map is still in queue for being finished
        if (pendingMapUpdates.containsKey(nameMap)) {
            try (FileWriter fileWriter = new FileWriter(fileName)) {
                fileWriter.write(pendingMapUpdates.get(nameMap).toString(4)); // Pretty print JSON

                // Remove map from the global pendingMap variable
                pendingMapUpdates.remove(nameMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
