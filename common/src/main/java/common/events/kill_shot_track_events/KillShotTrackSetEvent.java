package common.events.kill_shot_track_events;

import common.dto_model.KillShotTrackDTO;

import static common.Constants.KILL_SHOT_TRACK_SET;

public class KillShotTrackSetEvent extends KillShotTrackEvent {

    private int eventID = KILL_SHOT_TRACK_SET;


    public KillShotTrackSetEvent(KillShotTrackDTO killShotTrack){
        super(killShotTrack);
    }


    @Override
    public String toString() {
        return System.lineSeparator() + "The host decided: " + killShotTrack.getKillShotTrack().length + " kills to win";
    }
}
