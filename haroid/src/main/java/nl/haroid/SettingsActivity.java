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
        HaroidApp.getInstance().setCustomTheme(this);
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
        ListPreference listPreference = (ListPreference) findPreference(HaroidApp.PREF_KEY_THEME);
        List<String> themes = new ArrayList<String>();
        List<String> themeDisplayNames = new ArrayList<String>();
        for (Theme theme : Theme.values()){
            themes.add(theme.name());
            themeDisplayNames.add(theme.getDisplayName());
        }
        CharSequence[] themeList = themes.toArray(new CharSequence[themes.toArray().length]);
        CharSequence[] themeDisplayList = themeDisplayNames.toArray(new CharSequence[themeDisplayNames.toArray().length]);
        //The human-readable array to present as a list.
        listPreference.setEntries(themeDisplayList);
        //The array to find the value to save for a preference when an entry from entries is selected.
        listPreference.setEntryValues(themeList);
        if (listPreference.getValue() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                listPreference.setValue(Theme.LIGHT.name());
            } else {
                listPreference.setValue(Theme.DARK.name());
            }
        }

        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                final ListPreference listPreference = (ListPreference) preference;
                final String selectedTheme = (String) newValue;
                Log.i(LOG_TAG, "new value: " + newValue);
                Log.i(LOG_TAG, "new value class: " + newValue.getClass());
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setIcon(R.drawable.icon);
                builder.setTitle(getString(R.string.restartRequired));
                builder.setMessage(getString(R.string.themeChangeRequiresRestart))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.restart), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.i(LOG_TAG, "User clicked restart button.");
                                listPreference.setValue(selectedTheme);
                                Log.i(LOG_TAG, "User is sure to restart app.");
                                Intent intent = new Intent(getApplicationContext(), Haroid.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("EXIT", true);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(getString(R.string.undoChange), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.i(LOG_TAG, "User clicked undo button.");
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return false;
            }
        });
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
