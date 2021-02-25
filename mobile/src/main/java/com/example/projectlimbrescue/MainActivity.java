package com.example.projectlimbrescue;

import android.app.Activity;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.util.Log;

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

public class MainActivity extends Activity {

    private XYPlot plot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize our XYPlot reference:
        plot = findViewById(R.id.plot);

        // enable pinch/zoom
        PanZoom.attach(plot);

        List<Number> xVals = new ArrayList<>();
        List<Number> yVals = new ArrayList<>();

        // get data from CSV file
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("data.csv")));
            Scanner myReader = new Scanner(reader);
            String data;
            while (myReader.hasNextLine()) {
                data = myReader.nextLine();
                String[] nums = data.split(",");
                yVals.add(Double.parseDouble(nums[1]));
            }
            myReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < yVals.size(); i++) {
            xVals.add(i * (1.0 / 30));
        }

        // turn the above arrays into XYSeries
        XYSeries series1 = new SimpleXYSeries(xVals, yVals, "heart rate");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format =
                new LineAndPointFormatter(this, R.xml.line_point_formatter);

        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

        //plot.setDomainBoundaries(0, xVals.size() * (1.0/30), BoundaryMode.FIXED);
        //plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 1);
        //plot.setRangeBoundaries(0, 0.1, BoundaryMode.AUTO);
        //plot.setRangeStep(StepMode.INCREMENT_BY_VAL, 0.01);


    }
}