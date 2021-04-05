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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.projectlimbrescue.db.AppDatabase;
import com.example.projectlimbrescue.db.DatabaseSingleton;
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.session.SessionDao;
import com.example.projectlimbrescue.db.session.SessionWithReadings;
import com.example.shared.ReadingLimb;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HistoryFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    // ViewHolder subclass for HistoryFragment RecyclerView
    private class HistoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mSessionIDTextView;
        private TextView mDateTextView;
        private SessionWithReadings mSession;

        public HistoryHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_history, parent, false));
            itemView.setOnClickListener(this);

            mSessionIDTextView = itemView.findViewById(R.id.history_id);
            mDateTextView = itemView.findViewById(R.id.history_date);
        }

        public void bind(SessionWithReadings session) {
            mSession = session;
            String sessionText = "Session ID " + session.session.sessionId;
            mSessionIDTextView.setText(sessionText);
            String sessionDate = session.session.startTime.toString();
            mDateTextView.setText(sessionDate);
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
        // master list of sessions
        private List<SessionWithReadings> mSessionsMaster;
        // what's actually displayed & filtered
        private List<SessionWithReadings> mSessions;

        public HistoryAdapter(List<SessionWithReadings> sessions) {
            mSessionsMaster = sessions;
            mSessions = new LinkedList<>(mSessionsMaster);
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

        // filters the list to only show sessions from one limb
        public void filterLimb(ReadingLimb limb) {
            // going backwards means we can delete without skipping elements
            for (int i = mSessions.size() - 1; i >= 0; i--) {
                // if a session contains a reading from the other limb, remove it
                List<Reading> readings = mSessions.get(i).readings;
                for (Reading r : readings) {
                    if (r.limb != limb) {
                        mSessions.remove(i);
                        break;
                    }
                }
            }

            // let adapter know data changed to adjust UI
            notifyDataSetChanged();
        }

        // shows all sessions from any and all limbs
        public void filterNone() {
            mSessions = new LinkedList<>(mSessionsMaster);
            // let adapter know data changed to adjust UI
            notifyDataSetChanged();
        }
    }

    private RecyclerView mHistoryRecyclerView;
    private HistoryAdapter mAdapter;
    private Spinner mLimbFilterSpinner;
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

        // set up limb filter
        mLimbFilterSpinner = view.findViewById(R.id.history_limb_filter);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.filter_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLimbFilterSpinner.setAdapter(adapter);
        mLimbFilterSpinner.setOnItemSelectedListener(this);

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

    // on selection listeners for filter spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch(position) {
            case 0:
                // all limbs
                mAdapter.filterNone();
                break;
            case 1:
                // left limb
                mAdapter.filterLimb(ReadingLimb.LEFT_ARM);
                break;
            case 2:
                // right limb
                mAdapter.filterLimb(ReadingLimb.RIGHT_ARM);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }

}