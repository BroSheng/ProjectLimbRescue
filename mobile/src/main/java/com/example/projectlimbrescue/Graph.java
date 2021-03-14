package com.example.projectlimbrescue;

import android.content.Context;
import android.util.AttributeSet;

import com.androidplot.xy.XYPlot;

public class Graph extends XYPlot {

    public Graph(Context context, String title) {
        super(context, title);
    }

    public Graph(Context context, String title, RenderMode mode) {
        super(context, title, mode);
    }

    public Graph(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    public Graph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
