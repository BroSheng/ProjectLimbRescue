package com.example.projectlimbrescue;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

import com.example.myapplication.R;

/*
 * TODO: Wearable activity is deprecated. We can transition away from it, but it will take some
 *  work.
 */
public class MainActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView mTextView = (TextView) findViewById(R.id.text);
    }
}