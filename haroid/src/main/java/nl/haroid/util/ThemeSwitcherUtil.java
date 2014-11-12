package nl.haroid.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import nl.haroid.Haroid;
import nl.haroid.HaroidApp;
import nl.haroid.R;
import nl.haroid.common.Theme;

import java.util.ArrayList;
import java.util.List;

/**
 * This class has all the Theme Switcher logic.
 *
 * @author Ruud de Jong
 */
public final class ThemeSwitcherUtil {

    private static final String LOG_TAG = "ThemeSwitcherUtil";

    private ThemeSwitcherUtil() {
        // Utility class constructor
    }

    public static int getCustomThemeStyle() {
        int darkTheme = android.R.style.Theme_Black;
        int lightTheme = android.R.style.Theme_Light;
        // Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            darkTheme = android.R.style.Theme_Material;
            lightTheme = android.R.style.Theme_Material_Light;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            darkTheme = android.R.style.Theme_Holo;
            lightTheme = android.R.style.Theme_Holo_Light;
        }
        Theme chosenTheme = getChosenTheme();
        if (chosenTheme == Theme.LIGHT) {
            return lightTheme;
        } else {
            return darkTheme;
        }
    }

    public static void setCustomTheme(Activity activity) {
        activity.setTheme(ThemeSwitcherUtil.getCustomThemeStyle());
    }

    public static void setThemedRefreshIcon(View refreshActionView) {
        ImageView refreshImageView = (ImageView) refreshActionView;
        if (getChosenTheme() == Theme.LIGHT) {
            refreshImageView.setImageResource(R.drawable.holo_light_ic_action_refresh);
        } else {
            refreshImageView.setImageResource(R.drawable.holo_dark_ic_action_refresh);
        }
    }

    public static void setupThemeListPreference(final PreferenceActivity activity) {
        ListPreference listPreference = (ListPreference) activity.findPreference(HaroidApp.PREF_KEY_THEME);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setIcon(R.drawable.icon);
                builder.setTitle(activity.getString(R.string.restartRequired));
                builder.setMessage(activity.getString(R.string.themeChangeRequiresRestart))
                        .setCancelable(true)
                        .setPositiveButton(activity.getString(R.string.restart), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.i(LOG_TAG, "User clicked restart button.");
                                listPreference.setValue(selectedTheme);
                                Log.i(LOG_TAG, "User is sure to restart app.");
                                Intent intent = new Intent(activity.getApplicationContext(), Haroid.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("EXIT", true);
                                activity.startActivity(intent);
                            }
                        })
                        .setNegativeButton(activity.getString(R.string.undoChange), new DialogInterface.OnClickListener() {
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

    public static Theme getChosenTheme() {
        Theme defaultTheme = Theme.DARK;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            defaultTheme = Theme.LIGHT;
        }
        return Theme.valueOf(HaroidApp.getTheme(defaultTheme));
    }

}
