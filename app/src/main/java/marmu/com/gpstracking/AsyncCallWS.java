package marmu.com.gpstracking;

import android.os.AsyncTask;
import android.util.Log;


public class AsyncCallWS extends AsyncTask<Void, Void, Void> {
    private String TAG = "Response";

    @Override
    protected void onPreExecute() {
        Log.i(TAG, "onPreExecute");
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.i(TAG, "doInBackground");
        new CoordinatesReceiver().calculate();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        Log.i(TAG, "onPostExecute");
    }

}
