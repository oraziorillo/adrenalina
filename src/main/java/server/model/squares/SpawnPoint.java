package server.model.squares;

import com.google.gson.annotations.Expose;
import common.dto_model.SquareDTO;
import common.enums.SquareColourEnum;
import common.events.square_events.ItemCollectedEvent;
import common.events.square_events.SquareRefilledEvent;
import server.exceptions.EmptySquareException;
import server.exceptions.NotEnoughAmmoException;
import server.model.AmmoTile;
import server.model.Deck;
import server.model.Pc;
import server.model.WeaponCard;

import static common.Constants.*;

public class SpawnPoint extends Square {

    @Expose private int weaponToGrabIndex;
    @Expose private int weaponToDropIndex;
    private WeaponCard[] weapons;
    private Deck<WeaponCard> weaponsDeck;

    public SpawnPoint(){
        super();
        this.weaponToGrabIndex = -1;
        this.weaponToDropIndex = -1;
    }

    public SpawnPoint(int x, int y, SquareColourEnum colour) {
        super(x, y, colour);
        this.weaponToGrabIndex = -1;
        this.weaponToDropIndex = -1;
    }


    @Override
    public void init(Deck<WeaponCard> weaponsDeck, Deck<AmmoTile> ammoDeck) {
        this.weaponsDeck = weaponsDeck;
        weapons = new WeaponCard[CARDS_ON_SPAWN_POINT];
        for (int i = 0; i < CARDS_ON_SPAWN_POINT; i++)
            weapons[i] = weaponsDeck.draw();
    }


    @Override
    public boolean isEmpty() {
        for (WeaponCard weapon : weapons)
            if (weapon != null)
                return false;
        return true;
    }

    public WeaponCard[] getWeapons() {
        return weapons;
    }


    public int getWeaponToDropIndex() {
        return weaponToDropIndex;
    }


    @Override
    public boolean isSpawnPoint() {
        return true;
    }

    @Override
    public void setWeaponToGrabIndex(int weaponToGrabIndex) {
        if (weaponToGrabIndex >= 0 && weaponToGrabIndex < CARDS_ON_SPAWN_POINT && weapons[weaponToGrabIndex] != null)
            this.weaponToGrabIndex = weaponToGrabIndex;
        else
            throw new NullPointerException("You have to choose a weapon to grab");
    }


    @Override
    public void setWeaponToDropIndex(int weaponToDropIndex) {
        if (weaponToDropIndex >= 0 && weaponToDropIndex < CARDS_ON_SPAWN_POINT)
            this.weaponToDropIndex = weaponToDropIndex;
    }


    @Override
    public void resetWeaponIndexes() {
        this.weaponToDropIndex = -1;
        this.weaponToGrabIndex = -1;
    }


    public WeaponCard weaponAtIndex(int index){
        if (index >= 0 && index <= 2)
            return weapons[index];
        return null;
    }

    /**
     * Adds the pre-selected weapon to the given player, unless
     * 1. this square is empty
     * 2. the pre-selected weapon index is negative
     * 3. the weapon slot on that index is empty
     * 4. the given pc already has the max weapon number and didn't select a weapon to drop
     * @param currPc the pc collecting from this tile
     * @throws EmptySquareException if no weapon is available
     * @throws NotEnoughAmmoException if the player cannot pay the half-recharge cost
     */
    @Override
    public void collect(Pc currPc) throws EmptySquareException, NotEnoughAmmoException {
        if (isEmpty())
            throw new EmptySquareException();
        if (weaponToGrabIndex < 0)
            throw new IllegalStateException("You have to choose a weapon to grab");
        if (weapons[weaponToGrabIndex] == null)
            throw new NullPointerException("No weapon on that slot");
        WeaponCard weaponToGrab = weapons[weaponToGrabIndex];
        if (!currPc.hasEnoughAmmo(weaponToGrab.getAmmo()))
            throw new NotEnoughAmmoException();
        if (weaponToDropIndex > 0) {
            if (!currPc.isFullyArmed())
                throw new IllegalStateException("You can't drop a weapon now");
            else {
                WeaponCard weaponToDrop = currPc.weaponAtIndex(weaponToDropIndex);
                weapons[weaponToGrabIndex] = weaponToDrop;
            }
        } else {
            if (currPc.isFullyArmed())
                throw new IllegalStateException("You have to choose a weapon to drop");
            weapons[weaponToGrabIndex] = null;
        }
        currPc.addWeapon(weaponToGrab, weaponToDropIndex);
        short[] cost = weaponToGrab.getAmmo();
        cost[weaponToGrab.getColour().ordinal()]--;
        currPc.payAmmo(cost);

        //notify item collected
        events.fireEvent(new ItemCollectedEvent(
                currPc.getColour(), modelMapper.map(this, SquareDTO.class), weaponToGrab.getName()));

        resetWeaponIndexes();
        
    }


    public void refill() {

        for (int i = 0; i < CARDS_ON_SPAWN_POINT; i++) {
            if (weapons[i] == null && weaponsDeck.size() > 0) {
                weapons[i] = weaponsDeck.draw();
            }
        }

        //notify square refilled
        events.fireEvent(new SquareRefilledEvent(modelMapper.map(this, SquareDTO.class), true));
    }

}
