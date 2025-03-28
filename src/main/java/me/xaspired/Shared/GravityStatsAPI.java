package me.xaspired.Shared;

import me.xaspired.GravityReal.Main;
import me.xaspired.GravityReal.Connections.DatabaseConnection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class GravityStatsAPI {

    private static FileConfiguration statsFile;
    private static File statsFileRef;

    private static boolean usingLocalStorage() {
        return !Main.getInstance().getConfig().getString("database.storage", "local").equalsIgnoreCase("mysql");
    }

    private static void setupLocalStorage() {
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

    private static void saveLocalFile() {
        try {
            statsFile.save(statsFileRef);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ========== FAILS ==========
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

    public static void incrementFailsTotal(UUID uuid) {
        setFailsTotal(uuid, getFailsTotal(uuid) + 1);
    }

    // ========== TIME ==========
    public static int getTimePlayed(UUID uuid) {
        if (usingLocalStorage()) {
            setupLocalStorage();
            return statsFile.getInt(uuid.toString() + ".time_played", 0);
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            assert conn != null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT time_played FROM gravity_user_data WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getInt("time_played");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void setTimePlayed(UUID uuid, int time) {
        if (usingLocalStorage()) {
            setupLocalStorage();
            statsFile.set(uuid.toString() + ".time_played", time);
            saveLocalFile();
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            assert conn != null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO gravity_user_data (uuid, time_played) VALUES (?, ?) ON DUPLICATE KEY UPDATE time_played = ?")) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, time);
                ps.setInt(3, time);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void incrementTimePlayed(UUID uuid, int delta) {
        setTimePlayed(uuid, getTimePlayed(uuid) + delta);
    }
}