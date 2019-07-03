package server.controller.states;

import common.enums.AmmoEnum;
import server.controller.Controller;
import server.controller.Player;
import server.model.Pc;
import server.model.PowerUpCard;
import server.model.squares.Square;

public class RespawnState extends State {

    private Player deadPlayer;
    private int powerUpIndex;

    RespawnState(Controller controller) {
        super(controller);
        this.powerUpIndex = -1;
        recordDeath();
    }

    private void recordDeath() {
        deadPlayer = controller.getDeadPlayers().poll();
        if (controller.getGame().scoreDeath(deadPlayer.getPc(), controller.getDeadPlayers().size() == 1))
            controller.setLastPlayerIndex(controller.getCurrPlayerIndex());
        deadPlayer.getPc().drawPowerUp(1);
    }


    @Override
    public void selectPowerUp(int index) {
        if( deadPlayer.getPc().getPowerUps().get(index) != null)
            this.powerUpIndex = index;
    }


    @Override
    public boolean ok() {
        if (powerUpIndex > -1) {
            Pc pcToRespawn = deadPlayer.getPc();
            PowerUpCard powerUp = pcToRespawn.getPowerUpCard(powerUpIndex);
            AmmoEnum respawnColour = powerUp.getColour();
            Square s = controller.getGame().getSpawnPoint(respawnColour.toSquareColour());
            Square oldSquare = pcToRespawn.getCurrSquare();
            pcToRespawn.discardPowerUp(powerUp);
            pcToRespawn.spawn(s);
            oldSquare.getPcs().remove(pcToRespawn);
            controller.ackPlayer(deadPlayer, "\nA well-known historical figure took three days to resurrect, but you... you took a half turn");
            return true;
        }
        return false;
    }


    @Override
    public State nextState() {
        if (controller.getDeadPlayers().isEmpty() && controller.isNextOnDuty(deadPlayer)) {
            controller.increaseCurrPlayerIndex();
            return new StartTurnState(controller);
        }
        controller.nextTurn();
        return new InactiveState(controller, InactiveState.START_TURN_STATE);
    }
}
