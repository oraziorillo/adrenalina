package server.model.actions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import server.model.Pc;
import server.model.squares.Square;
import server.model.target_checkers.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MovementAction extends Action {

    @Expose private boolean selfMovement;
    @Expose private boolean linkedChecker;
    @Expose private TargetChecker destinationChecker;


    public MovementAction() {
        super();
    }


    public MovementAction(JsonObject jsonAction) {
        super(jsonAction);
        this.selfMovement = jsonAction.get("selfMovement").getAsBoolean();
        this.linkedChecker = jsonAction.get("linkedChecker").getAsBoolean();
        this.destinationChecker = new EmptyChecker();

        JsonArray json = jsonAction.get("destinationChecker").getAsJsonArray();
        for(JsonElement checker : json) {
            JsonObject jsonChecker = checker.getAsJsonObject();
            switch (jsonChecker.get("type").getAsString()) {
                case "minDistance":
                    this.destinationChecker = new MinDistanceDecorator(destinationChecker, jsonChecker.get("minDistance").getAsInt());
                    break;
                case "maxDistance":
                    this.destinationChecker = new MaxDistanceDecorator(destinationChecker, jsonChecker.get("maxDistance").getAsInt());
                    break;
                case "straightLine":
                    this.destinationChecker = new SimpleStraightLineDecorator(destinationChecker, null);
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public boolean isSelfMovement() {
        return selfMovement;
    }


    @Override
    public void selectPc(Pc targetPc) {
        if (!targets.isEmpty() && maxNumberOfTargets == 1)
            //se è già stato selezionato in un'azione precedente avrà come boolean false isParameterized
            return;
        if (!selfMovement) {
            //if are selected more Pcs than allowed, the target set becomes empty and adds the new Pc
            if (targets.size() < maxNumberOfTargets)
                targets.add(targetPc);
        }
    }


    @Override
    public void selectSquare(Square targetSquare) {
        if (this.targetSquare == null) {
            this.targetSquare = targetSquare;
        }
    }


    @Override
    public Set<Square> validSquares(Square targetSquare) {
        if (selfMovement)
            return targetChecker.validSquares(targetSquare);
        else if (linkedChecker){
            Set<Square> bothCheckers = targetChecker.validSquares(targetSquare);
            bothCheckers.retainAll(destinationChecker.validSquares(new ArrayList<>(targets).get(0).getCurrSquare()));
            return bothCheckers;
        }
        return destinationChecker.validSquares(new ArrayList<>(targets).get(0).getCurrSquare());
    }


    @Override
    public void resetAction() {
        targets.clear();
        targetSquare = null;
    }


    @Override
    public Set<Pc> apply(Pc shooter) {
        if (selfMovement)
            targets.add(shooter);
        if (!isComplete())
            return new HashSet<>();
        targets.forEach(pc -> pc.moveTo(targetSquare));
        return new HashSet<>();
    }


    @Override
    public boolean isComplete() {
        return !isParameterized() ||
                ((selfMovement || !targets.isEmpty()) && targetSquare != null);
    }
}
