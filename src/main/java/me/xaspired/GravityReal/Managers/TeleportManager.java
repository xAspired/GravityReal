package me.xaspired.GravityReal.Managers;

import me.xaspired.GravityReal.GameMethods;
import me.xaspired.GravityReal.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class TeleportManager {
    //@TODO Add Check if Every Locations Exist
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
            //Create a new virtual object named world, that names is the same as the one in the config
            World Lobby = Bukkit.getServer().getWorld(Main.getInstance().getConfig().getString("lobbyspawn.spawnpoint.world"));
            Lobby.setPVP(false);

            //Coords taken from the conf.yml file
            double x = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.x");
            double y = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.y");
            double z = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.z");
            double yaw = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.yaw");
            double pitch = Main.getInstance().getConfig().getDouble("lobbyspawn.spawnpoint.pitch");

            return new Location(Lobby, x, y, z, (float) yaw, (float) pitch);
    }

    /* **********************************************
         Spawn -current- Map Teleport (on Death)
    ********************************************** */
    public static Location getSpawnMap(Player player) {
        World map = Bukkit.getServer().getWorld(Main.getInstance().getConfig().getString("maps." + GameMethods.indexMaps.get(GameMethods.mapsIndex.get(player.getWorld().getName())) + ".spawnpoint.world"));
        double x = Main.getInstance().getConfig().getDouble("maps." + GameMethods.indexMaps.get(GameMethods.mapsIndex.get(player.getWorld().getName())) + ".spawnpoint.x");
        double y = Main.getInstance().getConfig().getDouble("maps." + GameMethods.indexMaps.get(GameMethods.mapsIndex.get(player.getWorld().getName())) + ".spawnpoint.y");
        double z = Main.getInstance().getConfig().getDouble("maps." + GameMethods.indexMaps.get(GameMethods.mapsIndex.get(player.getWorld().getName())) + ".spawnpoint.z");
        double yaw = Main.getInstance().getConfig().getDouble("maps." + GameMethods.indexMaps.get(GameMethods.mapsIndex.get(player.getWorld().getName())) + ".spawnpoint.yaw");
        double pitch = Main.getInstance().getConfig().getDouble("maps." + GameMethods.indexMaps.get(GameMethods.mapsIndex.get(player.getWorld().getName())) + ".spawnpoint.pitch");
        map.setPVP(false);

        return new Location(map, x, y, z, (float) yaw, (float) pitch);
    }

    /* **********************************************
                Get Next Spawn Map
    ********************************************** */

    public static Location getNextSpawnMap(Player player) {
        World map = Bukkit.getServer().getWorld(Main.getInstance().getConfig().getString("maps." + GameMethods.indexMaps.get((GameMethods.mapsIndex.get(player.getWorld().getName()) + 1)) + ".spawnpoint.world"));
        double x = Main.getInstance().getConfig().getDouble("maps." + GameMethods.indexMaps.get((GameMethods.mapsIndex.get(player.getWorld().getName()) + 1)) + ".spawnpoint.x");
        double y = Main.getInstance().getConfig().getDouble("maps." + GameMethods.indexMaps.get((GameMethods.mapsIndex.get(player.getWorld().getName()) + 1)) + ".spawnpoint.y");
        double z = Main.getInstance().getConfig().getDouble("maps." + GameMethods.indexMaps.get((GameMethods.mapsIndex.get(player.getWorld().getName()) + 1)) + ".spawnpoint.z");
        double yaw = Main.getInstance().getConfig().getDouble("maps." + GameMethods.indexMaps.get((GameMethods.mapsIndex.get(player.getWorld().getName()) + 1)) + ".spawnpoint.yaw");
        double pitch = Main.getInstance().getConfig().getDouble("maps." + GameMethods.indexMaps.get((GameMethods.mapsIndex.get(player.getWorld().getName()) + 1)) + ".spawnpoint.pitch");
        map.setPVP(false);

        return new Location(map, x, y, z, (float) yaw, (float) pitch);
    }
}
