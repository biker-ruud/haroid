package nl.haroid.service;

import nl.haroid.access.BalanceRepository;
import nl.haroid.common.BundleType;
import nl.haroid.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Ruud de Jong
 */
public final class HistoryMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMonitor.class);

    private final BalanceRepository balanceRepository;

    public HistoryMonitor(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public void convertToDatabase(Map<Integer, Integer> prefVerbruikMap, int startBalance) {
        Date lastDayOfPreviousPeriod = Utils.getLastDayOfPreviousPeriod(startBalance);
        LOGGER.info("convertToDatabase: startBalance=" + startBalance);
        for (Map.Entry<Integer, Integer> prefVerbruikItem : prefVerbruikMap.entrySet()) {
            int dagNummer = prefVerbruikItem.getKey();
            int dagVerbruik = prefVerbruikItem.getValue();
            Calendar cal = Calendar.getInstance();
            cal.setTime(lastDayOfPreviousPeriod);
            cal.add(Calendar.DATE, dagNummer);
            LOGGER.info("convertToDatabase: daynr " + dagNummer + ", amount: " + dagVerbruik + ", date: " + cal);
            balanceRepository.saveOrUpdate(cal.getTime(), dagVerbruik, BundleType.MAIN);
        }
    }

    public void setTegoed(int tegoed, Date datum) {
        if (tegoed >= 0) {
            LOGGER.info("setTegoed " + tegoed + " voor dag " + datum);
            balanceRepository.saveOrUpdate(datum, tegoed, BundleType.MAIN);
        }
    }

    public int getBalance(int dagInPeriode, Date datum) {
        if (dagInPeriode > 0 && dagInPeriode <= 31) {
            return balanceRepository.getBalance(datum, BundleType.MAIN);
        }
        return -1;
    }

    public int getTegoedGisteren(int startBalance) {
        Date lastDayOfPreviousPeriod = Utils.getLastDayOfPreviousPeriod(startBalance);
        return balanceRepository.getMostRecentBalance(lastDayOfPreviousPeriod, new Date(), BundleType.MAIN);
    }

    public List<UsagePoint> getUsageList(int startBalance, int maxBalance) {
        LOGGER.info("getUsageList()");
        Date lastDayOfPreviousPeriod = Utils.getLastDayOfPreviousPeriod(startBalance);
        Map<Integer, Integer> balanceList = balanceRepository.getBalanceList(lastDayOfPreviousPeriod, BundleType.MAIN);
        LOGGER.info("Balance List");
        for (Map.Entry<Integer, Integer> balanceListEntry : balanceList.entrySet()) {
            LOGGER.info(balanceListEntry.getKey() + ": " + balanceListEntry.getValue());
        }
        int amount = maxBalance;
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastDayOfPreviousPeriod);
        int dateCode = Utils.bepaalDatumCode(cal.getTime());
        if (balanceList.get(dateCode) != null) {
            amount = balanceList.get(dateCode);
        }
        int maxBalanceLastPeriod = amount;
        LOGGER.info("max balance of last period: " + amount);
        int currentDay = 0;
        List<UsagePoint> verbruikspuntList = new ArrayList<UsagePoint>();
        cal.add(Calendar.DATE, 1); // cal is now first day of period
        for (int i=1; i<32; i++) {
            int amountThisDay = -1;
            dateCode = Utils.bepaalDatumCode(cal.getTime());
            LOGGER.info("Searching Balance List for: " + dateCode);
            if (balanceList.get(dateCode) != null) {
                amountThisDay = balanceList.get(dateCode);
                LOGGER.info("Found it.");
            }
            if (amountThisDay != -1) {
                if (amountThisDay <= amount) {
                    // Normal day in period
                    int numberOfDays = i - currentDay;
                    int usageTheseDays = amount - amountThisDay;
                    int averageUsage = usageTheseDays / numberOfDays;
                    verbruikspuntList.addAll(genereerVerbruikspuntenLijst(currentDay, i, amountThisDay, averageUsage));
                    currentDay = i;
                    amount = amountThisDay;
                } else if (amountThisDay > amount) {
                    // First day of new period with amount of previous period.
                    verbruikspuntList.addAll(genereerVerbruikspuntenLijst(currentDay, i, amountThisDay, 0));
                    currentDay = i;
                    amount = amountThisDay;
                }
            }
            cal.add(Calendar.DATE, 1); // goto next day
        }
        // Insert last day of previous period at start of list.
        if (verbruikspuntList.size() > 0) {
            UsagePoint firstPoint = verbruikspuntList.get(0);
            if (firstPoint.getDagInPeriode() == 1 && firstPoint.getBalance() > maxBalanceLastPeriod) {
                verbruikspuntList.add(0, new UsagePoint(0, maxBalanceLastPeriod, 0));
            }
        }
        return Collections.unmodifiableList(verbruikspuntList);
    }

    private List<UsagePoint> genereerVerbruikspuntenLijst(int currentDay, int i, int amountThisDay, int averageUsage) {
        List<UsagePoint> verbruikspuntList = new ArrayList<UsagePoint>();
        for (int j=(currentDay+1); j<=i; j++) {
            int balance = -1;
            if (j == i) {
                balance = amountThisDay;
            }
            verbruikspuntList.add(new UsagePoint(j, balance, averageUsage));
        }
        return verbruikspuntList;
    }

    public void resetHistory() {
        LOGGER.info("resetHistory().");
        balanceRepository.reset();
    }

    public class UsagePoint {
        private int dagInPeriode;
        private int balance;
        private int used;

        UsagePoint(int dagInPeriode, int balance, int used) {
            this.dagInPeriode = dagInPeriode;
            this.balance = balance;
            this.used = used;
        }

        public int getDagInPeriode() {
            return dagInPeriode;
        }

        public int getBalance() {
            return balance;
        }

        public int getUsed() {
            return used;
        }
    }
}
