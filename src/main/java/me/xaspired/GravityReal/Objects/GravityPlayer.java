package me.xaspired.GravityReal.Objects;

import me.xaspired.GravityReal.GameMethods;
import org.bukkit.entity.Player;
public class GravityPlayer {

    private GameMethods.PlayerStatus status;
    private int actualMap;
    private int gameTime;
    private int failsMap;

    // Costructor
    public GravityPlayer(Player player, GameMethods.PlayerStatus status, int actualMap, int gameTime, int failsMap, int failsTotal, int coins) {
        this.status = status;
        this.actualMap = actualMap;
        this.gameTime = gameTime;
        this.failsMap = failsMap;
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

    public int getGameTime() {
        return gameTime;
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

    public void setGameTime(int gameTime) {
        this.gameTime = gameTime;
    }
}
