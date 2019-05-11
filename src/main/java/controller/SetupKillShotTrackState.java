package controller;

public class SetupKillShotTrackState extends State{

    @Override
    public boolean setNumberOfSkulls(int n) {
        controller.getGame().initKillShotTrack(n);
        return true;
    }

    public void nextState(){
        controller.setCurrState(controller.firstTurnState);
    }
}
