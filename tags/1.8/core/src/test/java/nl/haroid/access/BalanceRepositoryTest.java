package nl.haroid.access;

import nl.haroid.common.BundleType;
import nl.haroid.storage.StorageOpenHelperSqliteImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * @author Ruud de Jong
 */
public class BalanceRepositoryTest {

    @Test
    public void basicTest() {
        StorageOpenHelperSqliteImpl fixture = new StorageOpenHelperSqliteImpl(BalanceRepository.DATABASE_VERSION);
        fixture.getWritableStorage();
        fixture.getWritableStorage();
        fixture.getWritableStorage();
    }

    @Test
    public void testSaveOrUpdate() {
        Date today = new Date();
        int balanceToday = 275;
        BundleType bundleType = BundleType.MAIN;

        BalanceRepository fixture = new BalanceRepository(new StorageOpenHelperSqliteImpl(BalanceRepository.DATABASE_VERSION));
        fixture.saveOrUpdate(today, balanceToday, bundleType);

        int storedBalance = fixture.getBalance(today, bundleType);
        Assert.assertEquals(balanceToday, storedBalance);
        Assert.assertEquals(-1, fixture.getBalance(today, BundleType.INTERNET));
        Assert.assertEquals(-1, fixture.getBalance(today, BundleType.SMS));
    }

    @Test
    public void testGetBalance() {
        BalanceRepository fixture = new BalanceRepository(new StorageOpenHelperSqliteImpl(BalanceRepository.DATABASE_VERSION));
        int balance = fixture.getBalance(new Date(), BundleType.MAIN);
        Assert.assertEquals(-1, balance);
    }

    @Test
    public void testMostRecentBalance() {
        Calendar cal = Calendar.getInstance();
        Date today = new Date();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date tomorrow = cal.getTime();
        int balanceToday = 275;
        BundleType bundleType = BundleType.MAIN;

        BalanceRepository fixture = new BalanceRepository(new StorageOpenHelperSqliteImpl(BalanceRepository.DATABASE_VERSION));
        Assert.assertEquals(-1, fixture.getMostRecentBalance(yesterday, tomorrow, bundleType));

        fixture.saveOrUpdate(today, balanceToday, bundleType);

        Assert.assertEquals(balanceToday, fixture.getMostRecentBalance(yesterday, tomorrow, bundleType));
        Assert.assertEquals(-1, fixture.getMostRecentBalance(today, tomorrow, bundleType));
        Assert.assertEquals(-1, fixture.getMostRecentBalance(yesterday, today, bundleType));
    }

    @Test
    public void testBalanceList() {
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = cal.getTime();
        int balanceToday = 275;
        BundleType bundleType = BundleType.MAIN;

        BalanceRepository fixture = new BalanceRepository(new StorageOpenHelperSqliteImpl(BalanceRepository.DATABASE_VERSION));
        fixture.saveOrUpdate(today, balanceToday, bundleType);
        Map<Integer, Integer> resultMap = fixture.getBalanceList(yesterday, bundleType);

        Assert.assertNotNull(resultMap);
        Assert.assertEquals(1, resultMap.size());
        Assert.assertEquals(new Integer(balanceToday), resultMap.values().iterator().next());

        resultMap = fixture.getBalanceList(today, bundleType);
        Assert.assertNotNull(resultMap);
        Assert.assertEquals(0, resultMap.size());
    }

    @Test
    public void testReset() {
        BalanceRepository fixture = new BalanceRepository(new StorageOpenHelperSqliteImpl(BalanceRepository.DATABASE_VERSION));
        fixture.reset();
    }
}
