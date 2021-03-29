package com.example.projectlimbrescue;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.projectlimbrescue.db.AppDatabase;
import com.example.projectlimbrescue.db.DatabaseSingleton;
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.session.Session;
import com.example.projectlimbrescue.db.session.SessionDao;
import com.example.projectlimbrescue.db.session.SessionWithReadings;
import com.example.shared.ReadingLimb;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    // ViewHolder subclass for HistoryFragment RecyclerView
    private class HistoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mSessionID;
        private TextView mLimbTextView;
        private SessionWithReadings mSession;

        public HistoryHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_history, parent, false));
            itemView.setOnClickListener(this);

            mSessionID = itemView.findViewById(R.id.history_id);
            mLimbTextView = itemView.findViewById(R.id.history_limb);
        }

        public void bind(SessionWithReadings session) {
            mSession = session;
            String sessionText = "Session ID " + session.session.sessionId;
            mSessionID.setText(sessionText);
            String limb = "Limb: " + session.readings.get(0).limb.name();
            mLimbTextView.setText(limb);
        }

        @Override
        public void onClick(View v) {
            List<Reading> leftReadings = new ArrayList<>();
            List<Reading> rightReadings = new ArrayList<>();

            // add readings into appropriate lists
            for (Reading reading : mSession.readings) {
                if (reading.limb == ReadingLimb.LEFT_ARM) {
                    leftReadings.add(reading);
                } else if (reading.limb == ReadingLimb.RIGHT_ARM) {
                    rightReadings.add(reading);
                }
            }

            // put x and y values into arrays
            long[] leftTime = new long[leftReadings.size()];
            double[] leftValue = new double[leftReadings.size()];
            long[] rightTime = new long[rightReadings.size()];
            double[] rightValue = new double[rightReadings.size()];

            // left readings
            for (int i = 0; i < leftReadings.size(); i++) {
                leftTime[i] = leftReadings.get(i).time;
                leftValue[i] = leftReadings.get(i).value;
            }

            // right readings
            for (int i = 0; i < rightReadings.size(); i++) {
                rightTime[i] = rightReadings.get(i).time;
                rightValue[i] = rightReadings.get(i).value;
            }

            final FragmentTransaction ft = getParentFragmentManager().beginTransaction();
            // pass bundle to graph
            Bundle bundle = new Bundle();
            bundle.putLongArray(GraphFragment.RIGHT_LIMB_X, rightTime);
            bundle.putDoubleArray(GraphFragment.RIGHT_LIMB_Y, rightValue);
            bundle.putLongArray(GraphFragment.LEFT_LIMB_X, leftTime);
            bundle.putDoubleArray(GraphFragment.LEFT_LIMB_Y, leftValue);

            ft.replace(R.id.fragment_container_view, GraphFragment.class, bundle);

            ft.setReorderingAllowed(true);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    // Adapter class
    private class HistoryAdapter extends RecyclerView.Adapter<HistoryHolder> {
        private List<SessionWithReadings> mSessions;

        public HistoryAdapter(List<SessionWithReadings> sessions) {
            mSessions = sessions;
        }

        @NonNull
        @Override
        public HistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new HistoryHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryHolder holder, int position) {
            SessionWithReadings session = mSessions.get(position);
            holder.bind(session);
        }

        @Override
        public int getItemCount() {
            return mSessions.size();
        }
    }

    private RecyclerView mHistoryRecyclerView;
    private HistoryAdapter mAdapter;
    private AppDatabase db;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        mHistoryRecyclerView = view.findViewById(R.id.history_recycler_view);
        mHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // set the UI
        updateUI();

        // Inflate the layout for this fragment
        return view;
    }

    private void updateUI() {
        db = DatabaseSingleton.getInstance(getContext());
        SessionDao sessionDao = db.sessionDao();
        List<SessionWithReadings> sessions = sessionDao.getSessionsWithReadings();

        mAdapter = new HistoryAdapter(sessions);
        mHistoryRecyclerView.setAdapter(mAdapter);
    }

}