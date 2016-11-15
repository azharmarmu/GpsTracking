package marmu.com.gpstracking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager extends SQLiteOpenHelper {

    private static String DBNAME = "DB_LOCATION";

    private static int VERSION = 1;

    private static final String DATABASE_TABLE = "TABLE_LOCATION";

    private static final String ROW_ID = "_id";
    private static final String IMEI = "imei";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String BATTERYPERCENTAGE = "batteryPercentage";

    public DatabaseManager(Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        /*String sql = "CREATE TABLE " + DATABASE_TABLE + "(" +
                ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                IMEI + " TEXT NOT NULL," +
                LATITUDE + " TEXT NOT NULL," +
                LONGITUDE + " TEXT NOT NULL," +
                BATTERYPERCENTAGE + " TEXT NOT NULL," +
                ")";*/
        String sql = "CREATE TABLE TABLE_LOCATION(_id  INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "imei TEXT NOT NULL," +
                "latitude  TEXT NOT NULL," +
                "longitude TEXT NOT NULL," +
                "batteryPercentage  TEXT NOT NULL)";
        sqLiteDatabase.execSQL(sql);
    }

    //Add Coordinates
    public void addCoordinates(String imei, String latitude, String longitude, String batteryPerccentage) {
        ContentValues values = new ContentValues();
        values.put(IMEI, imei);
        values.put(LATITUDE, latitude);
        values.put(LONGITUDE, longitude);
        values.put(BATTERYPERCENTAGE, batteryPerccentage);

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.insert(DATABASE_TABLE, null, values);
        sqLiteDatabase.close();
    }

    //Get All Coordinates
    public Cursor getAllCoordinates() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + DATABASE_TABLE;

        return sqLiteDatabase.rawQuery(selectQuery, null);
    }

    //Get Row Count
    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + DATABASE_TABLE;
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(countQuery, null);
        cursor.close();

        return cursor.getCount();
    }

    // Updating single Row
    public int updateContact(int id, String imei, String latitude, String longitude, String batteryPerccentage) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(IMEI, imei);
        values.put(LATITUDE, latitude);
        values.put(LONGITUDE, longitude);
        values.put(BATTERYPERCENTAGE, batteryPerccentage);
        // updating row
        return sqLiteDatabase.update(DATABASE_TABLE, values, ROW_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    // Deleting single contact
    public void deleteContact(int id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(DATABASE_TABLE, ROW_ID + " = ?",
                new String[] { String.valueOf(id) });
        sqLiteDatabase.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }
}
