package nl.haroid.access;

import junit.framework.Assert;
import nl.haroid.common.BundleType;
import nl.haroid.storage.StorageOpenHelperSqliteImpl;
import org.junit.Test;

import java.util.Date;

/**
 * @author Ruud de Jong
 */
public class BalanceRepositoryTest {

    @Test
    public void testGetBalance() {
        BalanceRepository fixture = new BalanceRepository(new StorageOpenHelperSqliteImpl());
        int balance = fixture.getBalance(new Date(), BundleType.MAIN);
        Assert.assertEquals(-1, balance);
    }
}
