package com.example.projectlimbrescue;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.myapplication.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GraphFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GraphFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String XVALUES = "xvalues";
    private static final String YVALUES = "yvalues";

    private List<Double> mXVals;
    private List<Double> mYVals;
    private XYPlot mPlot;

    public GraphFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param xvals X values for the graph
     * @param yvals Y values for the graph
     * @return A new instance of fragment GraphFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GraphFragment newInstance(String xvals, String yvals) {
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        args.putString(XVALUES, xvals);
        args.putString(YVALUES, yvals);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            long[] x;
            double[] y;
            mXVals = new ArrayList<>();
            mYVals = new ArrayList<>();

            x = getArguments().getLongArray(XVALUES);
            y = getArguments().getDoubleArray(YVALUES);

            // convert arrays to ArrayList (I can't find a better way of doing this)
            // also have x start from 0
            long start = x[0];
            for (int i = 0; i < x.length; i++) {
                mXVals.add((x[i] - start) / 1000000000.0);
                mYVals.add(y[i]);
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_graph, container, false);

        mPlot = v.findViewById(R.id.plot);

        // initialize the plot

        PanZoom.attach(mPlot);

        // turn the above arrays into XYSeries
        XYSeries series1 = new SimpleXYSeries(mXVals, mYVals, "heart rate");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format =
                new LineAndPointFormatter(getContext(), R.xml.line_point_formatter);

        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:
        mPlot.addSeries(series1, series1Format);

        // display more decimals on range
        mPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT)
                .setFormat(new DecimalFormat("0.000"));

        //plot.setDomainBoundaries(0, xVals.size() * (1.0/30), BoundaryMode.FIXED);
        //plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 1);
        //plot.setRangeBoundaries(0, 0.1, BoundaryMode.AUTO);
        //plot.setRangeStep(StepMode.INCREMENT_BY_VAL, 0.01);

        return v;
    }
}