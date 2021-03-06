package marmu.com.gpstracking;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.telephony.TelephonyManager;

public class GPS_Service extends Service {

    WakeLock wakeLock;
    private LocationListener locationListener;
    private LocationManager locationManager;

    public GPS_Service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNotSleep");

        wakeLock.acquire();

        locationListener = new LocationListener() {

            @SuppressLint("HardwareIds")
            @Override
            public void onLocationChanged(Location location) {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                Intent i = new Intent("marmu.com.gpstracking.receiver");
                //Intent i = new Intent("location_update");
                i.putExtra("IMEI", tm.getDeviceId());
                i.putExtra("Latitude", location.getLatitude());
                i.putExtra("Longitude", location.getLongitude());
                sendBroadcast(i);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                // Settings panel to enable locationListener
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        //locationManager = (LocationManager) getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
        //noinspection MissingPermission
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeLock.release();
        if (locationManager != null) {
            //noinspection MissingPermission
            locationManager.removeUpdates(locationListener);
        }
    }
}
