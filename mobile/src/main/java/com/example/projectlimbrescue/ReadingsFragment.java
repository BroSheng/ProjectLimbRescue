package com.example.projectlimbrescue;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import java.util.concurrent.ExecutionException;

public class ReadingsFragment extends Fragment implements DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener {

    private static final String TAG = "Readings";

    Button mSendStartMessageBtn;

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String SENSOR_PATH = "/sensor";
    private static final String SESSION_KEY = "session";

    private AppDatabase db;

    private int nodesRequiredInSession = 0;
    private final List<JSONObject> readingSessions = new ArrayList<>();
    private long startTime = 0;

    public ReadingsFragment() {
        // Required empty public constructor
    }

    public static ReadingsFragment newInstance() {
        return new ReadingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        db = DatabaseSingleton.getInstance(getActivity().getApplicationContext());

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_readings, container, false);
        mSendStartMessageBtn = v.findViewById(R.id.start_reading);

        return v;
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
                String stop = "Start";
                mSendStartMessageBtn.setText(stop);
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

                    if (readingSessions.size() >= nodesRequiredInSession) {
                        new ReadingsFragment.StoreReadingsTask().start();
                    }
                }
            }


        }
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);
    }

    public void onStartWearableActivityClick(View view) {
        Log.d(TAG, "Generating RPC");

        new ReadingsFragment.StartWearableActivityTask().start();
        String stop = "Stop";
        mSendStartMessageBtn.setText(stop);
    }

    @WorkerThread
    private void sendStartActivityMessage(String node) {
        Instant now = Instant.now();
        this.startTime = now.getEpochSecond() * 1000000000 + now.getNano();
        Task<Integer> sendMessageTask = Wearable.getMessageClient(getActivity())
                .sendMessage(node, START_ACTIVITY_PATH, longToByteArr(this.startTime));

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            Integer result = Tasks.await(sendMessageTask);
            Log.d(TAG, "Message sent: " + result);

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed: " + exception);

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
            Instant now = Instant.now();
            long endTime = now.getEpochSecond() * 1000000000 + now.getNano();
            session.startTime = new Timestamp(startTime);
            session.endTime = new Timestamp(endTime);

            long sessionId = sessionAccess.insert(session)[0];
            for(JSONObject obj : readingSessions) {
                try {
                    JsonToDb.InsertJson(obj, sessionId, db);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Intent intent = new Intent(getActivity().getBaseContext(), DataAnalysisActivity.class);
            intent.putExtra("SESSION_ID", sessionId);
            startActivity(intent);
        }
    }
}