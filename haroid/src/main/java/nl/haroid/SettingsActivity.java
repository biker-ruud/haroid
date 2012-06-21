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

    public static final String USERNAME_PREF_KEY = "pref_username";
    public static final String TEGOED_PREF_KEY = "pref_max_tegoed";
    public static final String STARTDAG_PREF_KEY = "pref_start_tegoed";

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
        pretextMap.put(USERNAME_PREF_KEY, getString(R.string.emailadres_setting_pretext));
        pretextMap.put(TEGOED_PREF_KEY, getString(R.string.tegoed_setting_pretext));
        pretextMap.put(STARTDAG_PREF_KEY, getString(R.string.startdag_setting_pretext));

        //Iterate over all preferance objects and set initial values
        for (Map.Entry<String, String> entry : pretextMap.entrySet()){
            changePreferanceSummary(entry.getKey(), getPreferenceScreen().getSharedPreferences());
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
        changePreferanceSummary(s, sharedPreferences);
    }

    private void changePreferanceSummary(String s, SharedPreferences sharedPreferences) {
        Preference preference = getPreferenceScreen().findPreference(s);
        preference.setSummary(pretextMap.get(s) + " " + sharedPreferences.getString(s, ""));
    }

}
