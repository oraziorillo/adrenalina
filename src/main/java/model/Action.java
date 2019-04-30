package model;

import org.json.simple.JSONObject;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public abstract class Action {
    WeaponEffect effect;
    TargetChecker targetChecker;
    LinkedList<Pc> targets;

    Action (WeaponEffect effect){
        this.effect = effect;
        targetChecker = new EmptyChecker();
        targets = new LinkedList<>();
    }

    /**
     * executes the action
     */
    abstract void apply();

    /**
     * selects the targets that are valid for the current action
     *
     * @return a Set of all possible target Tiles
     */
    public Set<Tile> validTargetTiles() {
        return targetChecker.validTiles();
    }
}




class DamageMarksAction extends Action {
    private short damage;
    private short marks;

    DamageMarksAction(JSONObject jsonAction, WeaponEffect effect){
        super(effect);
        this.damage = (short) jsonAction.get("damage");
        this.marks = (short) jsonAction.get("marks");
        JSONObject[] jsonTargetCheckers = (JSONObject[]) jsonAction.get("targetCheckers");
        for (JSONObject jsonTargetChecker : jsonTargetCheckers) {
            switch ((String)jsonTargetChecker.get("type")){
                case "visible":
                    this.targetChecker = new VisibleDecorator(targetChecker, this.effect.getCard().getDeck().getCurrGame().getCurrentPc());
                    break;
                case "blindness":
                    this.targetChecker = new BlindnessDecorator(targetChecker, this.effect.getCard().getDeck().getCurrGame().getCurrentPc());
                    break;
                case "minDistanceDecorator":
                    this.targetChecker = new MinDistanceDecorator(targetChecker, jsonTargetChecker);
                    break;
                case "maxDistanceDecorator":
                    this.targetChecker = new MaxDistanceDecorator(targetChecker, jsonTargetChecker);
                    break;
                case "straightLine":
                    this.targetChecker = new SimpleStraightLineDecorator(targetChecker, null);
                    break;
                case "beyondWallsStraightLine":
                    this.targetChecker = new BeyondWallsStraightLineDecorator(targetChecker, null);
                    break;
                case "sameRoom":
                    this.targetChecker = new SameRoomDecorator(targetChecker);
                    break;
                case "differentRoom":
                    this.targetChecker = new DifferentRoomDecorator(targetChecker);
                    break;
                default:
                    break;
            }
        }
    }

    public Set<Pc> validTargets(){
        HashSet<Pc> validTargets = new HashSet<>();
        for (Tile t : validTargetTiles()) {
            validTargets.addAll(t.getPcs());
        }
        return validTargets;
    }


    @Override
    public void apply() {
        for (Pc pc: targets) {
            if (damage != 0)
                pc.takeDamage(damage);
            if (marks != 0)
                pc.takeMarks(marks);
        }
        targets.clear();
    }

    /**
     * add a set of Pc to the list of the targets of the action
     * @param pcs list containing Pcs on which damage/marks must be applied
     */
    public void addTargets(List<Pc> pcs){
        targets.addAll(pcs);
    }
}



class MovementAction extends Action {       //metodo da modificare poichè è stato tolto il metodo move
    private int maxDist;
    private Tile destination;

    MovementAction(JSONObject jsonAction, WeaponEffect effect) {
        super(effect);
        this.maxDist = (int)jsonAction.get("maxDist");
        this.destination = null;
    }

    /**
     * sets the destination of the move action
     * @param destination tile of destination
     */
    public void setDestination(Tile destination){
        this.destination = destination;
    }

    @Override
    public void apply() {
        for (Pc pc : targets) {
            pc.move(destination, maxDist);
        }
        targets.clear();
    }
}

