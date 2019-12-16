package com.example.mc.group28;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class MyDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator + "CSE535_ASSIGNMENT3" + File.separator + "Group281.db";


    private String tableName = "";

    public MyDBHelper(Context context, SQLiteDatabase.CursorFactory factory, String tableName) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        this.tableName = tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(db);
    }

    public void createTable() {

        SQLiteDatabase db = getWritableDatabase();
//        db.execSQL("DROP TABLE IF EXISTS " + tableName);

        String activityColumns = "";
        for(int i=1;i<51;i++){
            activityColumns += "acc_x_" + i + " REAL NOT NULL," + "acc_y_" + i + " REAL NOT NULL," + "acc_z_" + i + " REAL NOT NULL, ";
        }

        try {
            String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + activityColumns + "activity_label INTEGER NOT NULL);";
            System.out.println(query);
            System.out.println(" creating table");
            db.execSQL(query);
        } catch (Exception e) {

        }
    }

    public void addActivityData(float data[], int activityLabel) {
        ContentValues values = new ContentValues();

        for(int i=0; i<50; i++){
            values.put("acc_x_" + (i+1), data[i]);
            values.put("acc_y_" + (i+1), data[i+1]);
            values.put("acc_z_" + (i+1), data[i+2]);
        }
        values.put("activity_label", activityLabel);
        SQLiteDatabase db = getWritableDatabase();
//        db.insert(tableName, null, values);
        try {
            convert(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.print("\n\n\n\n\n" + patient.getX() + "\n" + patient.getY());
        //db.close();
    }

    public void convert(int file) throws IOException {

        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_NAME,null);
        File toWrite;
        if(file == 0)
            toWrite = new File(Environment.getExternalStorageDirectory() + "/Android/Data/CSE535_ASSIGNMENT3","database.txt");
        else
            toWrite = new File(Environment.getExternalStorageDirectory() + "/Android/Data/CSE535_ASSIGNMENT3","predict.txt");

        Cursor cursor = null;
        String query="SELECT MAX(ID) FROM " + tableName;

        cursor=db.rawQuery(query,null);
        cursor.moveToFirst();

        int id = cursor.getInt(0);
        FileWriter writer=new FileWriter(toWrite);

        for(int i=0; i<id; i++)
        {
            String output="";
            String sqlquery="SELECT * FROM "+ tableName + " WHERE ID="+(i+1);

            cursor=db.rawQuery(sqlquery,null);
            cursor.moveToFirst();

            String label = cursor.getString(151);
//            Log.d("fr", "clas="+label);
            output = "+" + label + " ";
//            if(label.equalsIgnoreCase("walking"))
//                output="+1 ";
//            else if(label.equalsIgnoreCase("running"))
//                output="+2 ";
//            else if(label.equalsIgnoreCase("jumping"))
//                output="+3 ";


            for(int j=1; j<=150; j++)
            {
                output+=j+":"+cursor.getFloat(j)+" ";
            }
            output.trim();
            output+="\n";
            writer.append(output);
            writer.flush();

        }
        writer.close();
    }




    public void closeDB() {
        SQLiteDatabase db = getWritableDatabase();
        db.close();
    }
}
