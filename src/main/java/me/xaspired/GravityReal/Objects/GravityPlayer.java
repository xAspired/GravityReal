package me.xaspired.GravityReal.Objects;

import org.bukkit.entity.Player;

public class GravityPlayer {
    private Player player;
    private String status;
    private String actualMap;
    private double gameTime;
    private int fails;
    private int coins;

    // Costructor
    public GravityPlayer(Player player, String status, String actualMap, double gameTime, int fails, int coins) {
        this.player = player;
        this.status = status;
        this.actualMap = actualMap;
        this.gameTime = gameTime;
        this.fails = fails;
        this.coins = coins;
    }

    // Getter
    public Player getPlayer() {
        return player;
    }

    public String getStatus() {
        return status;
    }

    public String getActualMap() {
        return actualMap;
    }

    public int getFails() {
        return fails;
    }

    public int getCoins() {
        return coins;
    }

    public double getGameTime() {
        return gameTime;
    }

    // Setter
    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setActualMap(String actualMap) {
        this.actualMap = actualMap;
    }

    public void setFails(int fails) {
        this.fails = fails;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void setGameTime(double gameTime) {
        this.gameTime = gameTime;
    }
}

