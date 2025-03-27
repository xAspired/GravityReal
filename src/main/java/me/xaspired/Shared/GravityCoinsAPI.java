package me.xaspired.Shared;

import me.xaspired.GravityReal.Connections.DatabaseConnection;
import me.xaspired.GravityReal.GlobalVariables;
import me.xaspired.GravityReal.Main;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class GravityCoinsAPI {

    private static PlayerPointsAPI playerPointsAPI = null;
    private static boolean usePlayerPointsFromConfig = false;
    private static boolean coinsAvailable = false;

    /* **********************************************
                  Coins Setup Method
    ********************************************** */
    public static void setup() {
        usePlayerPointsFromConfig = Main.getInstance().getConfig().getBoolean("coins.use-playerpoints", true);

        if (usePlayerPointsFromConfig) {
            // Check if PlayerPoints plugin is inside our server
            PlayerPoints plugin = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");

            // If is all good
            if (plugin != null && plugin.isEnabled()) {
                playerPointsAPI = plugin.getAPI();
                coinsAvailable = true;
                Bukkit.getLogger().info(GlobalVariables.pluginPrefix + " Connected with PlayerPoints!");
                return;
            } else {
                Bukkit.getLogger().warning(GlobalVariables.pluginPrefix + " PlayerPoints enabled but not found. Checking fallback on MySQL.");
            }
        }

        // Fallback on MySQL
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                coinsAvailable = true;
                Bukkit.getLogger().info(GlobalVariables.pluginPrefix + " Fallback on MySQL enabled.");
            } else {
                Bukkit.getLogger().severe(GlobalVariables.pluginPrefix + " Coins not available (PlayerPoints not enabled, MySQL not reachable).");
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe(GlobalVariables.pluginPrefix + " Error while using MySQL: " + e.getMessage());
        }
    }

    public static boolean isCoinsAvailable() {
        return coinsAvailable;
    }

    public static boolean usingPlayerPoints() {
        return usePlayerPointsFromConfig && playerPointsAPI != null;
    }

    /* **********************************************
                  Get Coins Method
    ********************************************** */
    public static int getCoins(UUID uuid) {
        if (!coinsAvailable) return 0;

        if (usingPlayerPoints()) {
            return playerPointsAPI.look(uuid);
        } else {
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
    }

    /* **********************************************
                  Set Coins Method
    ********************************************** */
    public static void setCoins(UUID uuid, int coins) {
        if (!coinsAvailable) return;

        if (usingPlayerPoints()) {
            playerPointsAPI.set(uuid, coins);
        } else {
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
    }

    /* **********************************************
                  Add Coins Method
    ********************************************** */
    public static void addCoins(UUID uuid, int amount) {
        if (!coinsAvailable) return;

        if (usingPlayerPoints()) {
            playerPointsAPI.give(uuid, amount);
        } else {
            int current = getCoins(uuid);
            setCoins(uuid, current + amount);
        }
    }

}
