package com.example.projectlimbrescue;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.projectlimbrescue.db.AppDatabase;
import com.example.projectlimbrescue.db.DatabaseSingleton;
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.reading.ReadingDao;
import com.example.shared.ReadingLimb;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class DataAnalysisActivity extends AppCompatActivity {

    private ProgressBar spinner;
    private void meanAverage(List<Double> values) {
        final int WINDOW_SIZE = 1;

        for (int i = WINDOW_SIZE; i < values.size() - WINDOW_SIZE; i++) {
            double value = values.get(i);
            for (int k = 0; k < WINDOW_SIZE; k++) {
                value += values.get(i - k);
                value += values.get(i + k);
            }
            values.set(i, value / (WINDOW_SIZE * 2 + 1));
        }
    }

    private void smoothData(long[] time, double[] value) {
        if (BuildConfig.DEBUG && !(time.length == value.length)) {
            throw new AssertionError("Assertion failed");
        }

        long startTime = time[0];
        double startValue = value[0];

        List<Double> lowFreqValue = new ArrayList<>();

        for (int i = 0; i < time.length; i++) {
            time[i] -= startTime;
            value[i] -= startValue;

            if (i % 10 == 0) {
                lowFreqValue.add(value[i]);
            }
        }

        lowFreqValue.add(value[value.length - 1]);

        meanAverage(lowFreqValue);

        for (int i = 0; i < value.length; i++) {
            int index = Math.min((i + value.length / 20) / 10, lowFreqValue.size() - 1);
            value[i] -= lowFreqValue.get(index);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_analysis);

        spinner = findViewById(R.id.progressBar1);

        long sessionId = getIntent().getLongExtra("SESSION_ID", 0);

        AppDatabase db = DatabaseSingleton.getInstance(getApplicationContext());
        ReadingDao readingDao = db.readingDao();

        // get readings from session id for each limb

        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        ListenableFuture<List<Reading>> leftReadingsFuture = readingDao.getReadingsForSessionIdAndLimb(sessionId, ReadingLimb.LEFT_ARM);
        ListenableFuture<List<Reading>> rightReadingsFuture = readingDao.getReadingsForSessionIdAndLimb(sessionId, ReadingLimb.RIGHT_ARM);

        ListenableFuture<List<List<Reading>>> bothReadingsFuture = Futures.allAsList(leftReadingsFuture, rightReadingsFuture);
        bothReadingsFuture.addListener(() -> {
            try {
                List<Reading> leftArm = bothReadingsFuture.get().get(0);
                List<Reading> rightArm = bothReadingsFuture.get().get(1);

                // turn readings into x and y arrays
                long[] rightTime = null;
                double[] rightValue = null;

                if (rightArm.size() > 0) {
                    rightTime = new long[rightArm.size()];
                    rightValue = new double[rightArm.size()];

                    // fill the values
                    for (int i = 0; i < rightTime.length; i++) {
                        rightTime[i] = rightArm.get(i).time;
                        rightValue[i] = rightArm.get(i).value;
                    }

                    smoothData(rightTime, rightValue);
                }

                long[] leftTime = null;
                double[] leftValue = null;

                if (leftArm.size() > 0) {
                    // turn readings into x and y arrays
                    leftTime = new long[leftArm.size()];
                    leftValue = new double[leftArm.size()];

                    // fill the values
                    for (int i = 0; i < leftTime.length; i++) {
                        leftTime[i] = leftArm.get(i).time;
                        leftValue[i] = leftArm.get(i).value;
                    }

                    smoothData(leftTime, leftValue);
                }

                runOnUiThread(() -> spinner.setVisibility(View.INVISIBLE));

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
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, service);
        spinner.setVisibility(View.VISIBLE);
    }
}
