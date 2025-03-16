package me.xaspired.GravityReal.Objects;

import me.xaspired.GravityReal.GameMethods;
import org.bukkit.entity.Player;

public class GravityPlayer {
    private Player player;
    private GameMethods.PlayerStatus status;
    private int actualMap;
    private int gameTime;
    private int failsMap;
    private int failsTotal;
    private int coins;

    // Costructor
    public GravityPlayer(Player player, GameMethods.PlayerStatus status, int actualMap, int gameTime, int failsMap, int failsTotal, int coins) {
        this.player = player;
        this.status = status;
        this.actualMap = actualMap;
        this.gameTime = gameTime;
        this.failsMap = failsMap;
        this.failsTotal = failsTotal;
        this.coins = coins;
    }

    // Getter
    public Player getPlayer() {
        return player;
    }

    public GameMethods.PlayerStatus getStatus() {
        return status;
    }

    public int getActualMap() {
        return actualMap;
    }

    public int getFailsMap() {
        return failsMap;
    }

    public int getFailsTotal() {
        return failsTotal;
    }

    public int getCoins() {
        return coins;
    }

    public int getGameTime() {
        return gameTime;
    }

    // Setter
    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setStatus(GameMethods.PlayerStatus status) {
        this.status = status;
    }

    public void setActualMap(int actualMap) {
        this.actualMap = actualMap;
    }

    public void setFailsMap(int failsMap) {
        this.failsMap = failsMap;
    }

    public void setFailsTotal(int failsTotal) {
        this.failsTotal = failsTotal;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void setGameTime(int gameTime) {
        this.gameTime = gameTime;
    }
}

