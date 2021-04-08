package com.example.projectlimbrescue;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

public class GraphActivity extends AppCompatActivity {
    XYPlot mPlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        mPlot = findViewById(R.id.plot);

        Intent intent = getIntent();

        // get arrays
        long[] xLeft = intent.getLongArrayExtra(HistoryFragment.LEFT_X_VALUES);
        double[] yLeft = intent.getDoubleArrayExtra(HistoryFragment.LEFT_Y_VALUES);
        long[] xRight = intent.getLongArrayExtra(HistoryFragment.RIGHT_X_VALUES);
        double[] yRight = intent.getDoubleArrayExtra(HistoryFragment.RIGHT_Y_VALUES);

        // initialize the plot

        PanZoom.attach(mPlot);

        /*
            We can't use primitive arrays to create an XYSeries and there's no easy
            way to convert them so we have to do this
         */

        // turn the above arrays into XYSeries
        XYSeries rightLimb = null;
        if (xRight != null) {
            List<Long> xRightList = new LinkedList<>();
            List<Double> yRightList = new LinkedList<>();
            for (int i = 0; i < xRight.length; i++) {
                xRightList.add(xRight[i]);
                yRightList.add(yRight[i]);
            }
            rightLimb = new SimpleXYSeries(xRightList, yRightList, "Right Limb PPG");
        }

        XYSeries leftLimb = null;
        if (xLeft != null) {
            List<Long> xLeftList = new LinkedList<>();
            List<Double> yLeftList = new LinkedList<>();
            for (int i = 0; i < xLeft.length; i++) {
                xLeftList.add(xLeft[i]);
                yLeftList.add(yLeft[i]);
            }
            leftLimb = new SimpleXYSeries(xLeftList, yLeftList, "Left Limb PPG");
        }

        // add a new series' to the xyplot
        if (rightLimb != null) {
            // create formatter
            LineAndPointFormatter rightLimbFormat = new LineAndPointFormatter(this,
                    R.xml.right_limb_point_formatter);
            rightLimbFormat.setInterpolationParams(new CatmullRomInterpolator.Params(4,
                    CatmullRomInterpolator.Type.Centripetal));

            // add plot with formatter to series
            mPlot.addSeries(rightLimb, rightLimbFormat);
        }

        if (leftLimb != null) {
            // create formatter
            LineAndPointFormatter leftLimbFormat =
                    new LineAndPointFormatter(this, R.xml.line_point_formatter);
            leftLimbFormat.setInterpolationParams(new CatmullRomInterpolator.Params(4,
                    CatmullRomInterpolator.Type.Centripetal));

            // add plot with formatter to series
            mPlot.addSeries(leftLimb, leftLimbFormat);
        }

        // display more decimals on range
        mPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT)
                .setFormat(new DecimalFormat("0.000"));
    }


}
