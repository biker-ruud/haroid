package nl.haroid;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author Ruud de Jong
 */
public final class InstellingenActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
