package nl.haroid;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

/**
 * @author Ruud de Jong
 * @author Xilv3r
 */
public final class InstellingenActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    public static final String USERNAME_PREF_KEY = "pref_username";
    public static final String TEGOED_PREF_KEY = "pref_max_tegoed";
    public static final String STARTDAG_PREF_KEY = "pref_start_tegoed";

    private EditTextPreference usernameText;
    private EditTextPreference tegoedText;
    private ListPreference startdagList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        //Get instance of specific preferance
        usernameText = (EditTextPreference)getPreferenceScreen().findPreference(USERNAME_PREF_KEY);
        tegoedText = (EditTextPreference)getPreferenceScreen().findPreference(TEGOED_PREF_KEY);
        startdagList = (ListPreference)getPreferenceScreen().findPreference(STARTDAG_PREF_KEY);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Set initial values
        usernameText.setSummary(getPreferenceScreen().getSharedPreferences().getString(USERNAME_PREF_KEY, getString(R.string.change_emailadres_sum)));
        tegoedText.setSummary(getString(R.string.tegoed_setting_pretext) + " " + getPreferenceScreen().getSharedPreferences().getString(TEGOED_PREF_KEY, "0"));
        startdagList.setSummary(getString(R.string.startdag_setting_pretext) + " " + getPreferenceScreen().getSharedPreferences().getString(STARTDAG_PREF_KEY, "0"));
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
        //todo (Dave) get rid of if else flow
        if (s.equals(USERNAME_PREF_KEY)){
            usernameText.setSummary(getString(R.string.emailadres_setting_pretext) + " " + sharedPreferences.getString(s, ""));
        } else if (s.equals(TEGOED_PREF_KEY)){
            tegoedText.setSummary(getString(R.string.tegoed_setting_pretext) + " " + sharedPreferences.getString(s, ""));
        } else if (s.endsWith(STARTDAG_PREF_KEY)){
            startdagList.setSummary(getString(R.string.startdag_setting_pretext) + " " + sharedPreferences.getString(s, ""));
        }
    }
}
