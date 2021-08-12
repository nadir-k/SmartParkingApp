package com.example.smartparking.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.smartparking.Model.Preference;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Title: Android SQLite Database with Table
 * Author: Ravi Tamada
 * Date: 12 July 2017
 * Version: 1.0
 * Availability: https://www.androidhive.info/2013/09/android-sqlite-database-with-multiple-tables/
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    /**
     *
     * Title: Correctly Managing your SQLite Database
     * Author: Alex Lockwood
     * Date: 21 May 2012
     * Version: 1.0
     * Availability: https://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
     *
     */
    private static DatabaseHelper instance = null;


    private static final String LOG = "DatabaseHelper";

    //database version
    private static final int DATABASE_VERSION = 1;

    //database name
    private static final String DATABASE_NAME ="smartparking";

    private Context cxt;

    //database tables
    private static final String TABLE_PREFERENCE = "preference";

    //common names
    private static final String KEY_ID = "id";

    //preference column names
    private static final String KEY_LOWEST_FLOOR_LEVEL = "lowestFloorLevel";
    private static final String KEY_DISABLED_SPACES = "disabledSpaces";

    //create table preference
    private static final String CREATE_TABLE_PREFERENCE = "CREATE TABLE "
            + TABLE_PREFERENCE + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_LOWEST_FLOOR_LEVEL + " INTEGER," + KEY_DISABLED_SPACES + " INTEGER" + ")";


    public static DatabaseHelper getInstance(Context context){
        if(instance == null){
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     *
     * Title: Correctly Managing your SQLite Database
     * Author: Alex Lockwood
     * Date: 21 May 2012
     * Version: 1.0
     * Availability: https://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
     *
     */
    private DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.cxt = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PREFERENCE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREFERENCE);

        onCreate(db);
    }

    public void closeDB(){
        SQLiteDatabase db = this.getReadableDatabase();
        if(db != null && db.isOpen()){
            db.close();
        }
    }

    //create a preference
    public long createPreference(Preference preference){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LOWEST_FLOOR_LEVEL, preference.getLowestFloor());
        values.put(KEY_DISABLED_SPACES, preference.getDisabledSpace());

        long preference_id = db.insert(TABLE_PREFERENCE, null, values);

        return preference_id;
    }

    public Preference getPreference(long preference_id){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_PREFERENCE + " WHERE "
                + KEY_ID + " = " + preference_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if(c != null){
            c.moveToFirst();
        }

        Preference preference = new Preference();
        preference.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        preference.setLowestFloor(c.getInt(c.getColumnIndex(KEY_LOWEST_FLOOR_LEVEL)));
        preference.setDisabledSpace(c.getInt(c.getColumnIndex(KEY_DISABLED_SPACES)));

        return preference;
    }

    public List<Preference> getAllPreferences(){
        List<Preference> preferences = new ArrayList<Preference>();

        String selectQuery = "SELECT * FROM " + TABLE_PREFERENCE;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do {
                Preference preference = new Preference();

                preference.setId(cursor.getInt((cursor.getColumnIndex(KEY_ID))));
                preference.setLowestFloor(cursor.getInt((cursor.getColumnIndex(KEY_LOWEST_FLOOR_LEVEL))));
                preference.setDisabledSpace(cursor.getInt((cursor.getColumnIndex(KEY_DISABLED_SPACES))));

                preferences.add(preference);

            } while(cursor.moveToNext());
        }

        return preferences;
    }

    public int updatePreference(Preference preference){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LOWEST_FLOOR_LEVEL, preference.getLowestFloor());
        values.put(KEY_DISABLED_SPACES, preference.getDisabledSpace());

        return db.update(TABLE_PREFERENCE, values, KEY_ID + " = ?",
                new String[]{String.valueOf(preference.getId())});
    }


    public boolean getPreferenceRecordExist(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PREFERENCE;
        Cursor cur = db.rawQuery(query, null);

        boolean exist = (cur.getCount() == 0);
        cur.close();
        db.close();
        return exist;

    }
}
