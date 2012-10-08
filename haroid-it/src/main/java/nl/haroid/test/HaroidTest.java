package nl.haroid.test;

import android.test.ActivityInstrumentationTestCase2;
import nl.haroid.app.Haroid;

public class HaroidTest extends ActivityInstrumentationTestCase2<Haroid> {

    public HaroidTest() {
        super(Haroid.class);
    }

    public void testActivity() {
        Haroid activity = getActivity();
        assertNotNull(activity);
    }
}

