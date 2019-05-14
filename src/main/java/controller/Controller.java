package controller;

import controller.states.*;
import exceptions.NotCurrPlayerException;
import model.Game;
import model.Pc;
import model.Square;
import model.WeaponCard;
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
    private static final int ACTIONS_PER_TURN = 2;
    private static final int ACTIONS_PER_FRENZY_TURN_AFTER_FIRST_PLAYER = 1;


    private boolean finalFrenzy = false;
    private boolean firstTurn = false;
    private Game game;
    private ArrayList<PcColourEnum> availablePcColours;
    private int remainingActions;
    private int currPlayerIndex;
    private int lastPlayerIndex; //to set when final frenzy starts
    private State currState;
    private WeaponCard currWeapon;
    private ArrayList<Player> players;
    private ArrayList<Square> squaresToRefill;

    public Controller(ArrayList<Player> players) throws RemoteException {
        super();
        this.game = new Game();
        this.players = new ArrayList<>();
        this.squaresToRefill = new ArrayList<>();
        this.availablePcColours = new ArrayList<>();
        Collections.addAll(availablePcColours, PcColourEnum.values());
        this.players.addAll(players);
        this.currPlayerIndex = 0;
        this.currState = new SetupMapState(this);
    }

    public synchronized boolean isFirstTurn() {
        return firstTurn;
    }

    public synchronized boolean isFinalFrenzy() {
        return finalFrenzy;
    }


    public synchronized Game getGame(){
        return game;
    }

    public synchronized ArrayList<Player> getPlayers() {
        return players;
    }


    public synchronized int getCurrPlayerIndex() {
        return currPlayerIndex;
    }

    public synchronized int getRemainingActions(){
        return remainingActions;
    }

    public synchronized WeaponCard getCurrWeapon(){
        return currWeapon;
    }

    public synchronized void setFirstTurn(boolean booleanValue){
        firstTurn = booleanValue;
    }


    public synchronized void setFinalFrenzy(boolean booleanValue){
        finalFrenzy =  booleanValue;
    }

    public synchronized void setLastPlayerIndex(int index){
        lastPlayerIndex = index;
    }

    public synchronized void setCurrWeapon(WeaponCard weapon){
        this.currWeapon = weapon;
    }

    public synchronized void decreaseRemainingActions(){
        this.remainingActions--;
    }

    public synchronized void resetRemainingActions(){
        if(!isFinalFrenzy() || beforeFirstPlayer(getCurrPlayerIndex()))
            this.remainingActions = ACTIONS_PER_TURN;
        else
            this.remainingActions = ACTIONS_PER_FRENZY_TURN_AFTER_FIRST_PLAYER;
    }

    public synchronized void addSquareToRefill(Square s){
        squaresToRefill.add(s);
    }

    /**
     * checks if the player who is trying to do something is the able to do it in this moment
     * @throws NotCurrPlayerException iff the player who called a method is not able to do it in this moment
     */
    public synchronized void validateCurrPlayer() throws NotCurrPlayerException {
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
        if (n >= FIRST_MAP && n <= LAST_MAP && currState.initializeMap(n))
                currState = currState.nextState();
    }


    @Override
    public synchronized void chooseNumberOfSkulls(int n) {
        if (n >= MIN_KILL_SHOT_TRACK_SIZE && n <= MAX_KILL_SHOT_TRACK_SIZE && (currState.setNumberOfSkulls(n)))
            currState = currState.nextState();
    }


    @Override
    public synchronized void choosePcColour(PcColourEnum colour) {
        if (availablePcColours.contains(colour) && currState.assignPcToPlayer(colour, players.get(currPlayerIndex))) {
                availablePcColours.remove(colour);
                nextTurn();
            }
    }


    @Override
    public synchronized void discardAndSpawn(int n) {
        Pc currPc = players.get(currPlayerIndex).getPc();
        if (n == 0 || n == 1 && currState.spawnPc(currPc, n))
                nextTurn();
    }


    @Override
    public synchronized void runAround() {
        if(currState.runAround())
            currState.setTargetables(players.get(currPlayerIndex).getPc());
    }


    @Override
    public synchronized void grabStuff() {
        if(currState.grabStuff())
            currState.setTargetables(players.get(currPlayerIndex).getPc());
    }


    @Override
    public synchronized void shootPeople() {
        if(currState.shootPeople())
            currState.setTargetables(players.get(currPlayerIndex).getPc());
    }


    @Override
    public synchronized void chooseSquare(int x, int y) {
        if(currState.selectSquare(players.get(currPlayerIndex).getPc(), game.map[x][y]))
            currState = currState.nextState();
    }


    @Override
    public synchronized void grabWeapon(int index){
        if((index >= 0 && index <= 2) && currState.grabWeapon(players.get(currPlayerIndex).getPc(), index))
            currState = currState.nextState();
    }

    @Override
    public synchronized void chooseWeapon(int index){
        if((index >= 0 && index <= 2) && currState.selectWeapon(players.get(currPlayerIndex).getPc(), index))
            currState = currState.nextState();
    }

    @Override
    public synchronized void quit() {

    }

    private synchronized void nextTurn() {          //da rivedere
        if (currPlayerIndex == players.size() - 1){
            currPlayerIndex = 0;
            currState = currState.nextState();
        } else
            currPlayerIndex++;
        if (isFirstTurn()){
            Pc currPc = players.get(currPlayerIndex).getPc();
            currPc.drawPowerUp();
            currPc.drawPowerUp();
        }

    }

    public synchronized boolean beforeFirstPlayer(int playerIndex){
        return playerIndex > lastPlayerIndex;
    }
}
