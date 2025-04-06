package me.xaspired.Shared;

import me.xaspired.GravityReal.Main;
import me.xaspired.GravityReal.Connections.DatabaseConnection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

/* **********************************************
                  PRIORITIES
          Coins: PlayerPoints - MySQL - Local
          Stats: MySQL - Local
********************************************** */

public class GravityStatsAPI {

    static FileConfiguration statsFile;
    private static File statsFileRef;

    static boolean usingLocalStorage() {
        return !Main.getInstance().getConfig().getString("database.storage", "local").equalsIgnoreCase("mysql");
    }

    /* **********************************************
                  Setup Local Storage
    ********************************************** */
    public static void setupLocalStorage() {
        if (statsFileRef == null) {
            statsFileRef = new File(Main.getInstance().getDataFolder(), "local_stats.yml");
            if (!statsFileRef.exists()) {
                try {
                    statsFileRef.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        statsFile = YamlConfiguration.loadConfiguration(statsFileRef);
    }

    /* **********************************************
                  Save File
    ********************************************** */
    public static void saveLocalFile() {
        try {
            statsFile.save(statsFileRef);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* **********************************************
                  Get Fails
    ********************************************** */
    public static int getFailsTotal(UUID uuid) {
        if (usingLocalStorage()) {
            setupLocalStorage();
            return statsFile.getInt(uuid.toString() + ".fails_total", 0);
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            assert conn != null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT fails_total FROM gravity_user_data WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getInt("fails_total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /* **********************************************
                  Get Wins
    ********************************************** */
    public static int getWins(UUID uuid) {
        if (usingLocalStorage()) {
            setupLocalStorage();
            return statsFile.getInt(uuid.toString() + ".wins", 0);
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            assert conn != null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT wins FROM gravity_user_data WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getInt("wins");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /* **********************************************
                  Set Fails
    ********************************************** */
    public static void setFailsTotal(UUID uuid, int totalFails) {
        if (usingLocalStorage()) {
            setupLocalStorage();
            statsFile.set(uuid.toString() + ".fails_total", totalFails);
            saveLocalFile();
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            assert conn != null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO gravity_user_data (uuid, fails_total) VALUES (?, ?) ON DUPLICATE KEY UPDATE fails_total = ?")) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, totalFails);
                ps.setInt(3, totalFails);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* **********************************************
                  Set Wins
    ********************************************** */
    public static void setWins(UUID uuid, int wins) {
        if (usingLocalStorage()) {
            setupLocalStorage();
            statsFile.set(uuid.toString() + ".wins", wins);
            saveLocalFile();
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            assert conn != null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO gravity_user_data (uuid, wins) VALUES (?, ?) ON DUPLICATE KEY UPDATE wins = ?")) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, wins);
                ps.setInt(3, wins);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* **********************************************
                  Increment Fails
    ********************************************** */
    public static void incrementFailsTotal(UUID uuid) {
        setFailsTotal(uuid, getFailsTotal(uuid) + 1);
    }

    /* **********************************************
                  Increment Wins
    ********************************************** */
    public static void incrementWins(UUID uuid) {
        setWins(uuid, getWins(uuid) + 1);
    }

}