package nl.haroid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

/**
 * @author Ruud de Jong
 */
final class AlarmManagerHelper {

    private static final String LOG_TAG = "AlarmManagerHelper";
    private static final int requestCode = 57205;
    private static final long ONE_MINUTE_IN_MILLIS = 60000l;

    private Context context;

    AlarmManagerHelper(Context context) {
        this.context = context;
    }

    void setOrResetAlarmManager(boolean autoUpdateEnabled) {
        AlarmManager alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this.context, UpdateReceiver.class);
        intent.putExtra(HaroidApp.ALARM_MANAGER_SET, true);
        PendingIntent sender = PendingIntent.getBroadcast(this.context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (autoUpdateEnabled) {
            Log.i(LOG_TAG, "Setting alarm manager.");
            Date now = new Date();
            long oneMinuteFromNow = now.getTime() + ONE_MINUTE_IN_MILLIS;
            alarmManager.setInexactRepeating(AlarmManager.RTC, oneMinuteFromNow, AlarmManager.INTERVAL_HOUR, sender);
//            alarmManager.setInexactRepeating(AlarmManager.RTC, oneMinuteFromNow, ONE_MINUTE_IN_MILLIS, sender);
        } else {
            Log.i(LOG_TAG, "Resetting alarm manager.");
            alarmManager.cancel(sender);
        }
    }
}
