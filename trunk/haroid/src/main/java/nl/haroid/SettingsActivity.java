package nl.haroid;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruud de Jong
 * @author Xilv3r
 */
public final class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private Map<String, String> pretextMap = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Load map
        pretextMap.put(HaroidApp.PREF_KEY_USERNAME, getString(R.string.emailadres_setting_pretext));
        pretextMap.put(HaroidApp.PREF_KEY_MAX_TEGOED, getString(R.string.tegoed_setting_pretext));
        pretextMap.put(HaroidApp.PREF_KEY_START_TEGOED, getString(R.string.startdag_setting_pretext));
        pretextMap.put(HaroidApp.PREF_KEY_UPDATE_CHANNEL, getString(R.string.update_channel_pretext));
        pretextMap.put(HaroidApp.PREF_KEY_WIFI_UPDATE_INTERVAL, getString(R.string.update_interval_pretext));
        pretextMap.put(HaroidApp.PREF_KEY_MOBILE_UPDATE_INTERVAL, getString(R.string.update_interval_pretext));

        //Iterate over all preferance objects and set initial values
        for (Map.Entry<String, String> entry : pretextMap.entrySet()){
            changePreferenceSummary(entry.getKey(), getPreferenceScreen().getSharedPreferences());
        }

        //Reregister Listener due to application being resumed
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Unregister Listener due to application being paused
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        changePreferenceSummary(s, sharedPreferences);
    }

    private void changePreferenceSummary(String s, SharedPreferences sharedPreferences) {
        Preference preference = getPreferenceScreen().findPreference(s);
        String preText = pretextMap.get(s);
        if (preText != null) {
            preference.setSummary(pretextMap.get(s) + " " + sharedPreferences.getString(s, ""));
        }
        if (HaroidApp.PREF_KEY_WIFI_UPDATE_INTERVAL.equals(s) || HaroidApp.PREF_KEY_MOBILE_UPDATE_INTERVAL.equals(s)) {
            preference.setSummary(preference.getSummary() + " " + getString(R.string.update_interval_posttext));
        }
        if (HaroidApp.PREF_KEY_ENABLE_AUTO_UPDATE.equals(s)) {
            boolean autoUpdateEnabled = sharedPreferences.getBoolean(s, false);
            getPreferenceScreen().findPreference(HaroidApp.PREF_KEY_UPDATE_CHANNEL).setEnabled(autoUpdateEnabled);
            getPreferenceScreen().findPreference(HaroidApp.PREF_KEY_WIFI_UPDATE_INTERVAL).setEnabled(autoUpdateEnabled);
            getPreferenceScreen().findPreference(HaroidApp.PREF_KEY_MOBILE_UPDATE_INTERVAL).setEnabled(autoUpdateEnabled);
        }
    }

}
