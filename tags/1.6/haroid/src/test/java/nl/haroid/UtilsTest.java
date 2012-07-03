package nl.haroid;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Ruud de Jong
 */
public class UtilsTest {

    @Test
    public void testBepaalPeriode() {
        int periodeNummer = Utils.bepaalPeriodeNummer(21);
        Assert.assertTrue(periodeNummer > 201200);
    }

    @Test
    public void testNumberOfDaysThisMonth() {
        int numberOfDays = Utils.numberOfDaysThisMonth();
        Assert.assertTrue(numberOfDays == 30);
    }

    @Test
    public void testNumberOfDaysPreviousMonth() {
        int numberOfDays = Utils.numberOfDaysPreviousMonth();
        Assert.assertTrue(numberOfDays == 31);
    }
}
