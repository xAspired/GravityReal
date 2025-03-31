package me.xaspired.Shared;

import me.xaspired.GravityReal.Connections.DatabaseConnection;
import me.xaspired.GravityReal.Managers.MessagesManager;
import me.xaspired.GravityReal.Main;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/* **********************************************
                  PRIORITIES
          Coins: PlayerPoints - MySQL - Local
          Stats: MySQL - Local
********************************************** */

public class GravityCoinsAPI {

    private static PlayerPointsAPI playerPointsAPI = null;
    private static boolean usingPlayerPoints = false;

    static boolean usingMySQL() {
        return !Main.getInstance().getConfig().getString("database.storage", "mysql").equalsIgnoreCase("local");
    }

    public static boolean usingPlayerPoints() {
        return usingPlayerPoints && playerPointsAPI != null;
    }

    /* **********************************************
                  Coins Setup Method
    ********************************************** */
    public static void setup() {
        usingPlayerPoints = Main.getInstance().getConfig().getBoolean("coins.use-playerpoints", true);

        // Priority 1: PlayerPoints
        if (usingPlayerPoints) {
            // Check if PlayerPoints plugin is inside our server
            PlayerPoints plugin = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");

            // If is all good
            if (plugin != null && plugin.isEnabled()) {
                playerPointsAPI = plugin.getAPI();
                Bukkit.getLogger().info(MessagesManager.pluginPrefix + " Connected with PlayerPoints!");
                return;
            } else {
                Bukkit.getLogger().warning(MessagesManager.pluginPrefix + " PlayerPoints enabled but not found. Checking fallback on MySQL, otherwise it will use local storage.");
            }
        }

        // Priority 2: MySQL
        if (usingMySQL()) {

            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn != null)
                    Bukkit.getLogger().info(MessagesManager.pluginPrefix + " Fallback on MySQL enabled.");
            } catch (Exception e) {
                Bukkit.getLogger().severe(MessagesManager.pluginPrefix + " Error while using MySQL: " + e.getMessage());
            }

        }

        // Priority 3: Local
        else {
            GravityStatsAPI.setupLocalStorage();
        }
    }

    /* **********************************************
                  Get Coins Method
    ********************************************** */
    public static int getCoins(UUID uuid) {

        // Priority 1: PlayerPoints
        if (usingPlayerPoints()) {
            return playerPointsAPI.look(uuid);
        }

        // Priority 2: MySQL
        else if (usingMySQL()) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                assert conn != null;
                try (PreparedStatement ps = conn.prepareStatement("SELECT coins FROM gravity_user_data WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) return rs.getInt("coins");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }

        // Priority 3: Local
        else {
            GravityStatsAPI.setupLocalStorage();
            return GravityStatsAPI.statsFile.getInt(uuid.toString() + ".coins", 0);
        }
    }

    /* **********************************************
                  Set Coins Method
    ********************************************** */
    public static void setCoins(UUID uuid, int coins) {

        // Priority 1: PlayerPoints
        if (usingPlayerPoints()) {
            playerPointsAPI.set(uuid, coins);
        }

        // Priority 2: MySQL
        else if (usingMySQL()) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                assert conn != null;
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO gravity_user_data (uuid, coins) VALUES (?, ?) ON DUPLICATE KEY UPDATE coins = ?")) {
                    ps.setString(1, uuid.toString());
                    ps.setInt(2, coins);
                    ps.setInt(3, coins);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Priority 3: Local
        else {
            GravityStatsAPI.setupLocalStorage();
            GravityStatsAPI.statsFile.set(uuid.toString() + ".coins", coins);
            GravityStatsAPI.saveLocalFile();
        }
    }

    /* **********************************************
                  Add Coins Method
    ********************************************** */
    public static void addCoins(UUID uuid, int amount) {

        // Priority 1: PlayerPoints
        if (usingPlayerPoints()) {
            playerPointsAPI.give(uuid, amount);
        }

        // Priority 2-3: MySQL - Local
        else {
            int current = getCoins(uuid);
            setCoins(uuid, current + amount);
        }
    }

}
