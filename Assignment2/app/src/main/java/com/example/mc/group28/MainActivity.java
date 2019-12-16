package com.example.mc.group28;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements AccelerometerListener {

    private TextView patientID;
    private TextView patientName;
    private TextView patientAge;
    private RadioGroup patientSex;

    private MyDBHelper myDB;
    private MyDBHelper downloadMyDB;

    LineGraphSeries<DataPoint> xseries;
    LineGraphSeries<DataPoint> yseries;
    LineGraphSeries<DataPoint> zseries;

    private final Handler mHandler = new Handler();

    private int lastX = 0;
    Button download;

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


        GraphView graph = (GraphView) findViewById(R.id.graph);

        Viewport viewport = graph.getViewport();
        viewport.setXAxisBoundsManual(true);;
        viewport.setMinX(0);
        viewport.setMaxX(9);
        viewport.setScrollable(true);

        xseries = new LineGraphSeries<DataPoint>();
        yseries = new LineGraphSeries<DataPoint>();
        zseries = new LineGraphSeries<DataPoint>();

        xseries.setColor(Color.BLUE); //X - Accelerometer
        yseries.setColor(Color.CYAN); //Y - Accelerometer
        zseries.setColor(Color.GREEN); //Z- Accelerometer

        graph.addSeries(xseries);
        graph.addSeries(yseries);
        graph.addSeries(zseries);

        download = (Button) findViewById(R.id.downloadButton);

    }

    //Everytime the acceleration of the phone is changed is changed this method is invoked
    @Override
    public void onAccelerationChanged(final float x, final float y, final float z) {
        addEntry(x, y, z);
        Long timestamp = System.currentTimeMillis() / 1000;
        Patient patient = new Patient(x, y, z, timestamp);
        myDB.addPatientData(patient);
    }


    private void addEntry(float x, float y, float z) {
        // here, we choose to display max 10 points on the viewport and we scroll to end
        xseries.appendData(new DataPoint(lastX, x), true, 10);
        yseries.appendData(new DataPoint(lastX, y), true, 10);
        zseries.appendData(new DataPoint(lastX, z), true, 10);
        lastX++;
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

    public void runButton(View view) {


        View v = this.getCurrentFocus();

        // to hide the keyboard when we click the run button
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

        // Check if the storage permission has been granted
        if (isStoragePermissionGranted()) {
            this.patientID = findViewById(R.id.text_patient_id);
            this.patientName = findViewById(R.id.text_patient_name);
            this.patientAge = findViewById(R.id.text_age);
            this.patientSex = findViewById(R.id.radio_group_text);
            boolean isMale = true;
            if (this.patientSex.getCheckedRadioButtonId() == R.id.radio_female) {
                isMale = false;
            }
            String gender = "Male";
            if (!isMale) {
                gender = "Female";
            }

            if (patientID.getText().toString().equals("")) {
                patientID.setError("Please enter Patient ID");
            } else if (patientAge.getText().toString().equals("")) {
                patientAge.setError("Please enter Patient Age");
            } else if (patientName.getText().toString().equals("")) {
                patientName.setError("Please enter Patient Name");
            } else {
                if (AccelerometerManager.isSupported(this)) {
                    AccelerometerManager.startListening(this);
                    Toast.makeText(this, "Accelerometer Started", Toast.LENGTH_SHORT).show();
                }

                String tableName = patientName.getText().toString() + "_" + patientID.getText().toString() + "_" + patientAge.getText().toString() + "_" + gender;

                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator;

                System.out.print("*******" + path + "******\n");

                File dir = new File(path + "CSE535_ASSIGNMENT2");
                File dir2 = new File(path+"CSE535_ASSIGNMENT2_DOWN");

                try {
                    if (dir.mkdir()) {
                        System.out.println("Directory created");
                    } else {
                        System.out.println("Directory is not created");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (dir2.mkdir()) {
                        System.out.println("Directory created");
                    } else {
                        System.out.println("Directory is not created");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                myDB = new MyDBHelper(this, path + "Group28.db", null, 1, tableName);
                myDB.createTable();

            }
        }
    }

    public void stopButton(View view) {

        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.removeAllSeries();

        if (AccelerometerManager.isListening()) {
            //Start Accelerometer Listening
            AccelerometerManager.stopListening();
            Toast.makeText(this, "Accelerometer Stopped", Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadButton(View view) {

        new UploadFile(this).execute("foo");
        final Handler handler = new Handler();
        Toast.makeText(MainActivity.this, "Uploading....", Toast.LENGTH_SHORT).show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                // Do something after 5s = 5000ms
                Toast.makeText(MainActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
            }
        }, 5000);


    }

    public void downloadButton(View view) {
        final GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.removeAllSeries();

        new DownloadFile().execute("foo");

        Toast.makeText(MainActivity.this, "Downloading....", Toast.LENGTH_SHORT).show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                Toast.makeText(MainActivity.this, "Download Successful", Toast.LENGTH_SHORT).show();
                boolean isMale = true;
                if (MainActivity.this.patientSex.getCheckedRadioButtonId() == R.id.radio_female) {
                    isMale = false;
                }
                String gender = "Male";
                if (!isMale) {
                    gender = "Female";
                }

                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator+"CSE535_ASSIGNMENT2_DOWN";
                String tableName = patientName.getText().toString() + "_" + patientID.getText().toString() + "_" + patientAge.getText().toString() + "_" + gender;

                downloadMyDB = new MyDBHelper(MainActivity.this, path + "Group28_download.db", null, 1, tableName, true);
                String data = downloadMyDB.dbToString();

                String[] dataSplit = data.split("\\r?\\n");

                xseries.resetData(new DataPoint[]{new DataPoint(0, 0)});
                yseries.resetData(new DataPoint[]{new DataPoint(0, 0)});
                zseries.resetData(new DataPoint[]{new DataPoint(0, 0)});
                lastX = 0;

                for (String s : dataSplit) {
                    String valuesSplit[] = s.split("\\s+");

                    xseries.appendData(new DataPoint(lastX, Float.parseFloat(valuesSplit[0])), true, 10);
                    yseries.appendData(new DataPoint(lastX, Float.parseFloat(valuesSplit[1])), true, 10);
                    zseries.appendData(new DataPoint(lastX, Float.parseFloat(valuesSplit[2])), true, 10);
                    lastX++;
                }

                graph.addSeries(xseries);
                graph.addSeries(yseries);
                graph.addSeries(zseries);

            }
        }, 5000);


    }

    private class UploadFile extends AsyncTask<String, Void, Boolean> {

        private Context mContext;

        public UploadFile(Context context) {
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(String... params) {


            HttpClient httpClient = new DefaultHttpClient();
            StringBuilder builder = new StringBuilder();
            try {
                HttpPost request = new HttpPost("http://impact.asu.edu/CSE535Spring18Folder/UploadToServer.php");

                MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator + "CSE535_ASSIGNMENT2" + File.separator + "Group28.db");

                f.createNewFile();
                FileBody picBody = new FileBody(f, "application/x-sqlite3");

                entity.addPart("uploaded_file", picBody);
                request.setEntity(entity);
                HttpResponse response = httpClient.execute(request);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity responseEntity = response.getEntity();
                    String responseStr = EntityUtils.toString(responseEntity);
                    System.out.println(responseStr);
                    return true;
                } else {
                    Toast.makeText(mContext, "Upload Failed:", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (IOException e) {
                Log.e("FileUpload IOException ", e.getMessage());
                Toast.makeText(getApplicationContext(), "Upload Failed:", Toast.LENGTH_SHORT).show();
                return false;
            } catch (Exception e) {
                Log.e("Exception", e.getMessage());
                Toast.makeText(getApplicationContext(), "Upload Failed:", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }

    private class DownloadFile extends AsyncTask<String, Void, Boolean> {

        private static final int MEGABYTE = 1024 * 1024;

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            StringBuilder builder = new StringBuilder();

            try {
                HttpGet request = new HttpGet("http://impact.asu.edu/CSE535Spring18Folder/Group28.db");

                MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                HttpResponse response = httpClient.execute(request);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {

                    FileOutputStream f = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator + "CSE535_ASSIGNMENT2_DOWN" + File.separator + "Group28_download.db");

                    HttpEntity responseEntity = response.getEntity();
                    InputStream content = responseEntity.getContent();
                    byte[] buffer = new byte[MEGABYTE];
                    int bufferLength = 0;
                    while ((bufferLength = content.read(buffer)) > 0) {
                        f.write(buffer, 0, bufferLength);
                    }
                    f.close();

                    return true;
                } else {

                    return false;
                }

            } catch (IOException e) {
                Log.e("FileUpload IOException ", e.getMessage());
                //Toast.makeText(getApplicationContext(), "Upload Failed:", Toast.LENGTH_SHORT).show();
                return false;
            } catch (Exception e) {
                Log.e("Exception", e.getMessage());
              // Toast.makeText(getApplicationContext(), "Upload Failed:", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }



}


