package model.target_checker;

import model.Character;
import model.Game;
import model.Tile;

public class MaxDistanceDecorator extends TargetCheckerDecorator{
    boolean valid
}



public class DifferentTileDecorator extends  TargetCheckerDecorator{

    public DifferentTileDecorator(Character character){
        this.thisCharacter = character;
    }

    public boolean isValid(Character possibleTargetCharacter) {
        game = new Game();
        boolean valid = false;
        Tile actionTile;
        actionTile = game.getCurrentCharacter().getCurrentTile();
        if(possibleTargetCharacter.getCurrentTile().equals(actionTile)) {
            valid == false;
        }
        else valid == true;
        return thisCharacter.is;
    }


}
