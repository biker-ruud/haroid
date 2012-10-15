package nl.haroid;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

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
        Assert.assertTrue(numberOfDays == 31);
    }

    @Test
    public void testNumberOfDaysPreviousMonth() {
        int numberOfDays = Utils.numberOfDaysPreviousMonth();
        Assert.assertTrue(numberOfDays == 31);
    }

    @Test
    public void testGetLastDayOfPreviousPeriodSameMonth() {
        int startDayOfPeriod = 21;
        Calendar cal = Calendar.getInstance();
        cal.set(2012, Calendar.OCTOBER, 25);
        Date lastDayOfPreviousPeriod = Utils.getLastDayOfPreviousPeriod(startDayOfPeriod, cal);

        Assert.assertNotNull(lastDayOfPreviousPeriod);
        Calendar result = Calendar.getInstance();
        result.setTime(lastDayOfPreviousPeriod);
        Assert.assertEquals(2012, result.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.OCTOBER, result.get(Calendar.MONTH));
        Assert.assertEquals(20, result.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testGetLastDayOfPreviousPeriodSameDay() {
        int startDayOfPeriod = 21;
        Calendar cal = Calendar.getInstance();
        cal.set(2012, Calendar.OCTOBER, 21);
        Date lastDayOfPreviousPeriod = Utils.getLastDayOfPreviousPeriod(startDayOfPeriod, cal);

        Assert.assertNotNull(lastDayOfPreviousPeriod);
        Calendar result = Calendar.getInstance();
        result.setTime(lastDayOfPreviousPeriod);
        Assert.assertEquals(2012, result.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.OCTOBER, result.get(Calendar.MONTH));
        Assert.assertEquals(20, result.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testGetLastDayOfPreviousPeriodNextMonth() {
        int startDayOfPeriod = 21;
        Calendar cal = Calendar.getInstance();
        cal.set(2012, Calendar.OCTOBER, 5);
        Date lastDayOfPreviousPeriod = Utils.getLastDayOfPreviousPeriod(startDayOfPeriod, cal);

        Assert.assertNotNull(lastDayOfPreviousPeriod);
        Calendar result = Calendar.getInstance();
        result.setTime(lastDayOfPreviousPeriod);
        Assert.assertEquals(2012, result.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.SEPTEMBER, result.get(Calendar.MONTH));
        Assert.assertEquals(20, result.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testGetLastDayOfPreviousPeriodLastDayOfMonth() {
        int startDayOfPeriod = 31;
        Calendar cal = Calendar.getInstance();
        cal.set(2012, Calendar.MARCH, 5);
        Date lastDayOfPreviousPeriod = Utils.getLastDayOfPreviousPeriod(startDayOfPeriod, cal);

        Assert.assertNotNull(lastDayOfPreviousPeriod);
        Calendar result = Calendar.getInstance();
        result.setTime(lastDayOfPreviousPeriod);
        Assert.assertEquals(2012, result.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.FEBRUARY, result.get(Calendar.MONTH));
        Assert.assertEquals(29, result.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testJoin() {
        String[] array = new String[]{"a", "bb", "ccc"};
        String separator = "--";
        String result = Utils.join(array, separator);
        Assert.assertNotNull(result);
        Assert.assertEquals("a--bb--ccc", result);
    }
}
