package me.xaspired.Shared;

import me.xaspired.GravityReal.Connections.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class GravityStatsAPI {

    public static int getFailsTotal(UUID uuid) {
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
        try (Connection conn = DatabaseConnection.getConnection()) {
            assert conn != null;
            try (PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO gravity_user_data (uuid, fails_total) VALUES (?, ?) " +
                                 "ON DUPLICATE KEY UPDATE fails_total = ?")) {
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
        int current = getFailsTotal(uuid);
        setFailsTotal(uuid, current + 1);
    }
}
