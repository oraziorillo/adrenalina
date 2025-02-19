package common.events.pc_board_events;

import common.dto_model.PcBoardDTO;
import common.enums.AmmoEnum;

import java.util.List;

import static common.Constants.AMMO_CHANGED;

public class AmmoChangedEvent extends PcBoardEvent {

    private int eventID = AMMO_CHANGED;
    private short[] ammoDifference;
    private List<String> powerUpsDiscarded;
    private boolean isEarned;


    public AmmoChangedEvent(PcBoardDTO pcBoard, short[] ammoDifference, List<String> powerUpsDiscarded, boolean isEarned){
        super(pcBoard);
        this.ammoDifference = ammoDifference;
        this.powerUpsDiscarded = powerUpsDiscarded;
        this.isEarned = isEarned;
    }


    private AmmoChangedEvent(PcBoardDTO pcBoard, short[] ammoDifference, List<String> powerUpsDiscarded, boolean isEarned, boolean isPrivate){
        super(pcBoard, isPrivate);
        this.ammoDifference = ammoDifference;
        this.powerUpsDiscarded = powerUpsDiscarded;
        this.isEarned = isEarned;
    }


    private String ammoDifferenceToString(short[] ammoDifference){
        StringBuilder stringBuilder = new StringBuilder();
        for (AmmoEnum a : AmmoEnum.values())
            stringBuilder.append(isEarned ? "+ " : "- ").append(ammoDifference[a.ordinal()]).append(" ").append(a).append(" ammo").append(System.lineSeparator());
        return stringBuilder.toString();
    }


    @Override
    public String toString() {
        StringBuilder dynamicMessageBuilder = new StringBuilder();
        dynamicMessageBuilder.append(
                (censored
                    ? pcBoard.getColour().getName()
                    : "You"));
        if(isEarned)
            dynamicMessageBuilder.append(" earned:").append(System.lineSeparator()).append(ammoDifferenceToString(ammoDifference));
        else
            dynamicMessageBuilder.append(" paid:").append(System.lineSeparator()).append(ammoDifferenceToString(ammoDifference)).append(
                    powerUpsDiscarded.isEmpty()
                    ? ""
                    : "and used:" + System.lineSeparator() + powerUpsDiscarded.toString() + System.lineSeparator() + "to pay the difference");
        return dynamicMessageBuilder.toString();
    }


    @Override
    public PcBoardEvent censor() {
        return new AmmoChangedEvent(pcBoard, ammoDifference, powerUpsDiscarded, isEarned, true);
    }
}
