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
import com.example.projectlimbrescue.db.device.Device;
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.reading.ReadingDao;
import com.example.projectlimbrescue.db.session.SessionDao;
import com.example.projectlimbrescue.db.session.SessionWithDevices;
import com.example.projectlimbrescue.db.session.SessionWithReadings;
import com.example.shared.ReadingLimb;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class HistoryFragment extends Fragment {

    public static final String LEFT_X_VALUES = "LEFT_X_VALUES";
    public static final String LEFT_Y_VALUES = "LEFT_Y_VALUES";
    public static final String RIGHT_X_VALUES = "RIGHT_X_VALUES";
    public static final String RIGHT_Y_VALUES = "RIGHT_Y_VALUES";

    // ViewHolder subclass for HistoryFragment RecyclerView
    private class HistoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mSessionIDTextView;
        private TextView mDateTextView;
        private SessionWithDevices mSession;

        public HistoryHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_history, parent, false));
            itemView.setOnClickListener(this);

            mSessionIDTextView = itemView.findViewById(R.id.history_id);
            mDateTextView = itemView.findViewById(R.id.history_date);
        }

        public void bind(SessionWithDevices session) {
            mSession = session;

            String armText;
            if (mSession.devices.size() > 1) {
                armText = "Both limbs";
            } else {
                armText = mSession.devices.get(0).limb.toString();
            }

            mSessionIDTextView.setText(armText);
            String sessionDate = session.session.startTime.toString();
            mDateTextView.setText(sessionDate);
        }

        // launch graph activity to display session
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), DataAnalysisActivity.class);
            intent.putExtra("SESSION_ID", mSession.session.sessionId);
            startActivity(intent);

            // get a sessionWithReadings from database
//            long[] sessionID = {mSession.session.sessionId};
//
//            AppDatabase db = DatabaseSingleton.getInstance(getContext());
//            ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
//
//            ListenableFuture<List<SessionWithReadings>> sessionWithReadingsFuture = db.sessionDao()
//                    .getSessionsWithReadingsByIds(sessionID);
//            sessionWithReadingsFuture.addListener(() -> {
//                List<SessionWithReadings> sessionWithReadings = null;
//                try {
//                    sessionWithReadings = sessionWithReadingsFuture.get();
//                } catch (ExecutionException | InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                // TODO: ask Cole for getReadingsBySessionId() to simplify
//
//                // get the readings
//                List<Reading> readings = new LinkedList<>();
//                if (sessionWithReadings.size() > 0) {
//                    readings.addAll(sessionWithReadings.get(0).readings);
//                }
//
//                // left limb x & y values
//                List<Long> xValsLeft = new LinkedList<>();
//                List<Double> yValsLeft = new LinkedList<>();
//
//                // right limb x & y values
//                List<Long> xValsRight = new LinkedList<>();
//                List<Double> yValsRight = new LinkedList<>();
//
//                final long startTime = mSession.session.startTime.getTime();
//                // put times into xVals
//                for (Reading r : readings) {
//                    if (r.limb == ReadingLimb.LEFT_ARM) {
//                        xValsLeft.add(r.time - startTime);
//                        yValsLeft.add(r.value);
//                    } else {
//                        xValsRight.add(r.time - startTime);
//                        yValsRight.add(r.value);
//                    }
//                }
//
//            /*
//                We have to convert the List<> into primitive arrays so we can pass them to
//                the activity
//             */
//
//                long[] xLeft = new long[xValsLeft.size()];
//                double[] yLeft = new double[yValsLeft.size()];
//                long[] xRight = new long[xValsRight.size()];
//                double[] yRight = new double[xValsRight.size()];
//
//                // insert values into left arrays
//                for (int i = 0; i < xValsLeft.size(); i++) {
//                    xLeft[i] = xValsLeft.get(i);
//                    yLeft[i] = yValsLeft.get(i);
//                }
//
//                // insert values into right arrays
//                for (int i = 0; i < xValsRight.size(); i++) {
//                    xRight[i] = xValsRight.get(i);
//                    yRight[i] = yValsRight.get(i);
//                }
//
//                // put arrays into intent
//                Intent intent = new Intent(getContext(), GraphActivity.class);
//                if (xLeft.length > 0) {
//                    intent.putExtra(LEFT_X_VALUES, xLeft);
//                    intent.putExtra(LEFT_Y_VALUES, yLeft);
//                }
//                if (xRight.length > 0) {
//                    intent.putExtra(RIGHT_X_VALUES, xRight);
//                    intent.putExtra(RIGHT_Y_VALUES, yRight);
//                }
//                startActivity(intent);
//            }, service);
        }
    }

    // Adapter class

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryHolder> {
        // all sessions
        private List<SessionWithDevices> mAllSessions;
        // sessions from both limbs
        private List<SessionWithDevices> mSessionsBoth;
        // sessions from left limb
        private List<SessionWithDevices> mSessionsLeft;
        // sessions from right limb
        private List<SessionWithDevices> mSessionsRight;
        // sessions used by the adapter, will be one of the above lists
        private List<SessionWithDevices> mSessions;
        // used to sort sessions
        Comparator<SessionWithDevices> mComparator;

        public HistoryAdapter(List<SessionWithDevices> allSessions) {
            mAllSessions = allSessions;
            mSessions = mAllSessions;

            // pre-filter sessions
            mSessionsBoth = new LinkedList<>();
            mSessionsLeft = new LinkedList<>();
            mSessionsRight = new LinkedList<>();

            for (SessionWithDevices s : mAllSessions) {
                List<Device> devices = s.devices;
                if (devices.size() == 2) {
                    mSessionsBoth.add(s);
                } else if (devices.size() == 1) {
                    if (devices.get(0).limb == ReadingLimb.LEFT_ARM) {
                        mSessionsLeft.add(s);
                    } else if (devices.get(0).limb == ReadingLimb.RIGHT_ARM) {
                        mSessionsRight.add(s);
                    }
                }
            }

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
            SessionWithDevices session = mSessions.get(position);
            holder.bind(session);
        }

        @Override
        public int getItemCount() {
            return mSessions.size();
        }

        // filters the list to only show sessions from one limb
        public void filterLimb(ReadingLimb limb) {
            switch (limb) {
                case LEFT_ARM:
                    mSessions = mSessionsLeft;
                    break;
                case RIGHT_ARM:
                    mSessions = mSessionsRight;
                    break;
                default:
                    break;
            }

            sort();
            // let adapter know data changed to adjust UI
            notifyDataSetChanged();
        }

        public void filterBothLimbs() {
            mSessions = mSessionsBoth;
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
            mComparator = (o1, o2) -> o2.session.startTime.compareTo(o1.session.startTime);
        }

        private void setSortDateEarliest() {
            mComparator = (o1, o2) -> o1.session.startTime.compareTo(o2.session.startTime);
        }

        public void sort() {
            Collections.sort(mSessions, mComparator);
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
                        // both limbs
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
                        // sort by most recent date first
                        mAdapter.setSortDateMostRecent();
                        mAdapter.sort();
                        mAdapter.notifyDataSetChanged();
                        break;
                    case 1:
                        // sort by earliest date first
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
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        ListenableFuture<List<SessionWithDevices>> sessionsFuture = sessionDao.getSessionsWithDevices();
        sessionsFuture.addListener(() -> {
            try {
                List<SessionWithDevices> sessions = sessionsFuture.get();
                this.getActivity().runOnUiThread(() -> {
                    mAdapter = new HistoryAdapter(sessions);
                    mHistoryRecyclerView.setAdapter(mAdapter);
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, service);
    }

}