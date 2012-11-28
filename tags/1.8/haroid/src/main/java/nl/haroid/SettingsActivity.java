package nl.haroid;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import nl.haroid.common.Provider;
import nl.haroid.common.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruud de Jong
 * @author Xilv3r
 */
public final class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "SettingsActivity";

    private Map<String, String> pretextMap = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        addProviderListPreferanceDynamicly();
    }

    private void addProviderListPreferanceDynamicly() {
        ListPreference listPreference = (ListPreference)findPreference(HaroidApp.PREF_KEY_PROVIDER);
        List<String> providers = new ArrayList<String>();
        for (Provider provider : Provider.values()){
            // TODO (Xilv3r): use cleaner string variable instead of enum name
            providers.add(provider.name());
        }
        CharSequence[] providerlist = providers.toArray(new CharSequence[providers.toArray().length]);
        listPreference.setEntries(providerlist);
        listPreference.setEntryValues(providerlist);
        if (listPreference.getValue() == null) {
            listPreference.setValue(Provider.HOLLANDS_NIEUWE.name());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Load map
        pretextMap.put(HaroidApp.PREF_KEY_PROVIDER, getString(R.string.kies_provider_setting_pretext));
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
        toggleAutoUpdatePreferences();
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
        if (HaroidApp.PREF_KEY_ENABLE_AUTO_UPDATE.equals(s)) {
            Log.i(LOG_TAG, "Processing auto update checkbox.");
            boolean autoUpdateEnabled = sharedPreferences.getBoolean(s, false);
            getPreferenceScreen().findPreference(HaroidApp.PREF_KEY_UPDATE_CHANNEL).setEnabled(autoUpdateEnabled);
            getPreferenceScreen().findPreference(HaroidApp.PREF_KEY_WIFI_UPDATE_INTERVAL).setEnabled(autoUpdateEnabled);
            getPreferenceScreen().findPreference(HaroidApp.PREF_KEY_MOBILE_UPDATE_INTERVAL).setEnabled(autoUpdateEnabled);
            AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper(this);
            alarmManagerHelper.setOrResetAlarmManager(autoUpdateEnabled);
        }
    }

    private void toggleAutoUpdatePreferences() {
        CheckBoxPreference autoUpdateCheckbox = (CheckBoxPreference) getPreferenceScreen().findPreference(HaroidApp.PREF_KEY_ENABLE_AUTO_UPDATE);
        boolean autoUpdateEnabled = autoUpdateCheckbox.isChecked();
        getPreferenceScreen().findPreference(HaroidApp.PREF_KEY_UPDATE_CHANNEL).setEnabled(autoUpdateEnabled);
        getPreferenceScreen().findPreference(HaroidApp.PREF_KEY_WIFI_UPDATE_INTERVAL).setEnabled(autoUpdateEnabled);
        getPreferenceScreen().findPreference(HaroidApp.PREF_KEY_MOBILE_UPDATE_INTERVAL).setEnabled(autoUpdateEnabled);
    }

    private void changePreferenceSummary(String s, SharedPreferences sharedPreferences) {
        Preference preference = getPreferenceScreen().findPreference(s);
        String preText = pretextMap.get(s);
        if (preText != null) {
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                if (listPreference.getEntry() == null) {
                    preference.setSummary(pretextMap.get(s));
                } else {
                    preference.setSummary(pretextMap.get(s) + " " + listPreference.getEntry());
                }
            } else {
                preference.setSummary(pretextMap.get(s) + " " + sharedPreferences.getString(s, ""));
            }
        }
    }
}