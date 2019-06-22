package server.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class AmmoTileDeckTest {

    public Deck<AmmoTile> deck;

    @Before
    public void ammoTileDeckConstructionFine() throws FileNotFoundException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        Type ammoTileType = new TypeToken<ArrayList<AmmoTile>>(){}.getType();

        JsonReader reader;

        reader = new JsonReader(new FileReader("src/main/resources/json/ammoTiles.json"));
        ArrayList<AmmoTile> ammoTiles = gson.fromJson(reader, ammoTileType);
        ammoTiles.forEach(AmmoTile::setHasPowerUp);
        deck = new Deck<>();
        ammoTiles.forEach(a -> deck.add(a));
    }


    @Test
    public void printDeck(){
        deck.print();
    }
}
