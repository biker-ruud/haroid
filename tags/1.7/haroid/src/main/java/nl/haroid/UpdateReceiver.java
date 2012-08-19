package nl.haroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * @author Ruud de Jong
 */
public final class UpdateReceiver extends BroadcastReceiver implements TegoedConsumer {

    private static final String LOG_TAG = "UpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "Intent received.");

        boolean alarmManagerSet = intent.getBooleanExtra(HaroidApp.ALARM_MANAGER_SET, false);
        Log.i(LOG_TAG, "Auto update enabled: " + HaroidApp.isAutoUpdateEnabled());
        Log.i(LOG_TAG, "Lastest update: " + HaroidApp.getLatestUpdate());
        Log.i(LOG_TAG, "Update channel: " + HaroidApp.getUpdateChannel());
        Log.i(LOG_TAG, "Wifi update interval: " + HaroidApp.getWifiUpdateInterval());
        Log.i(LOG_TAG, "Mobile update interval: " + HaroidApp.getMobileUpdateInterval());

        boolean autoUpdateEnabled = HaroidApp.isAutoUpdateEnabled();
        if (autoUpdateEnabled) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo.isConnected()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    Log.i(LOG_TAG, "WIFI connected.");
                    updateIfNecessary(HaroidApp.getWifiUpdateInterval());
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    Log.i(LOG_TAG, "Mobile connected.");
                    String updateChannel = HaroidApp.getUpdateChannel();
                    if (updateChannel.contains("mobile")) {
                        updateIfNecessary(HaroidApp.getMobileUpdateInterval());
                    } else {
                        Log.i(LOG_TAG, "User does not which to update through mobile.");
                    }
                } else {
                    Log.i(LOG_TAG, "Unsupported network type connected.");
                }
            } else {
                Log.i(LOG_TAG, "Too bad, no network available.");
            }
        } else {
            Log.i(LOG_TAG, "Ignoring this intent");
        }
        if (!alarmManagerSet) {
            Log.i(LOG_TAG, "Boot event detected. Need to set the alarm manager.");
            AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper(context);
            alarmManagerHelper.setOrResetAlarmManager(autoUpdateEnabled);
        }
    }

    @Override
    public void setTegoed(int balance) {
        Log.i(LOG_TAG, "Tegoed: " + balance);
        HaroidApp.getInstance().setCurrentBalance(balance);
    }

    @Override
    public void setProblem(String problem) {
        Log.i(LOG_TAG, "Problem: " + problem);
    }

    private void updateIfNecessary(int updateIntervalInHours) {
        Log.i(LOG_TAG, "Lastest update: " + HaroidApp.getLatestUpdate());
        Date now = new Date();
        long mustUpdateAfter = HaroidApp.getLatestUpdate() + (3600000l * ((long)updateIntervalInHours));
        Log.i(LOG_TAG, "Now:               " + now.getTime());
        Log.i(LOG_TAG, "Must update after: " + mustUpdateAfter);
        if (now.getTime() > mustUpdateAfter) {
            Log.i(LOG_TAG, "Time to update");
            String emailAdres = HaroidApp.getEmailAdres();
            String wachtwoord = HaroidApp.getPassword();
            HaringTask haringTask = new HaringTask();
            haringTask.setTegoedConsumer(this);
            haringTask.execute(emailAdres, wachtwoord);
        }
    }
}
