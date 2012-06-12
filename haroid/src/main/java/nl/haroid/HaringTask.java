package nl.haroid;

import android.os.AsyncTask;

/**
 * @author Ruud de Jong
 */
public final class HaringTask extends AsyncTask<String, Void, String> {

    private TegoedConsumer tegoedConsumer;

    public void setTegoedConsumer(TegoedConsumer tegoedConsumer) {
        this.tegoedConsumer = tegoedConsumer;
    }

    @Override
    protected String doInBackground(String... strings) {
        Haring haring = new Haring();
        return haring.start(strings[0], strings[1]);
    }

    @Override
    protected void onPostExecute(String tegoedString) {
        try {
            int tegoed = Integer.parseInt(tegoedString);
            this.tegoedConsumer.setTegoed(tegoed);
        } catch (NumberFormatException e) {
            // Geen getal
        }
    }
}