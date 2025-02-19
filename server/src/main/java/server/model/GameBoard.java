package server.model;

import com.google.gson.annotations.Expose;
import common.dto_model.GameBoardDTO;
import common.enums.PcColourEnum;
import common.enums.SquareColourEnum;
import server.model.squares.Square;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Support class for game that handles the map and the kill shot track
 */
public class GameBoard {
    @Expose private int numberOfMap;
    @Expose private int rows;
    @Expose private int columns;
    @Expose private List<Square> squares;
    @Expose private int[] doors;
    @Expose private KillShotTrack killShotTrack;
    private List<Square> spawnPoints;


    public GameBoard(){
        this.spawnPoints = new ArrayList<>();
    }


    void init(Deck<WeaponCard> weaponsDeck, Deck<AmmoTile> ammoDeck) {
        for (Square s : squares) {

            s.init(weaponsDeck, ammoDeck);

            if (s.isSpawnPoint())
                spawnPoints.add(s);

            //initialize an ArrayList of visible colours
            HashSet<Integer> visibleColours = new HashSet<>();
            visibleColours.add(s.getColour().ordinal());

            int sId = s.getRow() * columns + s.getCol();

            for (int j = 0; j < doors.length; j = j + 2)
                if (doors[j] == sId)
                    visibleColours.add(getSquare(
                            doors[j + 1] / columns,
                            doors[j + 1] % columns
                    ).getColour().ordinal());

            //then add all squares whose colour is contained in visibleColours to the list of i's visible squares
            squares.stream()
                    .filter(x -> visibleColours.contains(x.getColour().ordinal()))
                    .forEach(s::addVisible);
        }
    }


    public void setNumberOfMap(int numberOfMap) {
        this.numberOfMap = numberOfMap;
    }

    public int getNumberOfMap() {
        return numberOfMap;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public List<Square> getSquares() {
        return squares;
    }

    public List<Square> getSpawnPoints() {
        return spawnPoints;
    }

    
    /**
     * Inits the kill shot track with the given number of skulls
     * @param numberOfSkulls the desired number of skulls
     */
    void initKillShotTrack(int numberOfSkulls){
        killShotTrack = new KillShotTrack(numberOfSkulls);
    }

    public KillShotTrack getKillShotTrack() {
        return killShotTrack;
    }

    public KillShot[] getFinalFrenzyKillShotTrackArray() {
        return killShotTrack.getFinalFrenzyKillShotTrack();
    }


    public KillShot[] getKillShotTrackArray() {
        return killShotTrack.getKillShotTrack();
    }

    Square getSquare(int row, int col){
        if (row >= rows || row < 0 || col >= columns || col < 0)
            return null;
        Optional<Square> currSquare = squares.stream()
                .filter(s -> (col == s.getCol() && row == s.getRow()))
                .findFirst();
        return currSquare.orElse(null);
    }
    

    Square getSpawnPoint(SquareColourEnum requiredColour){
        for (Square s : spawnPoints) {
            if (s.getColour().equals(requiredColour))
                return s;
        }
        return null;
    }


    /**
     * Updates the KillShotTrack with the occurred kill
     * @param killerColour the colour of the killer
     * @param overkilled true if the player was overkilled, see the manual
     * @return True if the game turns into or already is in Final Frenzy mode
     */
    boolean killOccurred(PcColourEnum killerColour, Boolean overkilled){
        return killShotTrack.killOccured(killerColour,overkilled);
    }


    void addModelEventHandler(ModelEventHandler events) {
        squares.forEach(s -> s.addModelEventHandler(events));
    }


    String simplifiedToString(){
        StringBuilder b = new StringBuilder();
        for (Square s : squares) {
            b.append(System.lineSeparator()).append(s.toString()).append("\t\t").append(s.getColour()).append((s.isSpawnPoint() ? " Spawn point" : " Ammo square"));
        }
        return b.toString();
    }


    public GameBoardDTO convertoTo(){
        GameBoardDTO gameBoardDTO = new GameBoardDTO();
        gameBoardDTO.setNumberOfMap(numberOfMap);
        gameBoardDTO.setRows(rows);
        gameBoardDTO.setColumns(columns);
        gameBoardDTO.setSquares(squares.stream().map(Square::convertToDTO).collect(Collectors.toList()));
        return  gameBoardDTO;
    }
}
