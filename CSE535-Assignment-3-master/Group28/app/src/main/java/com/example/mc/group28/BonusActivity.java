package com.example.mc.group28;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import libsvm.svm;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import libsvm.svm_model;

public class BonusActivity extends AppCompatActivity implements AccelerometerListener {

    private Button activity_button;

    private float prediction_data[] = new float[150];
    private int dataIndex = 0;

    private svm_parameter param;
    private svm_problem data;
    private int nr_fold;
    private double accuracy_ans=0;

    private MyDBHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bonus);

        activity_button = (Button) findViewById(R.id.startActivityButton);
        set_parameters();
        try {
            read_data();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    @Override
    public void onAccelerationChanged(final float x, final float y, final float z) {
        addEntry(x, y, z);
    }

    @Override
    public void onShake(float force) {
//        Toast.makeText(this, "Motion detected", Toast.LENGTH_SHORT).show();
    }

    private void addEntry(float x, float y, float z) {

        prediction_data[dataIndex] = x;
        prediction_data[++dataIndex] = y;
        prediction_data[++dataIndex] = z;
        System.out.println(dataIndex);
        if(dataIndex == 149){
            if (AccelerometerManager.isListening()) {
                //Stop Accelerometer Listening
                AccelerometerManager.stopListening();
                activity_button.setEnabled(true);
            }
            Toast.makeText(this, "Data Added for Activity", Toast.LENGTH_SHORT).show();
            dataIndex = 0;
        }
        else{
            dataIndex++;
        }
    }

    private static double string_to_double(String s)
    {
        double d = Double.valueOf(s).doubleValue();
        if (Double.isNaN(d) || Double.isInfinite(d))
        {
            System.err.print("NaN or Infinity in input\n");
            System.exit(1);
        }
        return(d);
    }

    public void set_parameters()
    {

        param = new svm_parameter();

        // setting values
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.POLY;
        param.degree = 2;
        param.gamma = 0.007;	// 1/num_features
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 100;
        param.C = 10000;
        param.eps = 1e-2;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];
        nr_fold =5;
    }


    private svm_model train() throws IOException
    {
        AssetManager assetManager = getAssets();
        BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open("database.txt")));
        Vector<Double> vy = new Vector<Double>();
        Vector<svm_node[]> vx = new Vector<svm_node[]>();
        int max_index = 0;

        while(true)
        {
            String line = br.readLine();
            if(line == null)
                break;

            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

            vy.addElement(string_to_double(st.nextToken()));
            int m = st.countTokens()/2;
            svm_node[] x = new svm_node[m];
            for(int j=0;j<m;j++)
            {
                x[j] = new svm_node();
                x[j].index = Integer.parseInt(st.nextToken());
                x[j].value = string_to_double(st.nextToken());
            }
            if(m > 0)
                max_index = Math.max(max_index, x[m-1].index);
            vx.addElement(x);
        }

        data = new svm_problem();
        data.l = vy.size();
        data.x = new svm_node[data.l][];

        for(int i=0;i<data.l;i++)
            data.x[i] = vx.elementAt(i);
        data.y = new double[data.l];

        for(int i=0;i<data.l;i++)
            data.y[i] = vy.elementAt(i);

        if(param.gamma == 0 && max_index > 0)
            param.gamma = 1.0/max_index;

        svm_model model = svm.svm_train(data, param);
        br.close();

        return model;

    }

    public void evaluate(){

        svm_node[] nodes = new svm_node[prediction_data.length];
        for (int i = 0; i < prediction_data.length; i++)
        {
            svm_node node = new svm_node();
            node.index = i;
            node.value = prediction_data[i];
            nodes[i] = node;
        }

        try {
            svm_model model = train();
            int totalClasses = 3;
            int[] labels = new int[totalClasses];
            svm.svm_get_labels(model, labels);

            double[] prob_estimates = new double[totalClasses];
            double v = svm.svm_predict(model, nodes);

            for (int i = 0; i < totalClasses; i++){
                System.out.println("(" + labels[i] + ":" + prob_estimates[i] + ")");
            }

            System.out.println("HAHAHAHA: "+v);

            String result;
            if(v==1.0)
                result = "Walking";
            else if(v==2.0)
                result = "Running";
            else
                result = "Jumping";


            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error in training", Toast.LENGTH_SHORT).show();
        }

    }

    public void read_data() throws IOException
    {
        AssetManager assetManager = getAssets();
        BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open("database.txt")));
        Vector<Double> vy = new Vector<Double>();
        Vector<svm_node[]> vx = new Vector<svm_node[]>();
        int max_index = 0;

        while(true)
        {
            String line = br.readLine();
            if(line == null)
                break;

            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

            vy.addElement(string_to_double(st.nextToken()));
            int m = st.countTokens()/2;
            svm_node[] x = new svm_node[m];
            for(int j=0;j<m;j++)
            {
                x[j] = new svm_node();
                x[j].index = Integer.parseInt(st.nextToken());
                x[j].value = string_to_double(st.nextToken());
            }
            if(m > 0)
                max_index = Math.max(max_index, x[m-1].index);
            vx.addElement(x);
        }

        data = new svm_problem();
        data.l = vy.size();
        data.x = new svm_node[data.l][];

        for(int i=0;i<data.l;i++)
            data.x[i] = vx.elementAt(i);
        data.y = new double[data.l];

        for(int i=0;i<data.l;i++)
            data.y[i] = vy.elementAt(i);

        if(param.gamma == 0 && max_index > 0)
            param.gamma = 1.0/max_index;

        br.close();
    }

    public void startActivityButton(View view) {
        if (AccelerometerManager.isSupported(this)) {
            AccelerometerManager.startListening(this);
            activity_button.setEnabled(false);
//            Toast.makeText(this, "Accelerometer Started", Toast.LENGTH_SHORT).show();
        }

    }

    public void viewResultsButton(View view) {

        evaluate();
    }




}
