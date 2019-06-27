package server.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import common.dto_model.KillShotTrackDTO;
import common.enums.PcColourEnum;
import common.enums.SquareColourEnum;
import common.events.FinalFrenzyEvent;
import common.events.KillShotTrackSetEvent;
import common.events.ModelEventHandler;
import common.events.ModelEventListener;
import org.modelmapper.ModelMapper;
import server.controller.CustomizedModelMapper;
import server.database.DatabaseHandler;
import server.model.actions.Action;
import server.model.deserializers.ActionDeserializer;
import server.model.deserializers.GameBoardDeserializer;
import server.model.squares.Square;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents an ADRENALINE game
 */
public class Game {

    private ModelMapper modelMapper = new CustomizedModelMapper().getModelMapper();

    private ModelEventHandler events = new ModelEventHandler();

    private GameBoard gameBoard;
    private Set<Pc> pcs;
    private Deck<WeaponCard> weaponsDeck;
    private Deck<PowerUpCard> powerUpsDeck;
    private Deck<AmmoTile> ammoDeck;
    private boolean finalFrenzy;


    private Game() {
        this.pcs = new HashSet<>();
        this.weaponsDeck = new Deck<>();
        this.powerUpsDeck = new Deck<>();
        this.ammoDeck = new Deck<>();
    }

    public Game(UUID gameUUID) {
        //todo inizializza da file
    }


    public static Game getGame(UUID gameUUID){
        if (DatabaseHandler.getInstance().isPendantGame(gameUUID))
            return new Game(gameUUID);
        else {
            Game game = new Game();
            game.initWeaponsDeck();
            game.initPowerUpsDeck();
            game.initAmmoDeck();
            return game;
        }
    }


    private void initWeaponsDeck() {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Action.class, new ActionDeserializer());
            Gson customGson = gsonBuilder.excludeFieldsWithoutExposeAnnotation().create();

            Type weaponArrayListType = new TypeToken<ArrayList<WeaponCard>>() {
            }.getType();

            JsonReader reader = new JsonReader(new FileReader("src/main/resources/json/weapons.json"));
            ArrayList<WeaponCard> weapons = customGson.fromJson(reader, weaponArrayListType);

            weapons.forEach(w -> {
                w.init();
                weaponsDeck.add(w);
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void initPowerUpsDeck(){
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Action.class, new ActionDeserializer());
            Gson customGson = gsonBuilder.excludeFieldsWithoutExposeAnnotation().create();

            Type powerUpArrayListType = new TypeToken<ArrayList<PowerUpCard>>() {
            }.getType();

            JsonReader reader = new JsonReader(new FileReader("src/main/resources/json/powerUps.json"));
            ArrayList<PowerUpCard> powerUps = customGson.fromJson(reader, powerUpArrayListType);

            powerUps.forEach(p -> powerUpsDeck.add(p));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void initAmmoDeck(){
        try {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

            Type ammoTileArrayListType = new TypeToken<ArrayList<AmmoTile>>(){}.getType();

            JsonReader reader = new JsonReader(new FileReader("src/main/resources/json/ammoTiles.json"));
            ArrayList<AmmoTile> ammoTiles = gson.fromJson(reader, ammoTileArrayListType);

            ammoTiles.forEach(a -> {
                a.setHasPowerUp();
                ammoDeck.add(a);
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * Loads a map given the index
     * @param numberOfMap the index of the map
     */
    public void initMap(int numberOfMap) {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(GameBoard.class, new GameBoardDeserializer());
            Gson customGson = gsonBuilder.excludeFieldsWithoutExposeAnnotation().create();

            JsonReader reader = new JsonReader(new FileReader("src/main/resources/json/gameBoards.json"));
            JsonArray gameBoards = customGson.fromJson(reader, JsonArray.class);
            gameBoard = customGson.fromJson(gameBoards.get(numberOfMap), GameBoard.class);

            gameBoard.initSquares(weaponsDeck, ammoDeck);
            gameBoard.addModelEventHandler(events);

            //notify map set
            //events.fireEvent(new MapSetEvent(modelMapper.map(gameBoard, GameBoard.class), numberOfMap));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * Inits the KillShotTrack with the given number of skulls
     * @param numberOfSkulls The desired number of skulls
     */
    public void initKillShotTrack(int numberOfSkulls){
        gameBoard.initKillShotTrack(numberOfSkulls);

        //notify kill shot track set
        events.fireEvent(new KillShotTrackSetEvent(modelMapper.map(gameBoard.getKillShotTrack(), KillShotTrackDTO.class)));
    }


    public boolean isFinalFrenzy() {
        return finalFrenzy;
    }


    public void setFinalFrenzy(boolean finalFrenzy) {

        this.finalFrenzy = finalFrenzy;

        //notify listeners
        events.fireEvent(new FinalFrenzyEvent());
    }



    public Square getSquare(int row, int col){
        return gameBoard.getSquare(row, col);
    }


    public Square getSpawnPoint(SquareColourEnum requiredColour) {
        return gameBoard.getSpawnPoint(requiredColour);
    }


    public void setTargetableSquares(Set<Square> targetableSquares, boolean isTargetable){
        if (targetableSquares.isEmpty())
            return;
        targetableSquares.forEach(s -> s.setTargetable(isTargetable));
    }


    public void addPc(Pc pc) {
        pc.addModelEventHandler(events);
        pcs.add(pc);
    }


    PowerUpCard drawPowerUp(){
        return powerUpsDeck.draw();
    }


    public boolean scoreDeath(Pc deadPc, boolean doubleKill) {
        scoringPoints(deadPc, doubleKill);
        boolean turnIntoFinalFrenzy = scoreOnKillShotTrack(deadPc);
        if (!isFinalFrenzy())
            deadPc.increaseNumberOfDeaths();
        return turnIntoFinalFrenzy;
    }


    private void scoringPoints(Pc deadPc, boolean doublekill) {
        PcColourEnum [] deadPcDamageTrack = deadPc.getDamageTrack();
        int [] pcValue = deadPc.getPcBoard().getPcValue();
        int pcValueIndex = 0;
        boolean allPointsAssigned = false;
        int [] numOfDamages = new int [5];
        int max;

        if (!isFinalFrenzy()) {
            pcValueIndex = deadPc.getPcBoard().getNumOfDeaths();

            //assigns the first blood point, only if the board is not flipped
            pcs.stream().filter(pc -> pc.getColour() == deadPcDamageTrack[0]).findFirst().get().increasePoints(1);
        }
        //assigns an extra point, only if the current player gets a doubleKill
        if (doublekill)
            pcs.stream().filter(pc -> pc.getColour() == deadPcDamageTrack[10]).findFirst().get().increasePoints(1);

        //assign points to each Pc who damaged the deadPc
        for (PcColourEnum colour: deadPcDamageTrack) {
            if (colour != null)
                numOfDamages[colour.ordinal()]++;
            else
                break;
        }
        while (!allPointsAssigned) {
            max = 0;
            int maxIndex = 0;
            for (int i = 0; i < 5; i++) {
                if (numOfDamages[i] > max) {
                    max = numOfDamages[i];
                    maxIndex = i;
                }
            }
            if (max != 0) {
                int finalMaxIndex = maxIndex;
                pcs.stream().filter(pc -> pc.getColour().ordinal() == finalMaxIndex).findFirst().get().increasePoints(pcValue[pcValueIndex]);
                if (pcValueIndex != pcValue.length - 1)
                    pcValueIndex++;
                else
                    allPointsAssigned = true;
                numOfDamages[finalMaxIndex] = 0;
            }
            else
                allPointsAssigned = true;
        }
    }

    /**
     * Score the death on The KillShotTrack
     * @param deadPc pc which has received a killshot
     * @return True when the game turns into Final Frenzy mode
     */
    private boolean scoreOnKillShotTrack(Pc deadPc) {
        PcColourEnum [] deadPcDamageTrack = deadPc.getDamageTrack();
        PcColourEnum shooterPcColour = deadPcDamageTrack[10];
        if (gameBoard.killOccurred(shooterPcColour, deadPcDamageTrack[11] != null)) {
            deadPc.flipBoard();
            if (!isFinalFrenzy()) {
                setFinalFrenzy(true);
                return true;
            }
        }
        return false;
    }


    public List<Pc> computeWinner() {
        for (Pc pc: pcs) {
            scoringPoints(pc, false);
        }
        int maxPoints = 0;
        for (Pc pc: pcs) {
            if (pc.getPcBoard().getPoints() > maxPoints)
                maxPoints = pc.getPcBoard().getPoints();
        }
        int finalMaxPoints = maxPoints;
        List<Pc> potentialWinners = pcs.stream().filter(pc -> pc.getPcBoard().getPoints() == finalMaxPoints).collect(Collectors.toList());
        if (potentialWinners.size() != 1){
            HashMap<Pc, Integer> points = new HashMap<>();
            potentialWinners.forEach(pc -> points.put(pc, pointsFromKillShots(pc)));
            int maxPointsOnTrack = 0;
            for (Pc pc: points.keySet()) {
                if (points.get(pc) > maxPointsOnTrack) {
                    maxPointsOnTrack = points.get(pc);
                    potentialWinners = new ArrayList<>();
                    potentialWinners.add(pc);
                } else if (points.get(pc) == maxPointsOnTrack)
                    potentialWinners.add(pc);
            }
        }
        return potentialWinners;
    }


    private Integer pointsFromKillShots(Pc pc){
        return pointsOnKillShotTrack(pc, gameBoard.getKillShotTrack()) + pointsOnKillShotTrack(pc, gameBoard.getFinalFrenzyKillShotTrack());
    }


    private Integer pointsOnKillShotTrack(Pc pc, KillShot[] killShots){
        int points = 0;
        PcColourEnum pcColour = pc.getColour();
        for (KillShot killShot: killShots) {
            if (killShot.getColour() == pcColour){
                points++;
                if(killShot.isOverkilled())
                    points++;
            }
        }
        return points;
    }


    void killOccurred(PcColourEnum killerColour, boolean overkilled) {
        gameBoard.killOccurred(killerColour, overkilled);
    }


    public void addModelEventListener(ModelEventListener listener) {
        events.addModelEventListener(listener);
    }

}





