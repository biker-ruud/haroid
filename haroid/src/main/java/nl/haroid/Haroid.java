package nl.haroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

public final class Haroid extends Activity implements TegoedConsumer {
    private static final String LOG_TAG = "Haroid";
    private static final String CURRENT_TEGOED = "current tegoed";

    private HistoryMonitor historyMonitor;

    private int currentTegoed = -1;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "onCreate");

        setContentView(R.layout.main);
        initControls();
        if (savedInstanceState != null) {
            Log.i(LOG_TAG, "onCreate has savedInstanceState");
            this.currentTegoed = savedInstanceState.getInt(CURRENT_TEGOED, -1);
            Log.i(LOG_TAG, "onCreate tegoed: " + this.currentTegoed);
            if (this.currentTegoed != -1) {
                setTegoedProgress(this.currentTegoed);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (this.currentTegoed != -1) {
            outState.putInt(CURRENT_TEGOED, currentTegoed);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int maxTegoed = Integer.parseInt(prefs.getString("pref_max_tegoed", "0"));
        int startTegoed = Integer.parseInt(prefs.getString("pref_start_tegoed", "0"));
        bepaalGeschiedenis(startTegoed);
        int maxPeriod = berekenDuurTegoed(startTegoed);
        tekenVerbruik(maxTegoed, maxPeriod);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(LOG_TAG, "onCreateOptionsMenu");
        menu.add(Menu.NONE, R.id.menuSettings, 0, getString(R.string.settings));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(LOG_TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.menuSettings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                Log.i(LOG_TAG, "Verkeerde optie");
                return super.onOptionsItemSelected(item);
        }
    }

    private void initControls() {
        this.historyMonitor = new HistoryMonitor(this);
        Button tegoedButton = (Button) findViewById(R.id.ButtonTegoed);

        tegoedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startHaring();
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int startTegoed = Integer.parseInt(prefs.getString("pref_start_tegoed", "0"));
        berekenDuurTegoed(startTegoed);
    }

    private void startHaring() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String emailAdres = prefs.getString("pref_username", "");
        String wachtwoord = prefs.getString("pref_password", "");

        if (emailAdres != null && emailAdres.length() > 0 && wachtwoord != null && wachtwoord.length() > 0) {
            TextView tegoedView = (TextView) findViewById(R.id.TextTegoed);
            tegoedView.setText(getString(R.string.periodeTegoed) + " wordt opgehaald.");
            HaringTask haringTask = new HaringTask();
            haringTask.setTegoedConsumer(this);
            haringTask.execute(emailAdres, wachtwoord);
        }
    }

    private void tekenVerbruik(int maxTegoed, int maxPeriod) {
        Log.i(LOG_TAG, "tekenVerbruik");
        if (maxTegoed > 0 && maxPeriod > 0) {
            List<HistoryMonitor.UsagePoint> usageList = this.historyMonitor.getUsageList();
            MonthlyGraphView verbruikGraph = (MonthlyGraphView) findViewById(R.id.MonthlyGraphVerbruik);
            verbruikGraph.setMaxPeriod(maxPeriod);
            verbruikGraph.setMaxUnits(maxTegoed);
            verbruikGraph.setUsage(usageList);
            verbruikGraph.invalidate();
            DailyGraphView dagelijksVerbruikGraph = (DailyGraphView) findViewById(R.id.DailyGraphVerbruik);
            dagelijksVerbruikGraph.setMaxPeriod(maxPeriod);
            dagelijksVerbruikGraph.setMaxUnits(maxTegoed);
            dagelijksVerbruikGraph.setUsage(usageList);
            dagelijksVerbruikGraph.invalidate();
        }
    }

    private int berekenDuurTegoed(int startTegoed) {
        Log.i(LOG_TAG, "berekenDuurTegoed");
        ProgressBar duurTegoedProgress = (ProgressBar) findViewById(R.id.PbarTijd);
        if (startTegoed == 0) {
            // Niet ingesteld.
            duurTegoedProgress.setProgress(0);
            return 0;
        }
        int maxPeriod = berekenMaxPeriod(startTegoed);
        Calendar cal = Calendar.getInstance();
        int huidigeDagVdMaand = cal.get(Calendar.DAY_OF_MONTH);
        int verbruik = 0;
        if (huidigeDagVdMaand < startTegoed) {
            // Tegoed begonnen in vorige maand
            Log.i(LOG_TAG, "Tegoed begonnen in vorige maand");
            verbruik = (maxPeriod - startTegoed) + 1;
            verbruik += huidigeDagVdMaand;
        } else {
            // Tegoed begonnen in deze maand.
            Log.i(LOG_TAG, "Tegoed begonnen in deze maand");
            verbruik = (huidigeDagVdMaand - startTegoed) + 1;
        }
        int nogTeGaan = maxPeriod - verbruik;
        int currentPeriod = verbruik;
        Log.i(LOG_TAG, "currentPeriod: " + currentPeriod);
        Log.i(LOG_TAG, "maxPeriod: " + maxPeriod);
        duurTegoedProgress.setMax(maxPeriod);
        duurTegoedProgress.setProgress(nogTeGaan);
        TextView tijdView = (TextView) findViewById(R.id.TextTijd);
        if (nogTeGaan == 1) {
            tijdView.setText(getString(R.string.duurPeriode) + " nog " + nogTeGaan + " dag te gaan.");
        } else if (nogTeGaan < 1) {
            tijdView.setText(getString(R.string.duurPeriode) + " laatste dag.");
        } else {
            tijdView.setText(getString(R.string.duurPeriode) + " nog " + nogTeGaan + " dagen te gaan.");
        }
        return maxPeriod;
    }

    private int berekenMaxPeriod(int startTegoed) {
        Calendar cal = Calendar.getInstance();
        int huidigeDagVdMaand = cal.get(Calendar.DAY_OF_MONTH);
        if (huidigeDagVdMaand < startTegoed) {
            // Tegoed begonnen in vorige maand
            Log.i(LOG_TAG, "Tegoed begonnen in vorige maand");
            return Utils.numberOfDaysPreviousMonth();
        } else {
            // Tegoed begonnen in deze maand.
            Log.i(LOG_TAG, "Tegoed begonnen in deze maand");
            return Utils.numberOfDaysThisMonth();
        }
    }

    private void bepaalGeschiedenis(int startTegoed) {
        if (startTegoed == 0) {
            // Niet ingesteld
            return;
        }
        int periodeNummer = Utils.bepaalPeriodeNummer(startTegoed);
        Log.i(LOG_TAG, "periodeNummer: " + periodeNummer);
        this.historyMonitor.setPeriodeNummer(periodeNummer);
    }

    @Override
    public void setTegoed(int tegoed) {
        this.currentTegoed = tegoed;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int maxTegoed = Integer.parseInt(prefs.getString("pref_max_tegoed", "0"));
        int startTegoed = Integer.parseInt(prefs.getString("pref_start_tegoed", "0"));
        if (tegoed >= 0 && maxTegoed > 0) {
            int dagInPeriode = Utils.bepaaldDagInPeriode(startTegoed);
            Log.i(LOG_TAG, "dagInPeriode: " + dagInPeriode);
            this.historyMonitor.setTegoed(dagInPeriode, tegoed);
            setTegoedProgress(tegoed);
            List<HistoryMonitor.UsagePoint> usageList = this.historyMonitor.getUsageList();
            MonthlyGraphView verbruikGraph = (MonthlyGraphView) findViewById(R.id.MonthlyGraphVerbruik);
            verbruikGraph.setUsage(usageList);
            verbruikGraph.invalidate();
            DailyGraphView dagelijksVerbruikGraph = (DailyGraphView) findViewById(R.id.DailyGraphVerbruik);
            dagelijksVerbruikGraph.setUsage(usageList);
            dagelijksVerbruikGraph.invalidate();
        }
    }

    private void setTegoedProgress(int tegoed) {
        Log.i(LOG_TAG, "setTegoedProgress: " + tegoed);
        ProgressBar tegoedProgress = (ProgressBar) findViewById(R.id.PbarTegoed);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int startTegoed = Integer.parseInt(prefs.getString("pref_start_tegoed", "0"));
        int maxTegoed = Integer.parseInt(prefs.getString("pref_max_tegoed", "0"));
        if (tegoed >= 0 && maxTegoed > 0) {
            TextView tegoedView = (TextView) findViewById(R.id.TextTegoed);
            tegoedView.setText(getString(R.string.periodeTegoed) + " " + tegoed + " eenheden.");
            tegoedProgress.setMax(Math.max(tegoed, maxTegoed));
            tegoedProgress.setProgress(tegoed);
        }
        int tegoedGisteren = this.historyMonitor.getTegoedGisteren(maxTegoed);
        int verbruikVandaag = tegoedGisteren - tegoed;
        if (tegoedGisteren > -1 && verbruikVandaag >= 0) {
            int maxPeriod = berekenMaxPeriod(startTegoed);
            int procentueelVerbruik = (100 * verbruikVandaag * maxPeriod) / maxTegoed;
            TextView dagVerbruikView = (TextView) findViewById(R.id.TextDagVerbruik);
            dagVerbruikView.setText(getString(R.string.dagVerbruik) + " " + verbruikVandaag + " eenheden. (" + procentueelVerbruik + "% van de dag)");
            dagVerbruikView.setVisibility(View.VISIBLE);
        }
    }
}

