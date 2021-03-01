package com.example.projectlimbrescue;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shared.Reading;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener {

    private static final String TAG = "MainActivity";

    private Button mSendStartMessageBtn;
    private TextView mTextView;
    private static final String START_ACTIVITY_PATH = "/start-activity";

    private final LinkedList<Reading> readings = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();


    }

    @Override
    protected void onResume() {
        super.onResume();

        Wearable.getDataClient(this).addListener(this);
        Wearable.getMessageClient(this).addListener(this);
        Wearable.getCapabilityClient(this)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
    }//.

    @Override
    protected void onPause() {
        super.onPause();

        Wearable.getDataClient(this).removeListener(this);
        Wearable.getMessageClient(this).removeListener(this);
        Wearable.getCapabilityClient(this).removeListener(this);
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
                if (path.compareTo("/sensor") == 0) {
                    DataMapItem item = DataMapItem.fromDataItem(dataItem);
                    DataMap dm = item.getDataMap();
                    long elapsedTime = dm.getLong("elapsedtime") / 1000;
                    String readTime = "Took " + elapsedTime + " second reading";
                    mTextView.setText(readTime);
                }
            }
//            ByteArrayInputStream bis = new ByteArrayInputStream(event.getDataItem().getData());
//            try {
//                ObjectInput in = new ObjectInputStream(bis);
//                readings = (LinkedList<Reading>) in.readObject();
//                setText();
//            } catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
//            }

        }
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: " + capabilityInfo);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);
    }

    public void onStartWearableActivityClick(View view) {
        Log.d(TAG, "Generating RPC");

        new StartWearableActivityTask().execute();
        String stop = "Stop";
        mSendStartMessageBtn.setText(stop);
        mTextView.setText("");
    }

    @WorkerThread
    private void sendStartActivityMessage(String node) {
        Task<Integer> sendMessageTask = Wearable.getMessageClient(this).sendMessage(node, START_ACTIVITY_PATH, new byte[0]);

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
                Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();

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

    private void setText() {
        if (readings == null) {
            Log.e(TAG, "No readings available");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Reading reading : readings) {
            String r = reading.channel0 + " " + reading.channel1 + " " + reading.time + "\n";
            sb.append(r);
        }
        mTextView.setText(sb);
    }

    private void setupViews() {
        mSendStartMessageBtn = findViewById(R.id.start_activity);
        mTextView = findViewById(R.id.textView);
    }

    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendStartActivityMessage(node);
            }
            return null;
        }
    }
}