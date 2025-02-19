package server.model;

import common.enums.AmmoEnum;
import common.enums.SquareColourEnum;
import common.exceptions.EmptySquareException;
import common.exceptions.NotEnoughAmmoException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import server.model.squares.SpawnPoint;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpawnPointTest {

    @Mock
    private Pc pc;

    private final SquareColourEnum colour = SquareColourEnum.RED;

    @Mock
    private WeaponCard card0, card1, card2;

    @Mock
    private WeaponCard card3, card4, card5;

    private int x = 2;
    private int y = 2;
    private SpawnPoint tested;

    @Mock
    private Deck<WeaponCard> deck;

    @Mock
    private ModelEventHandler eventHandler;


    @Before
    public void setupAndConstructorTest() {
        when(deck.draw()).thenReturn(card0).thenReturn(card1).thenReturn(card2).
                thenReturn(card3).thenReturn(card4).thenReturn(card5);
        tested = new SpawnPoint(x, y, colour);
        tested.init(deck, null);
        tested.addModelEventHandler(eventHandler);
    }


    @Test
    public void collectWorksFineWhenPcIsFullyArmed() throws EmptySquareException, NotEnoughAmmoException {
        int grabIndex = 1;
        int dropIndex = 2;
        short[] ammoTile = new short[]{1, 1, 1};
        WeaponCard weaponToDrop = Mockito.mock(WeaponCard.class);
        when(pc.isFullyArmed()).thenReturn(true);
        when(pc.hasEnoughAmmo(any())).thenReturn(true);
        when(pc.weaponAtIndex(dropIndex)).thenReturn(weaponToDrop);
        when(card1.getAmmo()).thenReturn(ammoTile);
        when(card1.getColour()).thenReturn(AmmoEnum.BLUE);
        tested.setWeaponToGrabIndex(grabIndex);
        tested.setWeaponToDropIndex(dropIndex);
        tested.collect(pc);
        assertSame(weaponToDrop, tested.weaponAtIndex(grabIndex));
    }


    @Test
    public void collectWorksFineWhenPcIsNotFullyArmed() throws NotEnoughAmmoException, EmptySquareException {
        int grabIndex = 1;
        short[] ammoTile = new short[]{1, 1, 1};
        when(pc.isFullyArmed()).thenReturn(false);
        when(pc.hasEnoughAmmo(any())).thenReturn(true);
        when(card1.getAmmo()).thenReturn(ammoTile);
        when(card1.getColour()).thenReturn(AmmoEnum.BLUE);
        tested.setWeaponToGrabIndex(grabIndex);
        tested.collect(pc);
        assertNull(tested.weaponAtIndex(grabIndex));
    }


    @Test
    public void refillRefillsTheEntireSquare() throws NotEnoughAmmoException, EmptySquareException {
        collectWorksFineWhenPcIsNotFullyArmed();
        when(deck.size()).thenReturn(6);
        assertNotNull(tested.weaponAtIndex(0));
        assertNotNull(tested.weaponAtIndex(2));
        assertNull(tested.weaponAtIndex(1));
        tested.refill();
        assertNotNull(tested.weaponAtIndex(1));
    }



    @Test
    public void weaponAtIndexWorksFine() {
        assertEquals(card0, tested.weaponAtIndex(0));
        assertNotEquals(card1, tested.weaponAtIndex(2));
    }


    @Test
    public void resetIndexWorksFine() throws IllegalArgumentException {
        int grabIndex = 2;
        int dropIndex = 1;
        tested.setWeaponToGrabIndex(grabIndex);
        tested.setWeaponToDropIndex(dropIndex);
        tested.resetWeaponIndexes();
        assertThrows(IllegalStateException.class, () -> tested.collect(pc));
        tested.setWeaponToGrabIndex(grabIndex);
        when(pc.hasEnoughAmmo(any())).thenReturn(true);
        when(pc.isFullyArmed()).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> tested.collect(pc));
    }
}
