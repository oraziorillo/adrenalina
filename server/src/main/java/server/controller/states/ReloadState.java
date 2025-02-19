package server.controller.states;

import server.controller.Controller;
import server.controller.Player;
import server.database.DatabaseHandler;
import server.model.PowerUpCard;
import server.model.WeaponCard;
import server.model.squares.Square;

/**
 * Reloads the weapons
 */
public class ReloadState extends State {


    private WeaponCard weaponToReload;


    ReloadState(Controller controller) {
        super(controller);
        //cli.controller.startTimer();
    }


    /**
     * Selects a powerup to use as an ammo
     *
     * @param p
     * @param index the powerup card index
     * @see PowerUpCard
     */
    @Override
    public void selectPowerUp(Player p, int index) {
        PowerUpCard powerUp = controller.getCurrPc().getPowerUpCard(index);
        if (powerUp != null) {
            powerUp.setSelectedAsAmmo(!powerUp.isSelectedAsAmmo());
            controller.ackCurrent(System.lineSeparator() + "You'll lose a " + powerUp.toString() + " instead of paying one " + powerUp.getColour() + " ammo");
        }
    }


    /**
     * selects a weapon to reload
     * @param index the WeaponCard index
     */
    @Override
    public void selectWeaponOfMine(Player p, int index) {
        WeaponCard currWeapon = controller.getCurrPc().weaponAtIndex(index);
        if (currWeapon != null && !currWeapon.isLoaded()) {
            this.weaponToReload = currWeapon;
            controller.ackCurrent("Humankind cannot gain anything without first giving something in return." +
                    System.lineSeparator() + "To obtain, something of equal value must be lost." +
                    System.lineSeparator() + "To reload a " + currWeapon.toString() + " you have to pay:" + currWeapon.ammoToString());
        }
    }


    /**
     * Reloads the pre-selected weapon using methods from Pc and WeaponCard
     * @see server.model.Pc
     * @see WeaponCard
     * @return true iif a weapon was pre-selected
     */
    @Override
    public boolean ok(Player p) {
        if (weaponToReload != null){
            short [] weaponCost = weaponToReload.getAmmo();
            if (controller.getCurrPc().hasEnoughAmmo(weaponCost)) {
                controller.getCurrPc().payAmmo(weaponCost);
                weaponToReload.setLoaded(true);
                weaponToReload = null;
            } else {
                controller.ackCurrent("Not enough ammo to reload that weapon. You should have collected some before!");
            }
        }
        return false;
    }


    /**
     * Ends the player turn
     * @return true
     */
    @Override
    public boolean pass(Player p) {
        if (!controller.isLocked()) {
            controller.getSquaresToRefill().forEach(Square::refill);
            controller.resetSquaresToRefill();
            controller.ackCurrent(System.lineSeparator() + "Be a good boy/girl until your next turn" + System.lineSeparator());
            return true;
        }
        controller.ackCurrent(System.lineSeparator() + "Be patient! A player is choosing whether to use or not a Tagback Grenade");
        return false;
    }


    @Override
    public State forcePass(Player p) {
        controller.getSquaresToRefill().forEach(Square::refill);
        controller.resetSquaresToRefill();
        controller.ackCurrent(System.lineSeparator() + "Be a good boy/girl until your next turn" + System.lineSeparator());
        controller.nextTurn();
        return new InactiveState(controller, InactiveState.FIRST_TURN_STATE);
    }


    /**
     * Transition
     * @return ShootPeopleState if final frenzy is on, StartTurnState else
     */
    @Override
    public State nextState() {
        if (controller.isFinalFrenzy())
            return new ShootPeopleState(controller, true, true);
        DatabaseHandler.getInstance().save(controller);
        controller.nextTurn();
        return new InactiveState(controller, InactiveState.START_TURN_STATE);
    }
}
