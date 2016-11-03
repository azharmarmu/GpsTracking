package marmu.com.gpstracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by azharuddin on 4/11/16.
 */

public class CoordinatesReceiver extends BroadcastReceiver {

    private String TAG = "Response";

    private String imei, latitude, longitude;
    private int batteryPercentage;


    @Override
    public void onReceive(Context context, Intent intent) {
        imei = intent.getExtras().get("IMEI").toString();
        latitude = intent.getExtras().get("Latitude").toString();
        longitude = intent.getExtras().get("Longitude").toString();
        batteryPercentage = (int) new MainActivity().batteryLevel();

        AsyncCallWS task = new AsyncCallWS();
        task.execute();

        Log.i("Longitude", longitude);
        Log.i("Latitude", latitude);
        Log.i("BatteryPercentage", String.valueOf(batteryPercentage));

    }


    //SOAP method
    public void calculate() {
        String SOAP_ACTION = "http://tempuri.org/SetEnableTracking";
        String METHOD_NAME = "SetEnableTracking";
        String NAMESPACE = "http://tempuri.org/";
        String URL = "http://198.12.153.30/SETrackMobility/SETrack.asmx";
        try {
            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);

            /*PropertyInfo pi = new PropertyInfo();
            pi.name = "IMEANo";
            pi.type = PropertyInfo.STRING_CLASS;
            Request.addProperty(pi, imei);

            pi.name = "Latitude";
            pi.type = PropertyInfo.STRING_CLASS;*/

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
            SoapPrimitive resultString = (SoapPrimitive) soapEnvelope.getResponse();
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });*/
        } catch (Exception ex) {
            Log.e(TAG, "Error: " + ex.getMessage());
        }

    }

}
