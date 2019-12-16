package com.example.mc.group28;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import libsvm.svm;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class ResultsActivity extends AppCompatActivity {

    private svm_parameter param;
    private svm_problem data;
    private int nr_fold;
    private double accuracy_ans=0;


    private static double s_to_d(String s)
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
            if(line == null) break;

            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

            vy.addElement(s_to_d(st.nextToken()));
            int m = st.countTokens()/2;
            svm_node[] x = new svm_node[m];
            for(int j=0;j<m;j++)
            {
                x[j] = new svm_node();
                x[j].index = Integer.parseInt(st.nextToken());
                x[j].value = s_to_d(st.nextToken());
            }
            if(m>0) max_index = Math.max(max_index, x[m-1].index);
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

    private void accuracy_using_crossValidation()
    {
        int i;
        int total_correct = 0;
        double[] target = new double[data.l];

        svm.svm_cross_validation(data,param,nr_fold,target);

        for(i=0;i<data.l;i++)
            if(target[i] == data.y[i])
                ++total_correct;
        accuracy_ans = 100.0*total_correct/data.l;
        System.out.println("In Cross");
//        Toast.makeText(getBaseContext(), "Cross Validation Accuracy = "+100.0*total_correct/data.l+"%\n", Toast.LENGTH_LONG).show();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);


        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = (ResultsActivity.this).registerReceiver(null, ifilter);

        try
        {
            set_parameters();
        }
        catch(Exception ex)
        {
            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        TextView t = (TextView) findViewById(R.id.parameters);
        t.setText("SVM Type = "+param.svm_type+";\n" +
                "Kernel Type = "+param.kernel_type+";\n" +
                "Degree = "+param.degree+";\n" +
                "Gamma = "+ param.gamma+";\n" +
                "Coefficient 0 = "+ param.coef0+";\n" +
                "Nu = "+param.nu+";\n" +
                "Cache Size = "+param.cache_size+";\n" +
                "C = "+param.C+";\n" +
                "EPS = "+ param.eps+";\n" +
                "p = "+param.p+";\n" +
                "Shrinking = "+param.shrinking+";\n" +
                "NR Fold ="+nr_fold+";\n");

        TextView tv = (TextView)findViewById(R.id.accuracy);

        BatteryManager mBatteryManager = (BatteryManager)ResultsActivity.this.getSystemService(Context.BATTERY_SERVICE);
        int level = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        double mAh = (2300 * level * 0.01);

        try
        {
//            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int scale = 1;

            float Previous_batteryPct = level / (float)scale;
            System.out.println(Previous_batteryPct);
            read_data();

            String error_msg = svm.svm_check_parameter(data,param);

            if(error_msg != null)
            {
                Toast.makeText(getBaseContext(), error_msg, Toast.LENGTH_LONG).show();
            }

            accuracy_using_crossValidation();

//            level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//            scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            level = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

            float Final_batteryPct = level / (float)scale;
            System.out.println(Final_batteryPct);
            float Used_batteryPct= Final_batteryPct-Previous_batteryPct;

//            Toast.makeText(getBaseContext(),"Battery Used="+Used_batteryPct,Toast.LENGTH_LONG).show();
            tv.setText(""+accuracy_ans);

        }catch(Exception ex) {
            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}








