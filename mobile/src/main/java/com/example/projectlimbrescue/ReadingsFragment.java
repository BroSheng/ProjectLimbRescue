package com.example.projectlimbrescue;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.projectlimbrescue.db.AppDatabase;
import com.example.projectlimbrescue.db.DatabaseSingleton;
import com.example.projectlimbrescue.db.session.Session;
import com.example.projectlimbrescue.db.session.SessionDao;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ReadingsFragment extends Fragment implements DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener {

    enum RecordButtonState {
        READY,
        PREPARING,
        RECORDING,
        WAITING
    }

    private static final String TAG = "Readings";

    TextView mSendStartMessageBtn;
    RecordButtonState recordState = RecordButtonState.READY;
    TextView mReadingHint;
    BottomNavigationView mNavBar;

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String SENSOR_PATH = "/sensor";
    private static final String SESSION_KEY = "session";

    private static final int PREPARE_TIME = 3;
    private static final int RECORDING_TIME = 30;
    private static final int DATA_GRACE_PERIOD = 3000;

    private AppDatabase db;

    private int nodesRequiredInSession = 0;
    private final List<JSONObject> readingSessions = new ArrayList<>();
    private long startTimeNano = 0;

    private Drawable preparingBackground;
    private Drawable recordBackground;
    private Drawable recordingBackground;
    private Drawable emptyTickerBackground;
    private Drawable prepareEmptyTickerBackground;

    private Timer prepareTimer;
    private Timer recordTimer;
    private Timer waitForDataTimer;

    private TextView stopButton;

    private RelativeLayout recordingTickerContainer;
    private final List<ImageView> recordingTickers = new ArrayList<>();
    private final int[] tickerNames = {
            R.id.recordingTicker0,
            R.id.recordingTicker1,
            R.id.recordingTicker2,
            R.id.recordingTicker3,
            R.id.recordingTicker4,
            R.id.recordingTicker5,
            R.id.recordingTicker6,
            R.id.recordingTicker7,
            R.id.recordingTicker8,
            R.id.recordingTicker9,
            R.id.recordingTicker10,
            R.id.recordingTicker11,
            R.id.recordingTicker12,
            R.id.recordingTicker13,
            R.id.recordingTicker14,
            R.id.recordingTicker15,
            R.id.recordingTicker16,
            R.id.recordingTicker17,
            R.id.recordingTicker18,
            R.id.recordingTicker19,
            R.id.recordingTicker20,
            R.id.recordingTicker21,
            R.id.recordingTicker22,
            R.id.recordingTicker23,
            R.id.recordingTicker24,
            R.id.recordingTicker25,
            R.id.recordingTicker26,
            R.id.recordingTicker27,
            R.id.recordingTicker28,
            R.id.recordingTicker29};

    private RelativeLayout prepareTickerContainer;
    private final List<ImageView> prepareTickers = new ArrayList<>();
    private final int[] prepareNames = {
            R.id.prepareTicker0,
            R.id.prepareTicker1,
            R.id.prepareTicker2
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        db = DatabaseSingleton.getInstance(getActivity().getApplicationContext());

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_readings, container, false);
        mSendStartMessageBtn = v.findViewById(R.id.start_reading);
        mSendStartMessageBtn.setOnClickListener((View view) -> recordButtonOnClick());
        preparingBackground = ResourcesCompat.getDrawable(
                getResources(),
                R.drawable.preparing_button,
                getActivity().getTheme());
        recordBackground = ResourcesCompat.getDrawable(
                getResources(),
                R.drawable.record_button,
                getActivity().getTheme());
        recordingBackground = ResourcesCompat.getDrawable(
                getResources(),
                R.drawable.recording_button,
                getActivity().getTheme());
        emptyTickerBackground = ResourcesCompat.getDrawable(
                getResources(),
                R.drawable.empty_button,
                getActivity().getTheme());
        prepareEmptyTickerBackground = ResourcesCompat.getDrawable(
                getResources(),
                R.drawable.preparing_empty_button,
                getActivity().getTheme());

        recordingTickerContainer = v.findViewById(R.id.recordingTickers);
        for (int ticker : tickerNames) {
            recordingTickers.add(v.findViewById(ticker));
        }

        prepareTickerContainer = v.findViewById(R.id.prepareTickers);
        for (int ticker : prepareNames) {
            prepareTickers.add(v.findViewById(ticker));
        }

        mReadingHint = v.findViewById(R.id.reading_hint);
        mNavBar = getActivity().findViewById(R.id.bottom_navigation);

        stopButton = v.findViewById(R.id.stop_button);
        stopButton.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.setPressed(true);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (recordState == RecordButtonState.PREPARING) {
                    prepareTimer.cancel();
                    prepareTickerContainer.setVisibility(View.INVISIBLE);
                    mSendStartMessageBtn.setBackground(recordBackground);
                    mSendStartMessageBtn.setText(R.string.record);

                    getActivity().runOnUiThread(() -> {
                        BottomNavigationView navBar = mNavBar;
                        ObjectAnimator animation = ObjectAnimator.ofFloat(navBar, "translationY", 0f);
                        animation.setDuration(200);
                        animation.start();
                    });
                    stopButton.setVisibility(View.INVISIBLE);

                    recordState = RecordButtonState.READY;
                } else {
                    recordTimer.cancel();
                    finishRecording();
                }
                view.setPressed(false);
                return false;
            }
            return false;
        });

        return v;
    }

    private void recordButtonOnClick() {
        if (recordState == RecordButtonState.READY) {
            stopButton.setVisibility(View.VISIBLE);
            recordState = RecordButtonState.PREPARING;
            prepareToRecord();
        }
    }

    private void prepareToRecord() {
        ObjectAnimator animation = ObjectAnimator.ofFloat(mNavBar, "translationY",
                100f * getResources().getDisplayMetrics().scaledDensity);
        animation.setDuration(200);
        animation.start();

        mReadingHint.setVisibility(View.VISIBLE);

        mSendStartMessageBtn.setBackground(preparingBackground);
        for (ImageView ticker : prepareTickers) {
            ticker.setImageDrawable(prepareEmptyTickerBackground);
        }
        prepareTickerContainer.setVisibility(View.VISIBLE);
        prepareTimer = new Timer();
        prepareTimer.scheduleAtFixedRate(new CountDownTimer(prepareTimer), 0, 1000);
    }

    private void beginRecording() {
        getActivity().runOnUiThread(() -> {
            prepareTickerContainer.setVisibility(View.INVISIBLE);
            mSendStartMessageBtn.setText(getString(R.string.recording));
            mSendStartMessageBtn.setBackground(recordingBackground);
            for (ImageView ticker : recordingTickers) {
                ticker.setImageDrawable(emptyTickerBackground);
            }
            recordingTickerContainer.setVisibility(View.VISIBLE);
        });

        recordTimer = new Timer();
        recordTimer.scheduleAtFixedRate(new RecordTimer(recordTimer), 0, 1000);
        onStartWearableActivityClick();
    }

    private class RecordTimer extends TimerTask {
        Timer timer;
        int time = RECORDING_TIME;

        public RecordTimer(Timer timer) {
            this.timer = timer;
        }

        @Override
        public void run() {
            int currentTime = time;
            getActivity().runOnUiThread(() -> {
                int index = RECORDING_TIME - currentTime;
                if (index < recordingTickers.size()) {
                    recordingTickers.get(index).setImageDrawable(recordBackground);
                }
            });
            time--;
            if (time <= 0) {
                timer.cancel();
                getActivity().runOnUiThread(() -> mReadingHint.setVisibility(View.INVISIBLE));
                finishRecording();
            }
        }
    }

    private void finishRecording() {
        stopButton.setVisibility(View.INVISIBLE);
        recordState = RecordButtonState.WAITING;
        mSendStartMessageBtn.setText(R.string.waiting);
        waitForDataTimer = new Timer();
        waitForDataTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new ReadingsFragment.StoreReadingsTask().start();
            }
        }, DATA_GRACE_PERIOD);
        onStartWearableActivityClick();
    }

    private class CountDownTimer extends TimerTask {
        Timer timer;
        int time = PREPARE_TIME;

        public CountDownTimer(Timer timer) {
            this.timer = timer;
        }

        @Override
        public void run() {
            int currentTime = time;
            getActivity().runOnUiThread(() -> {
                mSendStartMessageBtn.setText(Integer.toString(currentTime));

                int index = PREPARE_TIME - currentTime;
                if (index < prepareTickers.size()) {
                    prepareTickers.get(index).setImageDrawable(preparingBackground);
                }
            });
            time--;
            if (time < 0) {
                timer.cancel();
                recordState = RecordButtonState.RECORDING;
                beginRecording();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Activity a = this.getActivity();
        Wearable.getDataClient(a).addListener(this);
        Wearable.getMessageClient(a).addListener(this);
        Wearable.getCapabilityClient(a)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
    }

    @Override
    public void onPause() {
        super.onPause();

        Activity a = this.getActivity();
        Wearable.getDataClient(a).removeListener(this);
        Wearable.getMessageClient(a).removeListener(this);
        Wearable.getCapabilityClient(a).removeListener(this);
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: " + capabilityInfo);
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged(): " + dataEventBuffer);

        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = event.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();

                if (path.compareTo(SENSOR_PATH) == 0) {
                    DataMapItem item = DataMapItem.fromDataItem(dataItem);
                    DataMap dm = item.getDataMap();
                    ByteArrayInputStream bis = new ByteArrayInputStream(dm.getByteArray(SESSION_KEY));

                    // Convert bytestream to JSON string
                    int n = bis.available();
                    byte[] bytes = new byte[n];
                    bis.read(bytes, 0, n);
                    String jsonString = new String(bytes, StandardCharsets.UTF_8);

                    try {
                        readingSessions.add(new JSONObject(jsonString));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }


        }
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);
    }

    public void onStartWearableActivityClick() {
        Log.d(TAG, "Generating RPC");

        new ReadingsFragment.StartWearableActivityTask().start();
    }

    @WorkerThread
    private void sendStartActivityMessage(String node) {
        Instant now = Instant.now();
        this.startTimeNano = now.getEpochSecond() * 1000000000 + now.getNano();
        Task<Integer> sendMessageTask = Wearable.getMessageClient(getActivity())
                .sendMessage(node, START_ACTIVITY_PATH, longToByteArr(this.startTimeNano));

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            Integer result = Tasks.await(sendMessageTask);
            Log.d(TAG, "Message sent: " + result);

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed on " + node + ": " + exception);

        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }
    }

    @WorkerThread
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();

        Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(getActivity().getApplicationContext()).getConnectedNodes();

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            List<Node> nodes = Tasks.await(nodeListTask);

            for (Node node : nodes) {
                results.add(node.getId());
            }

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }

        return results;
    }

    private byte[] longToByteArr(long time) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(time);
        return buffer.array();
    }

    private class StartWearableActivityTask extends Thread {
        @Override
        public void run() {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendStartActivityMessage(node);
            }
            readingSessions.clear();
            nodesRequiredInSession = nodes.size();
        }
    }

    private class StoreReadingsTask extends Thread {
        @Override
        public void run() {
            Looper.prepare();
            SessionDao sessionAccess = db.sessionDao();

            Session session = new Session();
            // Timestamps need to be in milliseconds
            session.startTime = new Timestamp(startTimeNano / 1000000L);
            session.endTime = new Timestamp(Instant.now().toEpochMilli());

            ListeningExecutorService service =
                    MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
            ListenableFuture<long[]> sessionIdFuture = sessionAccess.insert(session);
            sessionIdFuture.addListener(() -> {
                long sessionId = 0;
                try {
                    sessionId = sessionIdFuture.get()[0];
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

                for (JSONObject obj : readingSessions) {
                    try {
                        JsonToDb.InsertJson(obj, sessionId, db);
                    } catch (JSONException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                mSendStartMessageBtn.setText(getResources().getString(R.string.record));
                mSendStartMessageBtn.setBackground(recordBackground);
                recordingTickerContainer.setVisibility(View.INVISIBLE);

                getActivity().runOnUiThread(() -> {
                    BottomNavigationView navBar = getActivity().findViewById(R.id.bottom_navigation);
                    ObjectAnimator animation = ObjectAnimator.ofFloat(navBar, "translationY", 0f);
                    animation.setDuration(200);
                    animation.start();
                });

                recordState = RecordButtonState.READY;

                Intent intent = new Intent(getActivity().getBaseContext(), DataAnalysisActivity.class);
                intent.putExtra("SESSION_ID", sessionId);
                startActivity(intent);
            }, service);
        }
    }
}