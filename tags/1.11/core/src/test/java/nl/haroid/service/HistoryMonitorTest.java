package nl.haroid.service;

import junit.framework.Assert;
import nl.haroid.access.BalanceRepository;
import nl.haroid.common.BundleType;
import nl.haroid.common.Utils;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruud de Jong
 */
public class HistoryMonitorTest extends EasyMockSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMonitorTest.class);

    @Test
    public void testUsageListEmptyList() {
        int startBalance = 21;
        int maxTegoed = 275;
        BalanceRepository balanceRepository = this.createNiceMock(BalanceRepository.class);
        EasyMock.expect(balanceRepository.getBalanceList(EasyMock.<Date>anyObject(), EasyMock.<BundleType>anyObject())).andReturn(Collections.<Integer, Integer>emptyMap());

        this.replayAll();
        HistoryMonitor fixture = new HistoryMonitor(balanceRepository);

        List<HistoryMonitor.UsagePoint> usagePointList = fixture.getUsageList (startBalance, maxTegoed);
        Assert.assertNotNull(usagePointList);
        Assert.assertEquals(0, usagePointList.size());
    }

    @Test
    public void testUsageList() {
        int startBalance = 21;
        int maxTegoed = 275;
        Map<Integer, Integer> usageMap = new HashMap<Integer, Integer>();
        Date lastDayOfPreviousPeriod = Utils.getLastDayOfPreviousPeriod(startBalance);
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastDayOfPreviousPeriod);
        int dateCode = Utils.bepaalDatumCode(cal.getTime());
        usageMap.put(dateCode, 200);

        cal.add(Calendar.DATE, 1);
        dateCode = Utils.bepaalDatumCode(cal.getTime());
        usageMap.put(dateCode, 475);

        cal.add(Calendar.DATE, 1);
        dateCode = Utils.bepaalDatumCode(cal.getTime());
        usageMap.put(dateCode, 433);

        BalanceRepository balanceRepository = this.createNiceMock(BalanceRepository.class);
        EasyMock.expect(balanceRepository.getBalanceList(EasyMock.<Date>anyObject(), EasyMock.<BundleType>anyObject())).andReturn(usageMap);

        this.replayAll();
        HistoryMonitor fixture = new HistoryMonitor(balanceRepository);

        List<HistoryMonitor.UsagePoint> usagePointList = fixture.getUsageList (startBalance, maxTegoed);
        Assert.assertNotNull(usagePointList);
        for (HistoryMonitor.UsagePoint usagePoint : usagePointList) {
            LOGGER.info("Dag in periode: " + usagePoint.getDagInPeriode());
            LOGGER.info("Balance op dag: " + usagePoint.getBalance());
            LOGGER.info("Used: " + usagePoint.getUsed());
        }
        Assert.assertEquals(usageMap.size(), usagePointList.size());
    }
}
