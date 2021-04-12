package com.example.projectlimbrescue;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.shared.ReadingLimb;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GraphFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String LEFT_LIMB_X = "left_limb_x";
    public static final String LEFT_LIMB_Y = "left_limb_y";

    public static final String RIGHT_LIMB_X = "right_limb_x";
    public static final String RIGHT_LIMB_Y = "right_limb_y";

    private static final double NANO_TO_SECONDS = 1000000000.0;

    // x values are float since we convert the time in nanoseconds to time since first reading
    private List<Double> leftLimbX;
    private List<Double> leftLimbY;

    private List<Double> rightLimbX;
    private List<Double> rightLimbY;

    public GraphFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rightLimbX = new ArrayList<>();
            rightLimbY = new ArrayList<>();
            leftLimbX = new ArrayList<>();
            leftLimbY = new ArrayList<>();

            long[] rightX = getArguments().getLongArray(RIGHT_LIMB_X);
            double[] rightY = getArguments().getDoubleArray(RIGHT_LIMB_Y);

            long[] leftX = getArguments().getLongArray(LEFT_LIMB_X);
            double[] leftY = getArguments().getDoubleArray(LEFT_LIMB_Y);

            if (rightX != null) {
                // convert arrays to ArrayList (I can't find a better way of doing this)
                // also have x start from 0
                long rightStart = rightX[0];
                for (int i = 0; i < rightX.length; i++) {
                    rightLimbX.add((rightX[i] - rightStart) / NANO_TO_SECONDS);
                    rightLimbY.add(rightY[i]);
                }
            }

            if (leftX != null) {
                long leftStart = leftX[0];
                for (int i = 0; i < leftX.length; i++) {
                    leftLimbX.add((leftX[i] - leftStart) / NANO_TO_SECONDS);
                    leftLimbY.add(leftY[i]);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_graph, container, false);

        XYPlot plot = v.findViewById(R.id.plot);

        // initialize the plot

        PanZoom.attach(plot);

        // turn the above arrays into XYSeries
        if (rightLimbX.size() > 0) {
            XYSeries rightLimb = new SimpleXYSeries(rightLimbX, rightLimbY, "Right Arm");
            LineAndPointFormatter rightLimbFormat = new LineAndPointFormatter(getContext(),
                    R.xml.right_limb_point_formatter);
            plot.addSeries(rightLimb, rightLimbFormat);
        }

        if (leftLimbX.size() > 0) {
            XYSeries leftLimb = new SimpleXYSeries(leftLimbX, leftLimbY, "Left Arm");
            // create formatters to use for drawing a series using LineAndPointRenderer
            // and configure them from xml:
            LineAndPointFormatter leftLimbFormat =
                    new LineAndPointFormatter(getContext(), R.xml.line_point_formatter);
            // add a new series' to the xyplot:
            plot.addSeries(leftLimb, leftLimbFormat);
        }

        // display more decimals on range
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT);

        v.findViewById(R.id.export_button).setOnClickListener(view -> exportData());

        return v;
    }

    public void exportData() {
        StringBuilder data = new StringBuilder();
        data.append("Limb,Time,Value");
        for (int i = 0; i < leftLimbX.size(); i++) {
            data.append("\n" + ReadingLimb.LEFT_ARM.name() + "," + leftLimbX.get(i) + "," + leftLimbY.get(i));
        }
        for (int i = 0; i < rightLimbX.size(); i++) {
            data.append("\n" + ReadingLimb.RIGHT_ARM.name() + "," + rightLimbX.get(i) + "," + rightLimbX.get(i));
        }
        String fileSuffix = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
        String filename = "export_" + fileSuffix + ".csv";
        try {
            FileOutputStream out = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
            out.write(data.toString().getBytes());
            out.close();

            Context context = this.getContext().getApplicationContext();
            File file = new File(this.getContext().getFilesDir(), filename);
            Uri path = FileProvider.getUriForFile(context, "com.example.projectlimbrescue.fileprovider", file);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setDataAndType(path, "text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT,"PLR Export Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Export PLR Data"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}