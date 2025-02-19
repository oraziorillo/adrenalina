package common.dto_model;

import common.enums.PcColourEnum;
import common.enums.SquareColourEnum;

import java.util.Set;

public class SquareDTO implements DTO {

    private int row;
    private int col;
    private SquareColourEnum colour;
    private boolean targetable;
    private Set<PcColourEnum> pcs;
    private AmmoTileDTO ammoTile;
    private WeaponCardDTO[] weapons;

    public WeaponCardDTO[] getWeapons() {
        return weapons;
    }

    public void setWeapons(WeaponCardDTO[] weapons) {
        this.weapons = weapons;
    }

    public AmmoTileDTO getAmmoTile() {
        return ammoTile;
    }

    public void setAmmoTile(AmmoTileDTO ammoTile) {
        this.ammoTile = ammoTile;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public SquareColourEnum getColour() {
        return colour;
    }

    public void setColour(SquareColourEnum colour) {
        this.colour = colour;
    }

    public boolean isTargetable() {
        return targetable;
    }

    public void setTargetable(boolean targetable) {
        this.targetable = targetable;
    }

    public Set<PcColourEnum> getPcs() {
        return pcs;
    }

    public void setPcs(Set<PcColourEnum> pcs) {
        this.pcs = pcs;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  SquareDTO){
            SquareDTO s = (SquareDTO)obj;
            return s.col == this.col && s.row == this.row;
        }else {
            return false;
        }
    }


    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }


    public String description(){
        StringBuilder description = new StringBuilder();
        description.append("(").append(row).append(",").append(col).append(")");
        description.append(System.lineSeparator()).append(colour).append(System.lineSeparator()).append(ammoTile == null ? "Spawn point" + System.lineSeparator() + "Weapons:" : "Ammo square" + System.lineSeparator() + "Ammo tile:");
        int i = 1;
        if (ammoTile == null)
            for (WeaponCardDTO w : weapons) {
                description.append(System.lineSeparator()).append("[").append(i).append("] ").append(w.getName());
                i++;
            }
        else
            description.append(ammoTile.toString());
        return description.toString();
    }
}
