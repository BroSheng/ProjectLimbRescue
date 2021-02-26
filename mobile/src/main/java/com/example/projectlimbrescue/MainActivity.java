package com.example.projectlimbrescue;

import android.app.Activity;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.OrderedXYSeries;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SampledXYSeries;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.myapplication.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String XVALUES = "xvalues";
    private static final String YVALUES = "yvalues";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get data for graph
        List<Long> xVals = new ArrayList<>();
        List<Double> yVals = new ArrayList<>();

        // get data from CSV file
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("data.csv")));
            Scanner myReader = new Scanner(reader);
            String data;
            while (myReader.hasNextLine()) {
                data = myReader.nextLine();
                String[] nums = data.split(",");
                xVals.add(Long.parseLong(nums[0]));
                yVals.add(Double.parseDouble(nums[1]));
            }
            myReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // convert since I have no easy way of doing this
        long[] xValues = new long[xVals.size()];
        double[] yValues = new double[yVals.size()];
        for (int i = 0; i < xVals.size(); i++) {
            xValues[i] = xVals.get(i);
            yValues[i] = yVals.get(i);
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container_view);

        // initialize GraphFragment
        if (fragment == null) {
            Bundle bundle = new Bundle();
            bundle.putLongArray(XVALUES, xValues);
            bundle.putDoubleArray(YVALUES, yValues);
            fragment = new GraphFragment();
            fragment.setArguments(bundle);
            fm.beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, fragment)
                    .commit();
        }
    }
}