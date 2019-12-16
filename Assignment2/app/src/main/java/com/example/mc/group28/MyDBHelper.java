package com.example.mc.group28;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;

public class MyDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator + "CSE535_ASSIGNMENT2" + File.separator + "Group28.db";
    private static final String DOWNLOAD_DATABASE_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator + "CSE535_ASSIGNMENT2_DOWN" + File.separator + "Group28_download.db";

    private String Name_ID_Age_Sex = "Name_ID_Age_Sex";
    private static final String x = "x";
    private static final String y = "y";
    private static final String z = "z";
    private static final String time_stamp = "time_stamp";

    public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, String tableName) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        this.Name_ID_Age_Sex = tableName;
    }

    public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, String tableName, boolean type) {
        super(context, DOWNLOAD_DATABASE_NAME, factory, DATABASE_VERSION);
        this.Name_ID_Age_Sex = tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + Name_ID_Age_Sex);
        onCreate(db);
    }

    public void createTable() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            String query = "CREATE TABLE " + Name_ID_Age_Sex + "(" + x + " REAL," + y + " REAL," + z + " REAL," + time_stamp + " INT);";
            System.out.println(" creating table");
            db.execSQL(query);
        } catch (Exception e) {

        }
    }

    public void addPatientData(Patient patient) {
        ContentValues values = new ContentValues();
        values.put(x, patient.getX());
        values.put(y, patient.getY());
        values.put(z, patient.getZ());
        values.put(time_stamp, patient.getTime_stamp());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(Name_ID_Age_Sex, null, values);
        System.out.print("\n\n\n\n\n" + patient.getX() + "\n" + patient.getY());
        //db.close();
    }

    public String dbToString() {
        String dbString = "";
        SQLiteDatabase db = getWritableDatabase();
        System.out.println("HAHAHAHAH");
        System.out.println(db);
        String query = "SELECT * FROM " + Name_ID_Age_Sex + " DESC limit 10;";
        Cursor c;

        c = db.rawQuery(query, null);

        c.moveToFirst();
        while (!c.isAfterLast()) {
            if (c.getString(c.getColumnIndex(time_stamp)) != null) {
                dbString += c.getString(c.getColumnIndex(x));
                dbString += "\t";
                dbString += c.getString(c.getColumnIndex(y));
                dbString += "\t";
                dbString += c.getString(c.getColumnIndex(z));
                dbString += "\n";
            }
            c.moveToNext();
        }
        return dbString;

    }

    public String getName_ID_Age_Sex() {
        return Name_ID_Age_Sex;
    }

    public void setName_ID_Age_Sex(String name_ID_Age_Sex) {
        this.Name_ID_Age_Sex = name_ID_Age_Sex;
    }

    public void closeDB() {
        SQLiteDatabase db = getWritableDatabase();
        db.close();
    }
}
