package nl.haroid;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import nl.haroid.common.Provider;
import nl.haroid.common.Theme;
import nl.haroid.util.ThemeSwitcherUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Ruud de Jong
 * @author Xilv3r
 */
public final class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "SettingsActivity";

    private Map<String, String> pretextMap = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeSwitcherUtil.setCustomTheme(this);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        addProviderListPreferanceDynamically();
        addThemeListPreferanceDynamically();
        createPretextMap();

        // Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void addProviderListPreferanceDynamically() {
        ListPreference listPreference = (ListPreference) findPreference(HaroidApp.PREF_KEY_PROVIDER);
        List<String> providers = new ArrayList<String>();
        List<String> providerDisplayNames = new ArrayList<String>();
        for (Provider provider : Provider.values()){
            providers.add(provider.name());
            providerDisplayNames.add(provider.getDisplayName());
        }
        CharSequence[] providerlist = providers.toArray(new CharSequence[providers.toArray().length]);
        CharSequence[] providerDisplayList = providerDisplayNames.toArray(new CharSequence[providerDisplayNames.toArray().length]);
        //The human-readable array to present as a list.
        listPreference.setEntries(providerDisplayList);
        //The array to find the value to save for a preference when an entry from entries is selected.
        listPreference.setEntryValues(providerlist);
        if (listPreference.getValue() == null) {
            listPreference.setValue(Provider.HOLLANDS_NIEUWE.name());
        }
    }

    private void addThemeListPreferanceDynamically() {
        ThemeSwitcherUtil.setupThemeListPreference(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        if (HaroidApp.PREF_KEY_PROVIDER.equals(s)) {
            changeProvider();
        }
        changePreferenceSummary(s, sharedPreferences);
        if (HaroidApp.PREF_KEY_ENABLE_AUTO_UPDATE.equals(s)) {
            Log.i(LOG_TAG, "Processing auto update checkbox.");
            boolean autoUpdateEnabled = sharedPreferences.getBoolean(s, false);
            findPreference(HaroidApp.PREF_KEY_UPDATE_CHANNEL).setEnabled(autoUpdateEnabled);
            findPreference(HaroidApp.PREF_KEY_WIFI_UPDATE_INTERVAL).setEnabled(autoUpdateEnabled);
            findPreference(HaroidApp.PREF_KEY_MOBILE_UPDATE_INTERVAL).setEnabled(autoUpdateEnabled);
            AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper(this);
            alarmManagerHelper.setOrResetAlarmManager(autoUpdateEnabled);
        }
    }

    private void toggleAutoUpdatePreferences() {
        CheckBoxPreference autoUpdateCheckbox = (CheckBoxPreference) getPreferenceScreen().findPreference(HaroidApp.PREF_KEY_ENABLE_AUTO_UPDATE);
        boolean autoUpdateEnabled = autoUpdateCheckbox.isChecked();
        findPreference(HaroidApp.PREF_KEY_UPDATE_CHANNEL).setEnabled(autoUpdateEnabled);
        findPreference(HaroidApp.PREF_KEY_WIFI_UPDATE_INTERVAL).setEnabled(autoUpdateEnabled);
        findPreference(HaroidApp.PREF_KEY_MOBILE_UPDATE_INTERVAL).setEnabled(autoUpdateEnabled);
    }

    private void changePreferenceSummary(String s, SharedPreferences sharedPreferences) {
        Preference preference = findPreference(s);
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

    private void changeProvider() {
        EditTextPreference usernamePreference = (EditTextPreference) findPreference(HaroidApp.PREF_KEY_USERNAME);
        if (Provider.T_MOBILE == Provider.valueOf(HaroidApp.getProvider())) {
            pretextMap.put(HaroidApp.PREF_KEY_USERNAME, getString(R.string.gebruikersnaam_setting_pretext));
            usernamePreference.setTitle(getString(R.string.gebruikersnaam));
        } else {
            pretextMap.put(HaroidApp.PREF_KEY_USERNAME, getString(R.string.emailadres_setting_pretext));
            usernamePreference.setTitle(getString(R.string.emailadres));
        }
        changePreferenceSummary(HaroidApp.PREF_KEY_USERNAME, getPreferenceScreen().getSharedPreferences());
    }

    private void createPretextMap() {
        //Load map
        pretextMap.put(HaroidApp.PREF_KEY_PROVIDER, getString(R.string.kies_provider_setting_pretext));
        pretextMap.put(HaroidApp.PREF_KEY_MAX_TEGOED, getString(R.string.tegoed_setting_pretext));
        pretextMap.put(HaroidApp.PREF_KEY_START_TEGOED, getString(R.string.startdag_setting_pretext));
        pretextMap.put(HaroidApp.PREF_KEY_UPDATE_CHANNEL, getString(R.string.update_channel_pretext));
        pretextMap.put(HaroidApp.PREF_KEY_WIFI_UPDATE_INTERVAL, getString(R.string.update_interval_pretext));
        pretextMap.put(HaroidApp.PREF_KEY_MOBILE_UPDATE_INTERVAL, getString(R.string.update_interval_pretext));
        pretextMap.put(HaroidApp.PREF_KEY_THEME, getString(R.string.theme_pretext));
        changeProvider();
    }
}
