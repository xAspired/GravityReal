package me.xaspired.GravityReal.Connections;

import me.xaspired.GravityReal.GlobalVariables;
import me.xaspired.GravityReal.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection createConnection() throws SQLException {
        String dbUrl = Main.getInstance().getConfig().getString("database.credentials.dbUrl");
        int port = Main.getInstance().getConfig().getInt("database.credentials.port");
        String database = Main.getInstance().getConfig().getString("database.credentials.database");
        String url = "jdbc:mysql://" + dbUrl + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false";
        String username = Main.getInstance().getConfig().getString("database.credentials.username");
        String password = Main.getInstance().getConfig().getString("database.credentials.password");

        return DriverManager.getConnection(url, username, password);
    }

    // Give rapid connections
    public static Connection getConnection() {
        try {
            return createConnection();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(GlobalVariables.pluginPrefix + ChatColor.RED + "MySQL Connection error: " + e.getMessage());
            return null;
        }
    }

    // Only for DB Table Creation on Startup
    public static void createTables() {
        try (Connection conn = getConnection()) {
            if (conn == null) return;

            String gravityUserDataTable = """
                CREATE TABLE IF NOT EXISTS gravity_user_data (
                    uuid VARCHAR(36) PRIMARY KEY,
                    coins INT NOT NULL DEFAULT 0,
                    fails_total INT NOT NULL DEFAULT 0,
                    time_played INT NOT NULL DEFAULT 0
                );
                """;

            conn.createStatement().execute(gravityUserDataTable);

            Bukkit.getConsoleSender().sendMessage(GlobalVariables.pluginPrefix + ChatColor.GRAY + "MySQL tables created or verified.");
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(GlobalVariables.pluginPrefix + ChatColor.RED + "Error creating MySQL tables: " + e.getMessage());
        }
    }
}
