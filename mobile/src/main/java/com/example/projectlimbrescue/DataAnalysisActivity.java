package com.example.projectlimbrescue;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.projectlimbrescue.db.AppDatabase;
import com.example.projectlimbrescue.db.DatabaseSingleton;
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.session.SessionDao;
import com.example.projectlimbrescue.db.session.SessionWithReadings;
import com.example.shared.ReadingLimb;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DataAnalysisActivity extends AppCompatActivity {

    private void meanAverage(List<Double> values) {
        final int WINDOW_SIZE = 3;

        Queue<Double> rollingValues = new LinkedList<>();
        double rollingMean = 0.0;

        for (int i = 0; i < values.size(); i++) {
            rollingValues.add(values.get(i));
            rollingMean += values.get(i);
            if (rollingValues.size() > WINDOW_SIZE) {
                rollingMean -= rollingValues.remove();
            }
            values.set(i, rollingMean / WINDOW_SIZE);
        }
    }

    private void smoothData(long[] time, double[] value) {
        if (BuildConfig.DEBUG && !(time.length == value.length)) {
            throw new AssertionError("Assertion failed");
        }

        long startTime = time[0];
        double startValue = value[0];

        List<Long> lowFreqTime = new ArrayList<>();
        List<Double> lowFreqValue = new ArrayList<>();

        for (int i = 0; i < time.length; i++) {
            time[i] -= startTime;
            value[i] -= startValue;

            if (i % 10 == 0) {
                lowFreqTime.add(time[i]);
                lowFreqValue.add(value[i]);
            }
        }

        meanAverage(lowFreqValue);

        for (int i = 0; i < value.length; i++) {
            value[i] = value[i] - lowFreqValue.get(i / 10);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_analysis);

        long sessionId = getIntent().getLongExtra("SESSION_ID", 0);

        AppDatabase db = DatabaseSingleton.getInstance(getApplicationContext());
        SessionDao sessionDao = db.sessionDao();

        // get session with readings
        long[] sessionIds = { sessionId };
        List<SessionWithReadings> sessions = sessionDao.getSessionsWithReadingsByIds(sessionIds);
        SessionWithReadings recentSession = sessions.get(0);

        // TODO: Implement a database view for this logic
        List<Reading> rightArm = new ArrayList<>();
        List<Reading> leftArm = new ArrayList<>();

        for(Reading reading : recentSession.readings) {
            if(reading.limb == ReadingLimb.RIGHT_ARM) {
                rightArm.add(reading);
            } else if (reading.limb == ReadingLimb.LEFT_ARM) {
                leftArm.add(reading);
            }
        }


        // turn readings into x and y arrays
        long[] rightTime = new long[rightArm.size()];
        double[] rightValue = new double[rightArm.size()];

        // fill the values
        for (int i = 0; i < rightTime.length; i++) {
            rightTime[i] = rightArm.get(i).time;
            rightValue[i] = rightArm.get(i).value;
        }

        smoothData(rightTime, rightValue);

        // turn readings into x and y arrays
        long[] leftTime = new long[leftArm.size()];
        double[] leftValue = new double[leftArm.size()];

        // fill the values
        for (int i = 0; i < leftTime.length; i++) {
            leftTime[i] = leftArm.get(i).time;
            leftValue[i] = leftArm.get(i).value;
        }

        smoothData(leftTime, leftValue);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container_view);

        // initialize GraphFragment
        if (fragment == null) {
            Bundle bundle = new Bundle();
            bundle.putLongArray(GraphFragment.RIGHT_LIMB_X, rightTime);
            bundle.putDoubleArray(GraphFragment.RIGHT_LIMB_Y, rightValue);
            bundle.putLongArray(GraphFragment.LEFT_LIMB_X, leftTime);
            bundle.putDoubleArray(GraphFragment.LEFT_LIMB_Y, leftValue);
            fragment = new GraphFragment();
            fragment.setArguments(bundle);
            fm.beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, fragment)
                    .commit();
        }
    }
}
