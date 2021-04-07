package com.example.projectlimbrescue;

import android.content.Intent;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class HistoryFragment extends Fragment {

    public static final String LEFT_X_VALUES = "LEFT_X_VALUES";
    public static final String LEFT_Y_VALUES = "LEFT_Y_VALUES";
    public static final String RIGHT_X_VALUES = "RIGHT_X_VALUES";
    public static final String RIGHT_Y_VALUES = "RIGHT_Y_VALUES";

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

        // launch graph activity to display session
        @Override
        public void onClick(View v) {
            List<Long> xValsLeft = new LinkedList<>();
            List<Double> yValsLeft = new LinkedList<>();

            List<Long> xValsRight = new LinkedList<>();
            List<Double> yValsRight = new LinkedList<>();

            final long startTime = mSession.session.startTime.getTime();
            // put times into xVals
            for (Reading r : mSession.readings) {
                if (r.limb == ReadingLimb.LEFT_ARM) {
                    xValsLeft.add(r.time - startTime);
                    yValsLeft.add(r.value);
                } else {
                    xValsRight.add(r.time - startTime);
                    yValsRight.add(r.value);
                }
            }

            // convert to regular arrays to send to activity
            long[] xLeft = new long[xValsLeft.size()];
            double[] yLeft = new double[yValsLeft.size()];
            long[] xRight = new long[xValsRight.size()];
            double[] yRight = new double[xValsRight.size()];

            // left
            for (int i = 0; i < xValsLeft.size(); i++) {
                xLeft[i] = xValsLeft.get(i);
                yLeft[i] = yValsLeft.get(i);
            }

            // right
            for (int i = 0; i < xValsRight.size(); i++) {
                xRight[i] = xValsRight.get(i);
                yRight[i] = yValsRight.get(i);
            }

            Intent intent = new Intent(getContext(), GraphActivity.class);
            if (xLeft.length > 0) {
                intent.putExtra(LEFT_X_VALUES, xLeft);
                intent.putExtra(LEFT_Y_VALUES, yLeft);
            }
            if (xRight.length > 0) {
                intent.putExtra(RIGHT_X_VALUES, xRight);
                intent.putExtra(RIGHT_Y_VALUES, yRight);
            }
            startActivity(intent);
        }
    }

    // Adapter class

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryHolder> {
        // all sessions
        private List<SessionWithReadings> mAllSessions;
        // sessions displayed, potentially filtered
        private List<SessionWithReadings> mSessions;
        // comparator used for sorting
        private Comparator<SessionWithReadings> comparator;
        // TODO: split up left/right sessions in constructor (when DB is updated)

        public HistoryAdapter(List<SessionWithReadings> sessions) {
            mAllSessions = sessions;
            mSessions = new LinkedList<>(mAllSessions);
            setSortDateMostRecent();
            sort();
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

        // TODO: use getSessionWithDevices and use limb field there

        // filters the list to only show sessions from one limb
        public void filterLimb(ReadingLimb limb) {
            mSessions = new LinkedList<>(mAllSessions);
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

            sort();
            // let adapter know data changed to adjust UI
            notifyDataSetChanged();
        }

        public void filterBothLimbs() {
            mSessions = new LinkedList<>(mAllSessions);
            // going backwards means we can delete without skipping elements
            for (int i = mSessions.size() - 1; i >= 0; i--) {
                // keep the session if it has both left and right limb readings
                List<Reading> readings = mSessions.get(i).readings;
                boolean hasLeft = false;
                boolean hasRight = false;
                for (Reading r : readings) {
                    if (r.limb == ReadingLimb.LEFT_ARM) {
                        hasLeft = true;
                    }
                    if (r.limb == ReadingLimb.RIGHT_ARM) {
                        hasRight = true;
                    }
                    // if it has both we keep this and go to the next session
                    if (hasLeft && hasRight) {
                        break;
                    }
                }

                // if it's missing a limb we throw it out
                if (!hasLeft || !hasRight) {
                    mSessions.remove(i);
                }
            }

            sort();
            // let adapter know data changed to adjust UI
            notifyDataSetChanged();
        }

        // shows all sessions from any and all limbs
        public void filterNone() {
            mSessions = new LinkedList<>(mAllSessions);
            sort();
            // let adapter know data changed to adjust UI
            notifyDataSetChanged();
        }

        private void setSortDateMostRecent() {
            comparator = (o1, o2) -> o2.session.startTime.compareTo(o1.session.startTime);
        }

        private void setSortDateEarliest() {
            comparator = (o1, o2) -> o1.session.startTime.compareTo(o2.session.startTime);
        }

        public void sort() {
            Collections.sort(mSessions, comparator);
        }
    }

    private RecyclerView mHistoryRecyclerView;
    private HistoryAdapter mAdapter;
    private Spinner mLimbFilterSpinner;
    private Spinner mDateSortSpinner;
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
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.filter_array, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLimbFilterSpinner.setAdapter(filterAdapter);
        mLimbFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                        // all limbs
                        mAdapter.filterNone();
                        break;
                    case 1:
                        mAdapter.filterBothLimbs();
                        break;
                    case 2:
                        // left limb
                        mAdapter.filterLimb(ReadingLimb.LEFT_ARM);
                        break;
                    case 3:
                        // right limb
                        mAdapter.filterLimb(ReadingLimb.RIGHT_ARM);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        // set up sorting
        mDateSortSpinner = view.findViewById(R.id.history_date_sort);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.sort_array, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDateSortSpinner.setAdapter(sortAdapter);
        mDateSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mAdapter.setSortDateMostRecent();
                        mAdapter.sort();
                        mAdapter.notifyDataSetChanged();
                        break;
                    case 1:
                        mAdapter.setSortDateEarliest();
                        mAdapter.sort();
                        mAdapter.notifyDataSetChanged();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        // set the UI
        updateUI();

        // Inflate the layout for this fragment
        return view;
    }

    private void updateUI() {
        db = DatabaseSingleton.getInstance(getContext());
        SessionDao sessionDao = db.sessionDao();
        List<SessionWithReadings> sessions = sessionDao.getSessionsWithReadings();

        // sort sessions by most recent before giving it to the adapter
        //Collections.sort(sessions,
                //(o1, o2) -> o2.session.startTime.compareTo(o1.session.startTime));

        // TODO: get sensors and readings, look at limbs in sensors

        mAdapter = new HistoryAdapter(sessions);
        mHistoryRecyclerView.setAdapter(mAdapter);
    }

}