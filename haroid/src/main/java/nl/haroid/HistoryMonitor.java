package nl.haroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ruud de Jong
 */
public final class HistoryMonitor {
    private static final String LOG_TAG = "HistoryMonitor";

    private static final String PERIODE_NUMMER = "pref_periode_nummer";
    private static final String VERBRUIK_DAG = "pref_verbruik_dag";
    private SharedPreferences monitorPrefs;

    public HistoryMonitor(Context context) {
        this.monitorPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setPeriodeNummer(int periodeNummer) {
        int huidigePeriodeNummer = this.monitorPrefs.getInt(PERIODE_NUMMER, 0);
        if (periodeNummer != huidigePeriodeNummer) {
            resetGeschiedenis(periodeNummer);
        }
    }

    public void setTegoed(int dagInPeriode, int tegoed) {
        if (dagInPeriode > 0 && dagInPeriode <= 31 && tegoed >= 0) {
            Log.i(LOG_TAG, "setTegoed " + tegoed + " voor dag " + dagInPeriode);
            DecimalFormat decimalFormat = new DecimalFormat("00");
            String verbruikKey = VERBRUIK_DAG + decimalFormat.format(dagInPeriode);
            SharedPreferences.Editor prefEditor = this.monitorPrefs.edit();
            prefEditor.putInt(verbruikKey, tegoed);
            prefEditor.commit();
        }
    }

    public int getTegoedGisteren(int maxTegoed) {
        boolean vandaagGevonden = false;
        for (int i=31; i>0; i--) {
            // First day of period.
            if (i==1) {
                return maxTegoed;
            }
            DecimalFormat decimalFormat = new DecimalFormat("00");
            String verbruikKey = VERBRUIK_DAG + decimalFormat.format(i);
            int saldo = this.monitorPrefs.getInt(verbruikKey, -1);
            if (!vandaagGevonden) {
                if (saldo > -1) {
                    vandaagGevonden = true;
                }
            } else {
                return saldo;
            }
        }
        return -1;
    }

    public List<UsagePoint> getUsageList() {
        int amount = Integer.parseInt(this.monitorPrefs.getString("pref_max_tegoed", "0"));
        Log.i(LOG_TAG, "max balance: " + amount);
        int currentDay = 0;
        List<UsagePoint> verbruikspuntList = new ArrayList<UsagePoint>();
        DecimalFormat decimalFormat = new DecimalFormat("00");
        for (int i=1; i<32; i++) {
            String verbruikKey = VERBRUIK_DAG + decimalFormat.format(i);
            int amountThisDay = this.monitorPrefs.getInt(verbruikKey, -1);
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
        }
        return Collections.unmodifiableList(verbruikspuntList);
    }

    private void resetGeschiedenis(int periodeNummer) {
        Log.i(LOG_TAG, "resetGeschiedenis() voor periode: " + periodeNummer);
        SharedPreferences.Editor prefEditor = this.monitorPrefs.edit();
        prefEditor.putInt(PERIODE_NUMMER, periodeNummer);
        DecimalFormat decimalFormat = new DecimalFormat("00");
        for (int i=1; i<32; i++) {
            String verbruikKey = VERBRUIK_DAG + decimalFormat.format(i);
            prefEditor.putInt(verbruikKey, -1);
        }
        prefEditor.commit();
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
