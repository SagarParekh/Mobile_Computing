package com.example.mc.group28;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


import java.io.File;

public class MainActivity extends AppCompatActivity implements AccelerometerListener {


    private RadioGroup activity;

    private MyDBHelper myDB;


    LineGraphSeries<DataPoint> xseries;
    LineGraphSeries<DataPoint> yseries;
    LineGraphSeries<DataPoint> zseries;

    GraphView graph;


    private int lastX = 0;
    private Button activity_button;

    private float data[] = new float[150];
    private int dataIndex = 0;
    private int activityLabel;

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permission granted");
                return true;
            } else {
                System.out.println("Permission revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            System.out.println("Permission granted API <23");
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean b = isStoragePermissionGranted();

//        graph = (GraphView) findViewById(R.id.graph);
//
//        Viewport viewport = graph.getViewport();
//        viewport.setXAxisBoundsManual(true);;
//        viewport.setMinX(0);
//        viewport.setMaxX(9);
//        viewport.setScrollable(true);
//
//        xseries = new LineGraphSeries<DataPoint>();
//        yseries = new LineGraphSeries<DataPoint>();
//        zseries = new LineGraphSeries<DataPoint>();
//
//        xseries.setColor(Color.BLUE); //X - Accelerometer
//        yseries.setColor(Color.CYAN); //Y - Accelerometer
//        zseries.setColor(Color.GREEN); //Z- Accelerometer
//
//        graph.addSeries(xseries);
//        graph.addSeries(yseries);
//        graph.addSeries(zseries);


        activity_button = (Button) findViewById(R.id.startActivityButton);


    }

    //Everytime the acceleration of the phone is changed is changed this method is invoked
    @Override
    public void onAccelerationChanged(final float x, final float y, final float z) {
        addEntry(x, y, z);
    }


    private void addEntry(float x, float y, float z) {
        // here, we choose to display max 10 points on the viewport and we scroll to end
//        xseries.appendData(new DataPoint(lastX, x), true, 10);
//        yseries.appendData(new DataPoint(lastX, y), true, 10);
//        zseries.appendData(new DataPoint(lastX, z), true, 10);
        lastX++;
        data[dataIndex] = x;
        data[++dataIndex] = y;
        data[++dataIndex] = z;
        System.out.println(dataIndex);
        if(dataIndex == 149){
            if (AccelerometerManager.isListening()) {
                //Stop Accelerometer Listening
                AccelerometerManager.stopListening();
                activity_button.setEnabled(true);
//                Toast.makeText(this, "Accelerometer Stopped", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, "Data Added for Activity", Toast.LENGTH_SHORT).show();
            myDB.addActivityData(data, activityLabel);
            dataIndex = 0;
//            graph.removeAllSeries();
//            lastX = 0;
        }
        else{
            dataIndex++;
        }
    }


    @Override
    public void onShake(float force) {
//        Toast.makeText(this, "Motion detected", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (AccelerometerManager.isListening()) {
            AccelerometerManager.stopListening();
            Toast.makeText(this, "Accelerometer Stopped", Toast.LENGTH_SHORT).show();
        }
    }

    public void startActivityButton(View view) {

        activity_button.setEnabled(false);

        // Check if the storage permission has been granted
        if (isStoragePermissionGranted()) {

            this.activity = findViewById(R.id.radio_group_text);

            if (this.activity.getCheckedRadioButtonId() == R.id.radio_walking) {
                activityLabel = 1;
            }
            else if(this.activity.getCheckedRadioButtonId() == R.id.radio_running){
                activityLabel = 2;
            }
            else if(this.activity.getCheckedRadioButtonId() == R.id.radio_jumping){
                activityLabel = 3;
            }
            else{
                activityLabel = 1;
            }


            if (AccelerometerManager.isSupported(this)) {
                AccelerometerManager.startListening(this);
                Toast.makeText(this, "Accelerometer Started", Toast.LENGTH_SHORT).show();
            }

            String tableName = "Activity_Data";
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator;

            System.out.print("*******" + path + "******\n");

            File dir = new File(path + "CSE535_ASSIGNMENT3");


            try {
                if (dir.mkdir()) {
                    System.out.println("Directory created");
                } else {
                    System.out.println("Directory is not created");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            myDB = new MyDBHelper(this, null, tableName);
            myDB.createTable();


        }
        else{
            Toast.makeText(this, "Please grant storage permission", Toast.LENGTH_SHORT).show();
        }
    }

    public void viewResultsButton(View view) {

//        GraphView graph = (GraphView) findViewById(R.id.graph);
//        graph.removeAllSeries();

        Intent intent= new Intent(MainActivity.this, ResultsActivity.class);
        startActivity(intent);

    }

    public void webViewButton(View view) {


        Intent intent= new Intent(MainActivity.this, WebGraphView.class);
        startActivity(intent);

    }

    public void bonusButton(View view) {


        Intent intent= new Intent(MainActivity.this, BonusActivity.class);
        startActivity(intent);

    }

}


