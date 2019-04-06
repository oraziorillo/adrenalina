package model;

public class Character {
    private CharColour[] damageTrack;
    private Weapon[] weapons;
    private Powerup[] powerups;
    private short[] marks;
    private int points;
    private short numOfDeath;
    private short ammos[];
    private Tile position;
    private CharColour colour;

    public Character (CharColour colour){
        this.damageTrack = new CharColour[12];
        this.weapons = new Weapon[3];
        this.powerups = new Powerup[3];
        this.marks = new short[5];
        this.points = 0;
        this.numOfDeath = 0;
        this.ammos = new short[3];
        this.position = null;       //viene posto a null perchè ancora non è stato generato sulla mappa
        this.colour = colour;
    }

    public void addDamage(CharColour colour, int inflictedDamage, int inflictedMarks){
        int index = 0;
        int TotalDamage = marks[colour.ordinal()] + inflictedDamage;
        boolean overkilled = false;
        while (damageTrack[index] != null){
            index = index + 1;
        }
        while(TotalDamage != 0){       //il controller dovrà controllare ogni volta se il giocatore è morto, verificando che all'indice 10 il valore sia != null
            damageTrack[index] = colour;
            if(index > 10){
                overkilled = true;
                TotalDamage = 0;
            }
            else{
                index += 1;
            }
        }
        marks[colour.ordinal()] = inflictedMarks;        //conviene implementare un metodo SetMask?
    }
    public void collectWeapon(Weapon w){
        short index = 0;
        short newWeaponIndex;
        while(index < 3 && weapons[i] != null){
            index +=1;
        }
        if(index = 3){
            newWeaponIndex = dropWeapon();       //da implementare: chiedi all'utente quale arma vuole lasciare
        }
        weapons[newWeaponIndex] = w;
    }
    public void collectPowerup(Powerup p){
        //TODO: method
    }
    public void addPoints(int points){
        this.points+=points;
    }       //TODO: qui il controller dovrebbe fare dei controlli per vedere se è morto

    public void collectAmmos(short[] ammos){
        if(ammos.length!=this.ammos.length){
            throw new IllegalArgumentException("Array di dimensione errata");
        }
        for(int i=0; i<ammos.length; i++){
            this.ammos[i]+=ammos[i];
            if(ammos[i]>3)
                ammos[i]=3;
        }
    }

    public void respawn(RoomColour colour){
        this.damage = new CharColour[12];
        numOfDeath = numOfDeath + 1;
        this.position = //TODO: è dato dal colour che viene passato come parametro
    }

}
