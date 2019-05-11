package controller;

import exceptions.HoleInMapException;
import exceptions.NotCurrPlayerException;
import model.Game;
import model.Pc;
import model.Tile;
import model.enumerations.PcColourEnum;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;

public class Controller extends UnicastRemoteObject implements RemoteController {

    static final int FIRST_MAP = 1;
    static final int LAST_MAP = 4;
    static final int MIN_KILL_SHOT_TRACK_SIZE = 5;
    static final int MAX_KILL_SHOT_TRACK_SIZE = 8;

    static final int RUN = 1;
    static final int GRAB = 2;
    static final int SHOOT = 3;

    private boolean finalFrenzy = false;
    private boolean firstTurn = false;
    private Game game;
    private ArrayList<PcColourEnum> availablePcColours;
    private int currPlayerIndex;
    private int lastPlayerIndex; //to set when final frenzy starts
    private State currState;
    private int requestedAction;
    ArrayList<Player> players;
    State setupMapState, setupKillShotTrackState, pcChoiceState, firstTurnState, startTurnState, runState, grabState, shootState;

    public Controller(ArrayList<Player> players) throws RemoteException {
        super();
        this.game = new Game();
        this.players = new ArrayList<>();
        this.availablePcColours = new ArrayList<>();
        Collections.addAll(availablePcColours, PcColourEnum.values());
        State.setController(this);
        this.players.addAll(players);
        this.currPlayerIndex = 0;
        this.setupMapState = new SetupMapState();
        this.setupKillShotTrackState = new SetupKillShotTrackState();
        this.pcChoiceState = new PcChoiceState();
        this.firstTurnState = new FirstTurnState();
        this.startTurnState = new StartTurnState();
    }

    public boolean isFirstTurn() {
        return firstTurn;
    }

    public boolean isFinalFrenzy() {
        return finalFrenzy;
    }


    public Game getGame(){
        return game;
    }


    public ArrayList<Player> getPlayers() {
        return players;
    }


    public int getCurrPlayerIndex() {
        return currPlayerIndex;
    }


    public int getRequestedAction(){
        return requestedAction;
    }


    public void setFirstTurn(boolean booleanValue){
        firstTurn = booleanValue;
    }


    public void setFinalFrenzy(boolean booleanValue){
        finalFrenzy =  booleanValue;
    }


    public void setCurrState(State nextState){
        currState = nextState;
    }


    public void setRequestedAction(int action){
        requestedAction = action;
    }


    public void setLastPlayerIndex(int index){
        lastPlayerIndex = index;
    }


    /**
     * checks if the player who is trying to do something is the able to do it in this moment
     * @throws NotCurrPlayerException iff the player who called a method is not able to do it in this moment
     */
    public void validateCurrPlayer() throws NotCurrPlayerException {
        //TODO: deve prendere come arg un token
        //      deve sollevare un eccezione NotCurrPlayerException
        //      aggiungere try catch a tutti i metodi
    }

    @Override
    public synchronized void showComment(String comment) {
        game.setMessage(comment);
    }


    @Override
    public synchronized void chooseMap(int n) {
        if (n >= FIRST_MAP && n <= LAST_MAP) {
            if (currState.initializeMap(n))
                currState.nextState();
        }
    }


    @Override
    public synchronized void chooseNumberOfSkulls(int n) {
        if (n >= MIN_KILL_SHOT_TRACK_SIZE && n <= MAX_KILL_SHOT_TRACK_SIZE) {
            if (currState.setNumberOfSkulls(n))
                currState.nextState();
        }
    }


    @Override
    public synchronized void choosePcColour(PcColourEnum colour) {
        if (availablePcColours.contains(colour))
            if (currState.assignPcToPlayer(colour, players.get(currPlayerIndex))) {
                availablePcColours.remove(colour);
                nextTurn();
            }
    }


    @Override
    public synchronized void discardAndSpawn(int n) {
        Pc currPc = players.get(currPlayerIndex).getPc();
        if (n == 0 || n == 1)
            if (currState.spawnPc(currPc, n))
                nextTurn();
    }


    @Override
    public synchronized void runAround() {
        setRequestedAction(RUN);
        currState.changeState();
    }


    @Override
    public synchronized void grabStuff() {
        setRequestedAction(GRAB);
        currState.changeState();
    }


    @Override
    public synchronized void shootPeople() {
        setRequestedAction(SHOOT);
        currState.changeState();
    }


    @Override
    public synchronized void move(int x, int y) {
        int maxDistance = 1;

        switch (requestedAction){
            case RUN:
                maxDistance = isFinalFrenzy() ? 4 : 3;
                break;
            case GRAB:
                maxDistance = (isFinalFrenzy() || (players.get(currPlayerIndex).getPc().getAdrenaline() >= 1)) ? 2 : 1;
                break;
            default:
                break;
        }

        try {
            Tile destination = game.getTile(x, y);
            Tile currSquare = players.get(currPlayerIndex).getPc().getCurrTile();
            //if destination is contained by the collection of squares at distance <= maxDistance move the Pc and go to the next state
            if (currSquare.atDistance(maxDistance).contains(destination))
                if(currState.move(players.get(currPlayerIndex), destination))
                    currState.nextState();
        } catch (IndexOutOfBoundsException | HoleInMapException e) {
            //TODO mandare messaggio all'utente
        }
    }


    @Override
    public synchronized void quit() {

    }

    @Override
    public synchronized void selectSquare(int x, int y) {

    }

    private synchronized void nextTurn() {
        if (currPlayerIndex == players.size() - 1){
            currPlayerIndex = 0;
            currState.nextState();
        } else
            currPlayerIndex++;
        if (isFirstTurn()){
            Pc currPc = players.get(currPlayerIndex).getPc();
            currPc.drawPowerUp();
            currPc.drawPowerUp();
        }

    }

    private synchronized boolean beforeFirstPlayer(int playerIndex){
        return playerIndex > lastPlayerIndex;
    }


}
