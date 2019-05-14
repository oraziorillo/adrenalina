package model;


import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;


@RunWith(Theories.class)
public class AmmoTileTest {
    @DataPoints
    public static final boolean[] hasPowerup = {true, false};
    @DataPoints
    public static final short[][] ammos = {{1, 1, 1}, {3, 0, 0}, {2, 0, 0}, {0, -1, 4}, {4, 5, 6}};

    @Theory
    public void doesNotAlterCostructionParameters(short[] ammos, boolean hasPowerup) {
        assumeTrue("Invalid parameters", AmmoTile.validParameters(ammos, hasPowerup));
        AmmoTile tested = new AmmoTile(ammos, hasPowerup);
        assertEquals("Different ammos", ammos, tested.getAmmos());
        assertEquals("Different powerup", hasPowerup, tested.containsPowerup());
    }

    @Theory
    public void throwsExceptionOnInvalidParameters(short[] ammos, boolean hasPowerup) {
        assumeFalse("Valid parameters", AmmoTile.validParameters(ammos, hasPowerup));
        assertThrows("Exception not thrown", IllegalArgumentException.class,()->new AmmoTile(ammos, hasPowerup));
    }

    @Theory
    public void parameterValidatorWorksFine(short[] ammos, boolean hasPowerup) {
        boolean correct = true;
        short t = 0;
        if (hasPowerup) {
            t++;
        }
        for (short s : ammos) {
            if (s < 0) {
                correct = false;
            }
            t += s;
        }
        if (t != Constants.AMMOS_PER_CARD) {
            correct = false;
        }
        assertEquals(AmmoTile.validParameters(ammos, hasPowerup), correct);
    }

}
