package nl.haroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import nl.haroid.common.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Ruud de Jong
 */
public final class Haroid extends Activity implements TegoedConsumer {
    private static final String LOG_TAG = "Haroid";

    private boolean firstTime = true;
    private HaroidApp app;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this BundleType contains the data it most
     *                           recently supplied in onSaveInstanceState(BundleType). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "onCreate");
        this.app = HaroidApp.getInstance();

        setContentView(R.layout.main);
        initControls();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
        if (properSettings()) {
            int currentBalance = this.app.getCurrentBalance();
            if (currentBalance != -1) {
                setTegoedProgress(currentBalance);
            }
            updateLatestUpdate();
            HaroidApp.Stats stats = this.app.recalculate();
            setProgressBars(stats);
            tekenVerbruik(stats.maxBalance, stats.maxPeriod, this.app.getUsageList());
        } else if (this.firstTime) {
            gotoSettings();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.icon);
            builder.setTitle(getString(R.string.noSettings));
            builder.setMessage(getString(R.string.haroidHasNoSettings))
                    .setCancelable(true)
                    .setPositiveButton(getString(R.string.gotoSettings), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            gotoSettings();
                        }
                    })
                    .setNegativeButton(getString(R.string.exitApp), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            closeApplication();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.firstTime = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(LOG_TAG, "onCreateOptionsMenu");
        menu.add(Menu.NONE, R.id.menuSettings, 0, getString(R.string.settings));
        menu.add(Menu.NONE, R.id.menuAbout, 0, getString(R.string.about));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(LOG_TAG, "onOptionsItemSelected");
        String versionName = null;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "";
        }
        switch (item.getItemId()) {
            case R.id.menuSettings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menuAbout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.icon);
                builder.setTitle(getString(R.string.aboutHaroid));
                builder.setMessage("Haroid " + versionName)
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.okButtonText), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            default:
                Log.i(LOG_TAG, "Verkeerde optie");
                return super.onOptionsItemSelected(item);
        }
    }

    private void gotoSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void closeApplication() {
        this.finish();
    }

    private boolean properSettings() {
        return (HaroidApp.getEmailAdres().length() > 0 && HaroidApp.getPassword().length() > 0);
    }

    private void updateLatestUpdate() {
        long latestUpdate = HaroidApp.getLatestUpdate();
        TextView latestUpdateText = (TextView) findViewById(R.id.TextLatestUpdate);
        String latestUpdatePretext = getString(R.string.latestUpdate);
        if (latestUpdate == 0) {
            latestUpdateText.setText(latestUpdatePretext + " nooit.");
        } else {
            Date latestUpdateDate = new Date();
            latestUpdateDate.setTime(latestUpdate);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            latestUpdateText.setText(latestUpdatePretext + " " + sdf.format(latestUpdateDate));
        }
    }

    private void initControls() {
        Button tegoedButton = (Button) findViewById(R.id.ButtonTegoed);
        Button removeHistoryButton = (Button) findViewById(R.id.ButtonRemoveHistory);

        tegoedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startHaring();
            }
        });

        removeHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmRemoveHistory();
            }
        });
    }

    private void startHaring() {
        String emailAdres = HaroidApp.getEmailAdres();
        String wachtwoord = HaroidApp.getPassword();

        if (emailAdres != null && emailAdres.length() > 0 && wachtwoord != null && wachtwoord.length() > 0) {
            TextView tegoedView = (TextView) findViewById(R.id.TextTegoed);
            tegoedView.setText(getString(R.string.periodeTegoed) + " wordt opgehaald.");
            HaringTask haringTask = new HaringTask();
            haringTask.setTegoedConsumer(this);
            haringTask.execute(emailAdres, wachtwoord);
        }
    }

    private void tekenVerbruik(int maxTegoed, int maxPeriod, List<HistoryMonitor.UsagePoint> usageList) {
        Log.i(LOG_TAG, "tekenVerbruik");
        if (maxTegoed > 0 && maxPeriod > 0) {
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

    private void setProgressBars(HaroidApp.Stats stats) {
        Log.i(LOG_TAG, "berekenDuurTegoed");
        ProgressBar duurTegoedProgress = (ProgressBar) findViewById(R.id.PbarTijd);
        if (stats.startBalance == 0) {
            // Niet ingesteld.
            duurTegoedProgress.setProgress(0);
            return;
        }
        duurTegoedProgress.setMax(stats.maxPeriod);
        duurTegoedProgress.setProgress(stats.daysToGoInPeriod);
        TextView tijdView = (TextView) findViewById(R.id.TextTijd);
        if (stats.daysToGoInPeriod == 1) {
            tijdView.setText(getString(R.string.duurPeriode) + " nog " + stats.daysToGoInPeriod + " dag te gaan.");
        } else if (stats.daysToGoInPeriod < 1) {
            tijdView.setText(getString(R.string.duurPeriode) + " laatste dag.");
        } else {
            tijdView.setText(getString(R.string.duurPeriode) + " nog " + stats.daysToGoInPeriod + " dagen te gaan.");
        }
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

    @Override
    public void setTegoed(int tegoed) {
        this.app.setCurrentBalance(tegoed);
//        this.currentTegoed = tegoed;
        int maxTegoed = HaroidApp.getMaxTegoed();
        if (tegoed >= 0 && maxTegoed > 0) {
            updateLatestUpdate();
            setTegoedProgress(tegoed);
            List<HistoryMonitor.UsagePoint> usageList = this.app.getUsageList();
            MonthlyGraphView verbruikGraph = (MonthlyGraphView) findViewById(R.id.MonthlyGraphVerbruik);
            verbruikGraph.setUsage(usageList);
            verbruikGraph.invalidate();
            DailyGraphView dagelijksVerbruikGraph = (DailyGraphView) findViewById(R.id.DailyGraphVerbruik);
            dagelijksVerbruikGraph.setUsage(usageList);
            dagelijksVerbruikGraph.invalidate();
        }
    }

    @Override
    public void setProblem(String problem) {
        TextView tegoedView = (TextView) findViewById(R.id.TextTegoed);
        tegoedView.setText(problem);
    }

    private void setTegoedProgress(int tegoed) {
        Log.i(LOG_TAG, "setTegoedProgress: " + tegoed);
        int startTegoed = HaroidApp.getStartTegoed();
        int maxTegoed = HaroidApp.getMaxTegoed();
        if (tegoed >= 0 && maxTegoed > 0) {
            TextView tegoedView = (TextView) findViewById(R.id.TextTegoed);
            tegoedView.setText(getString(R.string.periodeTegoed) + " " + tegoed + " eenheden.");
            ProgressBar tegoedProgress = (ProgressBar) findViewById(R.id.PbarTegoed);
            tegoedProgress.setMax(Math.max(tegoed, maxTegoed));
            tegoedProgress.setProgress(tegoed);
        }
        int tegoedGisteren = this.app.getBalanceYesterday();
        int verbruikVandaag = tegoedGisteren - tegoed;
        if (tegoedGisteren > -1 && verbruikVandaag >= 0) {
            int maxPeriod = berekenMaxPeriod(startTegoed);
            int procentueelVerbruik = (100 * verbruikVandaag * maxPeriod) / maxTegoed;
            TextView dagVerbruikView = (TextView) findViewById(R.id.TextDagVerbruik);
            dagVerbruikView.setText(getString(R.string.dagVerbruik) + " " + verbruikVandaag + " eenheden. (" + procentueelVerbruik + "% van de dag)");
            dagVerbruikView.setVisibility(View.VISIBLE);
        }
    }

    private void confirmRemoveHistory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_menu_delete);
        builder.setTitle(getString(R.string.removeHistory));
        builder.setMessage(getString(R.string.areYouSure))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.yesButtonText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeHistory();
                    }
                })
                .setNegativeButton(getString(R.string.noButtonText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void removeHistory() {
        this.app.resetHistory();
//        this.currentTegoed = 0;
        int maxTegoed = HaroidApp.getMaxTegoed();
        TextView tegoedView = (TextView) findViewById(R.id.TextTegoed);
        tegoedView.setText(getString(R.string.periodeTegoed));
        ProgressBar tegoedProgress = (ProgressBar) findViewById(R.id.PbarTegoed);
        tegoedProgress.setMax(maxTegoed);
        tegoedProgress.setProgress(0);
        MonthlyGraphView verbruikGraph = (MonthlyGraphView) findViewById(R.id.MonthlyGraphVerbruik);
        verbruikGraph.setUsage(Collections.<HistoryMonitor.UsagePoint>emptyList());
        verbruikGraph.invalidate();
        DailyGraphView dagelijksVerbruikGraph = (DailyGraphView) findViewById(R.id.DailyGraphVerbruik);
        dagelijksVerbruikGraph.setUsage(Collections.<HistoryMonitor.UsagePoint>emptyList());
        dagelijksVerbruikGraph.invalidate();
    }
}

