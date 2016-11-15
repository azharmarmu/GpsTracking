package marmu.com.gpstracking;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by azharuddin on 14/11/16.
 */

public class Common {

    public static void saveUserData(String key, String data, Context context) {
        //Attempt to invoke virtual method 'android.content.SharedPreferences
        // android.content.Context.getSharedPreferences(java.lang.String, int)' on a null object reference

        SharedPreferences sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, data);
        editor.commit();
    }

    public static String getUserData(String key, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return sharedPref.getString(key, "");
    }
}
