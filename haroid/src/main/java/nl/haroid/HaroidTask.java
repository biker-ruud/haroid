package nl.haroid;

import android.os.AsyncTask;
import android.util.Log;
import nl.haroid.common.Provider;
import nl.haroid.common.Utils;
import nl.haroid.webclient.Haring;
import nl.haroid.webclient.HaringFactory;
import nl.haroid.webclient.HaringHnImpl;
import nl.haroid.webclient.HaringTmobileImpl;

import java.text.ParseException;

/**
 * @author Ruud de Jong
 */
public final class HaroidTask extends AsyncTask<String, Void, String> {

    private TegoedConsumer tegoedConsumer;

    private Provider provider;

    public void setTegoedConsumer(TegoedConsumer tegoedConsumer) {
        this.tegoedConsumer = tegoedConsumer;
    }

    public HaroidTask(Provider provider) {
        this.provider = provider;
    }

    @Override
    protected String doInBackground(String... strings) {
        Haring haring = HaringFactory.getInstance().getHaring(provider);
        return haring.start(strings[0], strings[1]);
    }

    @Override
    protected void onPostExecute(String tegoedString) {
        try {
            int tegoed = Utils.parseGetal(tegoedString);
            this.tegoedConsumer.setTegoed(tegoed);
        } catch (ParseException e) {
            // Geen getal
            this.tegoedConsumer.setProblem(tegoedString);
        }
    }
}