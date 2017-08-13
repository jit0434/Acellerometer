package com.example.papa.acceldataplot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


import android.widget.Button;
import android.widget.LinearLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.LegendRenderer;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView xText, yText, zText, temp;
    private int i = 0;
    private Sensor mySensor;
    private SensorManager SM;
    private Button btnStart, btnStop, btnUpload;
    private boolean started = false;
    private ArrayList<AccelData> sensorData;
    private ArrayList<SGFilter> filtData;
    private LinearLayout layout;
    private SensorManager sensorManager;
    private final String TAG = "GraphSensors";
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private GraphView mGraphAccel;
    private GraphView line_graph;
    private LineGraphSeries<DataPoint>  mSeriesAccelX, mSeriesAccelY, mSeriesAccelZ, mSeriesAccelTotal;
    private LineGraphSeries<DataPoint> mSeriesAccelsmooth1, mSeriesAccelsmooth2, mSeriesAccelsmooth3;
    private double graphLastAccelXValue = 5d;
    private Double totalaccel, smoothaccel;
    private float[] data;
    private ArrayList<Float> datar = new ArrayList<Float>(50);

    private double[] coeffs ;
    float[] smooth = new float[500];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);

        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);
        temp = (TextView)findViewById(R.id.smoothText);


        mSeriesAccelX = initSeries(Color.BLUE, "X");
        mSeriesAccelY = initSeries(Color.RED, "Y");
        mSeriesAccelZ = initSeries(Color.GREEN, "Z");

        mGraphAccel = initGraph(R.id.xyzgraph, "Sensor.TYPE_ACCELEROMETER");

        mGraphAccel.addSeries(mSeriesAccelX);
        mGraphAccel.addSeries(mSeriesAccelY);
        mGraphAccel.addSeries(mSeriesAccelZ);

        mSeriesAccelTotal = initSeries(Color.BLUE, "Accel");

        mGraphAccel = initGraph(R.id.accelgraph, "Sensor.TYPE_ACCELEROMETER");

        mGraphAccel.addSeries(mSeriesAccelTotal);

//        mSeriesAccelsmooth1 = initSeries(Color.RED, "FData1");
//        mSeriesAccelsmooth2 = initSeries(Color.BLACK, "FData2");
        mSeriesAccelsmooth3 = initSeries(Color.BLUE, "FData3");
        //mGraphAccel = initGraph(R.id.smoothgraph, "Sensor.TYPE_ACCELEROMETER");
        //mGraphAccel.addSeries(mSeriesAccelsmooth1);
        //mGraphAccel.addSeries(mSeriesAccelsmooth2);
        mGraphAccel.addSeries(mSeriesAccelsmooth3);

        startAccel();
    }


    public GraphView initGraph(int id, String title) {
        GraphView graph = (GraphView) findViewById(id);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(5);
        graph.getGridLabelRenderer().setLabelVerticalWidth(100);
        graph.setTitle(title);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        return graph;
    }

    public void startAccel(){
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not in use
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x,y,z;
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

        xText.setText("X: " + x);
        yText.setText("Y: " + y);
        zText.setText("Z: " + z);

        graphLastAccelXValue += 0.15d;

        mSeriesAccelX.appendData(new DataPoint(graphLastAccelXValue, x), true, 33);
        mSeriesAccelY.appendData(new DataPoint(graphLastAccelXValue, y), true, 33);
        mSeriesAccelZ.appendData(new DataPoint(graphLastAccelXValue, z), true, 33);

        totalaccel = Math.sqrt(x * x + y * y + z * z);
        mSeriesAccelTotal.appendData(new DataPoint(graphLastAccelXValue, totalaccel) ,true, 33);

        Float obj = new Float(totalaccel);
        datar.add(obj);
        data=new float[datar.size()];
        for(int j=0;j<datar.size();j++){
            data[j]=(float)datar.get(j);}
        coeffs = SGFilter.computeSGCoefficients(5, 5, 4);
        SGFilter sgFilter = new SGFilter(5, 5);
        smooth = sgFilter.smooth(data, coeffs);
//        smoothaccel = 1;

       // mSeriesAccelsmooth1.appendData(new DataPoint(graphLastAccelXValue, ((double)smooth[i])),true, 33);
        // mSeriesAccelsmooth2.appendData(new DataPoint(graphLastAccelXValue, realResult[i%realResult.length]), true, 33);

        mSeriesAccelsmooth3.appendData(new DataPoint(graphLastAccelXValue, smooth[i]*2), true, 33);
        i++;
        String dataString = String.valueOf(event.accuracy) + "," + String.valueOf(event.timestamp) + "," + String.valueOf(event.sensor.getType()) + "\n";
        Log.d(TAG, "Data received: " + dataString);
    }


    public LineGraphSeries<DataPoint> initSeries(int color, String title) {
        LineGraphSeries<DataPoint> series;
        series = new LineGraphSeries<>();
        series.setDrawDataPoints(true);
        series.setDrawBackground(false);
        series.setColor(color);
        series.setTitle(title);
        return series;
    }

    private void assertCoeffsEqual(double[] coeffs, double[] tabularCoeffs) {
        for (int i = 0; i < tabularCoeffs.length; i++) {
            assertEquals(tabularCoeffs[i], coeffs[i], 0.001);
        }
    }

    private void assertResultsEqual(double[] results, double[] real) {
        for (int i = 0; i < real.length; i++) {assertEquals(real[i], results[i], 0.001);
        }
    }

    private void assertResultsEqual(float[] results, double[] real) {
        for (int i = 0; i < real.length; i++) {
            assertEquals(real[i], results[i], 0.1);
        }
    }


}