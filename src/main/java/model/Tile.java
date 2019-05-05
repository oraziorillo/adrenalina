package model;

import model.Enumerations.CardinalDirectionEnum;
import model.Enumerations.TileColourEnum;
import java.util.HashSet;
import java.util.Optional;

public abstract class Tile {
    private final int x;
    private final int y;
    private final TileColourEnum tileColour;
    private HashSet<Pc> pcs;            //ricordarsi di aggiugnere degli observer che ad ogni spostamento del pc modifichi questo insieme
    private HashSet<Tile> visibles;

    /**
     * Builder for a generic tile
     * @param x first index in map
     * @param y second index in map
     * @param colour the room colour
     */
    public Tile(int x, int y, TileColourEnum colour) {
        this.x = x;
        this.y = y;
        this.tileColour = colour;
        this.pcs = new HashSet<>();
        this.visibles = new HashSet<>();
    }

    /**
     * Getter for x coordinate in the map
     * @return the x coordinate of this tile
     */
    public int getX() {
        return x;
    }

    /**
     * Getter for y coordinate in the map
     * @return the y coordinate of this tile
     */
    public int getY() {
        return y;
    }

    /**
     * @author matteo
     * @implNote usare x e y non sarebbe più pulito?
     * Returns an HashSet containing all the Tiles at a given distance
     * @param dist distance of returned tiles
     * @return HashSet of Tiles at distance dist
     */
    public HashSet<Tile> atDistance(int dist){
        if(dist < 0){
            throw new IllegalArgumentException("Distance has to be positive");
        }
        Optional<Tile> tempTile;
        HashSet<Tile> temp = new HashSet<>();
        if(dist == 0){
            temp.add(this);
        }
        else {
            for(CardinalDirectionEnum direction : CardinalDirectionEnum.values()){
                tempTile = this.onDirection(direction);
                if(tempTile.isPresent()) {
                    temp.addAll(tempTile.get().atDistance(dist - 1));
                }
            }
        }
        return temp;
    }

    /**
     * Getter for room colour
     * @return The colour of this room
     */
    public TileColourEnum getTileColour() {
        return tileColour;
    }

    //La tile alla card. dir. specificata si ottiene più facilmente con semplice algebra sulla mappa

    /**
     * @author matteo
     * @implNote usando gli indici non risulta più pulito?
     * Given a cardinal direction, returns the first tile in that direction if no wall is encountered
     * @param direction the cardinal direction
     * @return The first tile in the given direction if there is no wall between, Optional.empty else
     */
    public Optional<Tile> onDirection(CardinalDirectionEnum direction){
        Optional<Tile> temp = Optional.empty();
        switch(direction) {
            case NORTH:
                temp = visibles.stream().filter(elem -> elem.getY() == this.getY() + 1 && elem.getX() == this.getX()).findFirst();
                break;
            case EAST:
                temp = visibles.stream().filter(elem -> elem.getX() == this.getX() + 1 && elem.getY() == this.getY()).findFirst();
                break;
            case SOUTH:
                temp = visibles.stream().filter(elem -> elem.getY() == this.getY() - 1 && elem.getX() == this.getX()).findFirst();
                break;
            case WEST:
                temp = visibles.stream().filter(elem -> elem.getX() == this.getX() - 1 && elem.getY() == this.getY()).findFirst();
                break;
        }
        return temp;
    }

    /**
     * Returns the Pcs on this tile
     * @return the Pcs on this tile
     */
    public HashSet<Pc> getPcs() {
        return pcs;
    }

    /**
     * returns the tiles that a Pc on this tile could see
     * @return the tile visibles from this
     */
    public HashSet<Tile> getVisibles() {
        return (HashSet<Tile>)visibles.clone();
    }

    /**
     * adds a pc to this tile
     * @param pc the pc to put on this tile
     */
    public void addPc(Pc pc) {
        pcs.add(pc);
    }

    /**
     * removes a pc from this tile
     * @param c the pc to remove
     */
    public void removePc(Pc c) {
        pcs.remove(c);
    }

    /**
     *after this method the given tile will be visible from this tile (and then contained into the getVisibles collection)
     * @param t the tile to make visible
     */
    public void addVisible(Tile t) {
        visibles.add(t);
    }

    public abstract void refill();

    /**
     * @author matteo
     * @apiNote Fare qui il metodo che fa raccogliere cose mi sembra la soluzione più semplice
     * Abstract method. Used for subclasses for giving objects to a player.
     * @param player the player wich is collecting something
     * @param objectIndex the index of the object to collect (weapon). In some subclasses could be irrelevant (one object only)
     */
    public abstract void collect(Pc player, int objectIndex);

    /* davvero utile???? non dovrebbe essere un '=='?? Noi restituiamo sempre i riferimenti ai tile, mai cloni
    MATTEO: in effetti non serve a molto
    public boolean equals(Tile t){
        return ((this.getX() == t.getX()) && (this.getY() == t.getY()));
    }

     */

}


public class SpawnTile extends Tile {

    private WeaponCard[] weapons;
    private Deck<WeaponCard> weaponDeck;

    SpawnTile(int x, int y, TileColourEnum colour, Deck<WeaponCard> deck) {
        super(x, y, colour);
        this.weaponDeck = deck;
        weapons = new WeaponCard[3];
        for (int i = 0; i < 3; i++)
            weapons[i] = weaponDeck.draw();
    }

    public WeaponCard[] getWeapons() {
        return weapons.clone();
    }

    WeaponCard pickWeapon(int index) {
        WeaponCard temp = weapons[index];
        weapons[index] = null;
        return temp;
    }

    public WeaponCard switchWeapon(int index, WeaponCard w) {
        WeaponCard temp = weapons[index];
        weapons[index] = w;
        return temp;
    }

    public void refill(){
        //TODO
    }

    @Override
    public void collect(Pc player, int objectIndex) {
        //TODO
    }
}


public class AmmoTile extends Tile {
    private AmmoCard ammoCard;
    private Deck<AmmoCard> ammoDeck;

    AmmoTile(int x, int y, TileColourEnum colour, Deck<AmmoCard> deck) {
        super(x, y, colour);
        ammoDeck = deck;
        ammoCard = ammoDeck.draw();
    }

    public AmmoCard pickAmmo() {
        AmmoCard oldCard = ammoCard;
        ammoCard = null;
        return oldCard;
    }

    public void refill(){
        if(ammoCard == null) {
            ammoCard = ammoDeck.draw();
        }
    }

    @Override
    public void collect(Pc player, int objectIndex) {
        //TODO
    }
}


