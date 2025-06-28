package me.xaspired.GravityReal.Objects;

import me.xaspired.GravityReal.GameMethods;
import org.bukkit.entity.Player;

public class GravityPlayer {

    private GameMethods.PlayerStatus status;
    private int actualMap;
    private int gameTime;
    private int failsGame;
    private Player player;

    // Constructor
    public GravityPlayer(GameMethods.PlayerStatus status, int actualMap, int gameTime, int failsGame, Player player) {
        this.status = status;
        this.actualMap = actualMap;
        this.gameTime = gameTime;
        this.failsGame = failsGame;
    }

    public GameMethods.PlayerStatus getStatus() {
        return status;
    }

    public int getActualMap() {
        return actualMap;
    }

    public int getFailsGame() {
        return failsGame;
    }

    public int getGameTime() {
        return gameTime;
    }

    public Player getPlayer() { return player; }

    public void setStatus(GameMethods.PlayerStatus status) {
        this.status = status;
    }

    public void setActualMap(int actualMap) {
        this.actualMap = actualMap;
    }

    public void setFailsGame(int failsGame) {
        this.failsGame = failsGame;
    }

    public void setGameTime(int gameTime) {
        this.gameTime = gameTime;
    }
}
