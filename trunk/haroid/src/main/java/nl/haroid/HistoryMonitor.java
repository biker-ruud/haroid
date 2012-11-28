package nl.haroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import nl.haroid.access.BalanceRepository;
import nl.haroid.common.BundleType;
import nl.haroid.common.Utils;
import nl.haroid.storage.StorageOpenHelperAndroidImpl;

import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Ruud de Jong
 */
public final class HistoryMonitor {
    private static final String LOG_TAG = "HistoryMonitor";

    private static final String PERIODE_NUMMER = "pref_periode_nummer";
    private static final String VERBRUIK_DAG = "pref_verbruik_dag";
    private final SharedPreferences monitorPrefs;
    private final BalanceRepository balanceRepository;

    public HistoryMonitor(SharedPreferences monitorPrefs, Context context) {
        this.monitorPrefs = monitorPrefs;
        this.balanceRepository = new BalanceRepository(new StorageOpenHelperAndroidImpl(context, BalanceRepository.DATABASE_NAME, BalanceRepository.DATABASE_VERSION));
    }

    public void convertToDatabase(int startBalance) {
        Date lastDayOfPreviousPeriod = Utils.getLastDayOfPreviousPeriod(startBalance);
        Log.i(LOG_TAG, "convertToDatabase: startBalance=" + startBalance);
        DecimalFormat decimalFormat = new DecimalFormat("00");
        for (int i=1; i<32; i++) {
            String verbruikKey = VERBRUIK_DAG + decimalFormat.format(i);
            int amountThisDay = this.monitorPrefs.getInt(verbruikKey, -1);
            if (amountThisDay != -1) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(lastDayOfPreviousPeriod);
                cal.add(Calendar.DATE, i);
                Log.i(LOG_TAG, "convertToDatabase: daynr " + i + ", amount: " + amountThisDay + ", date: " + cal);
                balanceRepository.saveOrUpdate(cal.getTime(), amountThisDay, BundleType.MAIN);
                SharedPreferences.Editor prefEditor = this.monitorPrefs.edit();
                prefEditor.remove(verbruikKey);
                prefEditor.commit();
            }
        }
    }

    public void setPeriodeNummer(int periodeNummer) {
        int huidigePeriodeNummer = this.monitorPrefs.getInt(PERIODE_NUMMER, 0);
        if (periodeNummer != huidigePeriodeNummer) {
            resetGeschiedenis(periodeNummer);
        }
    }

    public void setTegoed(int dagInPeriode, int tegoed, Date datum) {
        if (dagInPeriode > 0 && dagInPeriode <= 31 && tegoed >= 0) {
            Log.i(LOG_TAG, "setTegoed " + tegoed + " voor dag " + dagInPeriode);
            balanceRepository.saveOrUpdate(datum, tegoed, BundleType.MAIN);
        }
    }

    public int getBalance(int dagInPeriode, Date datum) {
        if (dagInPeriode > 0 && dagInPeriode <= 31) {
            return balanceRepository.getBalance(datum, BundleType.MAIN);
        }
        return -1;
    }

    public int getTegoedGisteren(int maxTegoed, int startBalance) {
        Date lastDayOfPreviousPeriod = Utils.getLastDayOfPreviousPeriod(startBalance);
        return balanceRepository.getMostRecentBalance(lastDayOfPreviousPeriod, new Date(), BundleType.MAIN);
    }

    public List<UsagePoint> getUsageList(int startBalance, int maxBalance) {
        Log.i(LOG_TAG, "getUsageList()");
        Date lastDayOfPreviousPeriod = Utils.getLastDayOfPreviousPeriod(startBalance);
        Map<Integer, Integer> balanceList = balanceRepository.getBalanceList(lastDayOfPreviousPeriod, BundleType.MAIN);
        Log.i(LOG_TAG, "Balance List");
        for (Map.Entry<Integer, Integer> balanceListEntry : balanceList.entrySet()) {
            Log.i(LOG_TAG, balanceListEntry.getKey() + ": " + balanceListEntry.getValue());
        }
        int amount = maxBalance;
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastDayOfPreviousPeriod);
        int dateCode = Utils.bepaalDatumCode(cal.getTime());
        if (balanceList.get(dateCode) != null) {
            amount = balanceList.get(dateCode);
        }
        Log.i(LOG_TAG, "max balance of last period: " + amount);
        int currentDay = 0;
        List<UsagePoint> verbruikspuntList = new ArrayList<UsagePoint>();
        cal.add(Calendar.DATE, 1); // cal is now first day of period
        for (int i=1; i<32; i++) {
            int amountThisDay = -1;
            dateCode = Utils.bepaalDatumCode(cal.getTime());
            Log.i(LOG_TAG, "Searching Balance List for: " + dateCode);
            if (balanceList.get(dateCode) != null) {
                amountThisDay = balanceList.get(dateCode);
                Log.i(LOG_TAG, "Found it.");
            }
            if (amountThisDay != -1) {
                if (amountThisDay <= amount) {
                    // Normal day in period
                    int numberOfDays = i - currentDay;
                    int usageTheseDays = amount - amountThisDay;
                    int averageUsage = usageTheseDays / numberOfDays;
                    for (int j=(currentDay+1); j<=i; j++) {
                        int balance = -1;
                        if (j == i) {
                            balance = amountThisDay;
                        }
                        verbruikspuntList.add(new UsagePoint(j, balance, averageUsage));
                    }
                    currentDay = i;
                    amount = amountThisDay;
                } else if (amountThisDay > amount) {
                    // First day of new period with amount of previous period.
                    for (int j=(currentDay+1); j<=i; j++) {
                        int balance = -1;
                        if (j == i) {
                            balance = amountThisDay;
                        }
                        verbruikspuntList.add(new UsagePoint(j, balance, 0));
                    }
                    currentDay = i;
                    amount = amountThisDay;
                }
            }
            cal.add(Calendar.DATE, 1); // goto next day
        }
        return Collections.unmodifiableList(verbruikspuntList);
    }

    public void resetHistory() {
        Log.i(LOG_TAG, "resetHistory().");
        balanceRepository.reset();
    }

    private void resetGeschiedenis(int periodeNummer) {
        Log.i(LOG_TAG, "resetGeschiedenis() voor periode: " + periodeNummer);
        SharedPreferences.Editor prefEditor = this.monitorPrefs.edit();
        prefEditor.putInt(PERIODE_NUMMER, periodeNummer);
        prefEditor.commit();
        resetHistory();
    }

    class UsagePoint {
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
