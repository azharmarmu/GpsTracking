package marmu.com.gpstracking;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;


public class MainActivity extends AppCompatActivity {

    private String TAG = "Response";


    private BroadcastReceiver broadcastReceiver;
    private SoapPrimitive resultString;

    private String imei, latitude, longitude;
    private int batteryPercentage;

    @Override
    protected void onResume() {
        super.onResume();

        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, final Intent intent) {
                    imei = intent.getExtras().get("IMEI").toString();
                    latitude = intent.getExtras().get("Latitude").toString();
                    longitude = intent.getExtras().get("Longitude").toString();
                    batteryPercentage = (int) batteryLevel();

                    AsyncCallWS task = new AsyncCallWS();
                    task.execute();


                    Log.i("Longitude", longitude);
                    Log.i("Latitude", latitude);
                    Log.i("BatteryPercentage", String.valueOf(batteryPercentage));
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);


        if (!runtime_permissions()) {
            enable_gps();
        }
    }

    //battery level
    public float batteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if (level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float) level / (float) scale) * 100.0f;
    }

    //SOAP method
    public void calculate() {
        String SOAP_ACTION = "http://tempuri.org/SetEnableTracking";
        String METHOD_NAME = "SetEnableTracking";
        String NAMESPACE = "http://tempuri.org/";
        String URL = "http://198.12.153.30/SETrackMobility/SETrack.asmx";
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

            transport.call(SOAP_ACTION, soapEnvelope);
            resultString = (SoapPrimitive) soapEnvelope.getResponse();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "Error: " + ex.getMessage());
        }

    }

    //AsyncTask
    private class AsyncCallWS extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(TAG, "doInBackground");
            calculate();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(TAG, "onPostExecute");
        }

    }

    private void enable_gps() {
        Toast.makeText(MainActivity.this, "Services is started", Toast.LENGTH_LONG).show();
        Intent gpsService = new Intent(MainActivity.this, GPS_Service.class);
        startService(gpsService);
    }

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
            }, 100);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                enable_gps();
            } else {
                runtime_permissions();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

}
