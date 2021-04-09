package com.example.projectlimbrescue;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
import com.example.projectlimbrescue.db.session.SessionDao;
import com.example.projectlimbrescue.db.session.SessionWithDevices;
import com.example.shared.ReadingLimb;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
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
        private final TextView mSessionIDTextView;
        private final TextView mDateTextView;
        private SessionWithDevices mSession;

        public HistoryHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_history, parent, false));
            itemView.setOnClickListener(this);

            mSessionIDTextView = itemView.findViewById(R.id.history_id);
            mDateTextView = itemView.findViewById(R.id.history_date);
        }

        public void bind(SessionWithDevices session) {
            mSession = session;

            String armText = "";
            try {
                List<Reading> leftReadings = db.readingDao().getReadingsForSessionIdAndLimb(mSession.session.sessionId,
                        ReadingLimb.LEFT_ARM).get();
                List<Reading> rightReadings = db.readingDao().getReadingsForSessionIdAndLimb(mSession.session.sessionId,
                        ReadingLimb.RIGHT_ARM).get();
                if(leftReadings.size() > 0 && rightReadings.size() > 0) {
                    armText = "Both limbs";
                } else if (leftReadings.size() > 0) {
                    armText = ReadingLimb.LEFT_ARM.toString();
                } else {
                    armText = ReadingLimb.RIGHT_ARM.toString();
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
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
        }
    }

    // Adapter class

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryHolder> {
        // all sessions
        private final List<SessionWithDevices> mAllSessions;
        // sessions from both limbs
        private final List<SessionWithDevices> mSessionsBoth;
        // sessions from left limb
        private final List<SessionWithDevices> mSessionsLeft;
        // sessions from right limb
        private final List<SessionWithDevices> mSessionsRight;
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
                try {
                    List<Reading> leftReadings = db.readingDao().getReadingsForSessionIdAndLimb(s.session.sessionId,
                            ReadingLimb.LEFT_ARM).get();
                    List<Reading> rightReadings = db.readingDao().getReadingsForSessionIdAndLimb(s.session.sessionId,
                            ReadingLimb.RIGHT_ARM).get();
                    if(leftReadings.size() > 0 && rightReadings.size() > 0) {
                        mSessionsBoth.add(s);
                    } else if (leftReadings.size() > 0) {
                        mSessionsLeft.add(s);
                    } else {
                        mSessionsRight.add(s);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
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
            mSessions.sort(mComparator);
        }
    }

    private RecyclerView mHistoryRecyclerView;
    private HistoryAdapter mAdapter;
    private AppDatabase db;

    public HistoryFragment() {
        // Required empty public constructor
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
        Spinner mLimbFilterSpinner = view.findViewById(R.id.history_limb_filter);
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
        Spinner mDateSortSpinner = view.findViewById(R.id.history_date_sort);
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
        // Create a temporary blank list
        // TODO: Maybe implement a spinner for while this is loading.
        mAdapter = new HistoryAdapter(new ArrayList<>());
        mHistoryRecyclerView.setAdapter(mAdapter);

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