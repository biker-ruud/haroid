package nl.haroid;

import android.os.AsyncTask;
import nl.haroid.common.Provider;
import nl.haroid.webclient.HaringHnImpl;
import nl.haroid.webclient.HaringTmobileImpl;

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
            switch (provider){
                case T_MOBILE:
                    HaringTmobileImpl tmobile = new HaringTmobileImpl();
                    return tmobile.start(strings[0], strings[1]);
                default:
                    HaringHnImpl haring = new HaringHnImpl();
                    return haring.start(strings[0], strings[1]);
            }
    }

    @Override
    protected void onPostExecute(String tegoedString) {
        try {
            int tegoed = Integer.parseInt(tegoedString);
            this.tegoedConsumer.setTegoed(tegoed);
        } catch (NumberFormatException e) {
            // Geen getal
            this.tegoedConsumer.setProblem(tegoedString);
        }
    }
}