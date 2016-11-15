package marmu.com.gpstracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.Date;

public class CoordinatesReceiver extends BroadcastReceiver {

    String SOAP_ACTION = "http://tempuri.org/SetEnableTracking";
    String METHOD_NAME = "SetEnableTracking";
    String NAMESPACE = "http://tempuri.org/";
    String URL = "http://198.12.153.30/SETrackMobility/SETrack.asmx";

    private String TAG = "Response";
    private Context ctx;
    private String imei, latitude, longitude;
    private int batteryPercentage;


    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context;
        imei = intent.getExtras().get("IMEI").toString();
        latitude = intent.getExtras().get("Latitude").toString();
        longitude = intent.getExtras().get("Longitude").toString();
        batteryPercentage = (int) batteryLevel();
        //batteryPercentage = (int) getBatteryPercentage();

        DatabaseManager databaseManager = new DatabaseManager(ctx);

        //Read LocalDB
        Cursor cursor = databaseManager.getAllCoordinates();
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                AsyncCallWS task = new AsyncCallWS(cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        Integer.parseInt(cursor.getString(4)),
                        ctx,
                        Integer.parseInt(cursor.getString(0)));
                task.execute();
            }
        } finally {
            cursor.close();
        }

        AsyncCallWS task = new AsyncCallWS(imei, latitude, longitude, batteryPercentage, ctx, -1);
        task.execute();
    }


    //battery level
    public float batteryLevel() {
        Intent batteryIntent = ctx.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        assert batteryIntent != null;
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if (level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float) level / (float) scale) * 100.0f;
    }

    //SOAP method
    public void calculate(String imei, String latitude, String longitude,
                          int batteryPercentage, Context mctx) {
        try {
            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);

            Request.addProperty("IMEANo", imei);
            Request.addProperty("Latitude", Double.parseDouble(latitude));
            Request.addProperty("Longitude", Double.parseDouble(longitude));
            Request.addProperty("BatteryPercentage", batteryPercentage);

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            soapEnvelope.dotNet = true;
            soapEnvelope.encodingStyle = SoapSerializationEnvelope.XSD;
            soapEnvelope.setOutputSoapObject(Request);

            MarshalDouble md = new MarshalDouble();
            md.register(soapEnvelope);

            HttpTransportSE transport = new HttpTransportSE(URL);

            Log.e("Request: ", "Requesting time " + new Date());
            Log.i("IMENO", imei);
            Log.i("Longitude", longitude);
            Log.i("Latitude", latitude);
            Log.i("BatteryPercentage", String.valueOf(batteryPercentage));
            transport.call(SOAP_ACTION, soapEnvelope);

            SoapPrimitive resultString = (SoapPrimitive) soapEnvelope.getResponse();
            Log.e(TAG, "Success: " + String.valueOf(resultString));

        } catch (Exception ex) {
            Log.e(TAG, "Error: " + ex.getMessage());
            DatabaseManager databaseManager = new DatabaseManager(mctx);
            databaseManager.addCoordinates(imei, latitude, longitude, String.valueOf(batteryPercentage));
        }

    }

    //RecallSOAP method
    public void calculate(String imei, String latitude, String longitude,
                          int batteryPercentage, Context mctx, int count) {
        try {
            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);

            Request.addProperty("IMEANo", imei);
            Request.addProperty("Latitude", Double.parseDouble(latitude));
            Request.addProperty("Longitude", Double.parseDouble(longitude));
            Request.addProperty("BatteryPercentage", batteryPercentage);

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            soapEnvelope.dotNet = true;
            soapEnvelope.encodingStyle = SoapSerializationEnvelope.XSD;
            soapEnvelope.setOutputSoapObject(Request);

            MarshalDouble md = new MarshalDouble();
            md.register(soapEnvelope);

            HttpTransportSE transport = new HttpTransportSE(URL);

            Log.e("Recall Request ", "Requesting time " + new Date());
            Log.i("IMENO", imei);
            Log.i("Longitude", longitude);
            Log.i("Latitude", latitude);
            Log.i("BatteryPercentage", String.valueOf(batteryPercentage));
            transport.call(SOAP_ACTION, soapEnvelope);

            SoapPrimitive resultString = (SoapPrimitive) soapEnvelope.getResponse();
            Log.e(TAG, "Success: " + String.valueOf(resultString));

            //Remove from Local
            DatabaseManager databaseManager = new DatabaseManager(mctx);
            databaseManager.deleteContact(count);

        } catch (Exception ex) {
            Log.e(TAG, "Recall Error: " + ex.getMessage());
            DatabaseManager databaseManager = new DatabaseManager(mctx);
            databaseManager.updateContact(count, imei, latitude, longitude, String.valueOf(batteryPercentage));
        }

    }

    public class AsyncCallWS extends AsyncTask<Void, Void, Void> {
        private String imei, latitude, longitude;
        private Context ctx;
        private int batteryPercentage;
        private int chooseMethod = -1;


        public AsyncCallWS(String imei, String latitude, String longitude,
                           int batteryPercentage, Context ctx, int chooseMethod) {
            this.imei = imei;
            this.latitude = latitude;
            this.longitude = longitude;
            this.batteryPercentage = batteryPercentage;
            this.ctx = ctx;
            this.chooseMethod = chooseMethod;
        }

        @Override
        protected void onPreExecute() {
            //Log.i(TAG, "onPreExecute");
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (chooseMethod == -1)
                new CoordinatesReceiver().calculate(imei, latitude, longitude, batteryPercentage, ctx);
            else
                new CoordinatesReceiver().calculate(imei, latitude, longitude, batteryPercentage, ctx, chooseMethod);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //Log.i(TAG, "onPostExecute");
        }
    }
}