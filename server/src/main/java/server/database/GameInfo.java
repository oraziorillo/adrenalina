package server.database;

import com.google.gson.annotations.Expose;
import server.model.Game;

import java.util.List;
import java.util.UUID;

public class GameInfo {

    private boolean active;
    @Expose private Game game;
    @Expose private List<UUID> playersTokens;
    @Expose private int currPlayerIndex;
    @Expose private int lastPlayerIndex;

    GameInfo(){}

    GameInfo(List<UUID> tokens){
        this.playersTokens = tokens;
    }

    boolean isActive() {
        return active;
    }

    List<UUID> getPlayersTokens() {
        return playersTokens;
    }

    void gameStarted(){
        this.active = true;
    }

    public void setPlayersTokens(List<UUID> playersTokens) {
        this.playersTokens = playersTokens;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getCurrPlayerIndex() {
        return currPlayerIndex;
    }

    public void setCurrPlayerIndex(int currPlayerIndex) {
        this.currPlayerIndex = currPlayerIndex;
    }

    public int getLastPlayerIndex() {
        return lastPlayerIndex;
    }

    public void setLastPlayerIndex(int lastPlayerIndex) {
        this.lastPlayerIndex = lastPlayerIndex;
    }
}
