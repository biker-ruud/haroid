package nl.haroid;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * @author Ruud de Jong
 */
public final class HaroidApp extends Application {

    public static final String PREF_KEY_MAX_TEGOED = "pref_max_tegoed";
    public static final String PREF_KEY_START_TEGOED = "pref_start_tegoed";
    public static final String PREF_KEY_USERNAME = "pref_username";
    public static final String PREF_KEY_PASSWORD = "pref_password";
    private static final String LOG_TAG = "HaroidApp";


    private static SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "OnCreate()");
    }

    /**
     * Set by main activity. Still unknown how to let the application fetch it's own preferences.
     * @param sharedPreferences1
     */
    public static void setPreferences(SharedPreferences sharedPreferences1) {
        sharedPreferences = sharedPreferences1;
    }

    public static String getEmailAdres() {
        return sharedPreferences.getString(PREF_KEY_USERNAME, "");
    }

    public static String getPassword() {
        return sharedPreferences.getString(PREF_KEY_PASSWORD, "");
    }

    public static int getMaxTegoed() {
        return Integer.parseInt(sharedPreferences.getString(PREF_KEY_MAX_TEGOED, "0"));
    }

    public static int getStartTegoed() {
        return Integer.parseInt(sharedPreferences.getString(PREF_KEY_START_TEGOED, "0"));
    }
}
