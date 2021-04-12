package com.example.projectlimbrescue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.projectlimbrescue.db.AppDatabase;
import com.example.projectlimbrescue.db.DatabaseSingleton;
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.session.Session;
import com.example.projectlimbrescue.db.session.SessionDao;
import com.example.projectlimbrescue.db.session.SessionWithDevices;
import com.example.shared.ReadingLimb;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class HistoryFragment extends Fragment {
    private RecyclerView mHistoryRecyclerView;
    private HistoryAdapter mAdapter;
    private ProgressBar progressBar;

    ListenableFuture<List<SessionWithDevices>> sessionsFuture;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(sessionsFuture != null) { sessionsFuture.cancel(true); }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        progressBar = view.findViewById(R.id.history_progress_bar);

        mHistoryRecyclerView = view.findViewById(R.id.history_recycler_view);
        mHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider));
        mHistoryRecyclerView.addItemDecoration(itemDecorator);

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
                        mAdapter.setSortDateMostRecent();
                        mAdapter.sort();
                        mAdapter.notifyDataSetChanged();
                        break;
                    case 1:
                        mAdapter.setSortDateLeastRecent();
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
        mAdapter = new HistoryAdapter(new ArrayList<>(), getActivity());
        mHistoryRecyclerView.setAdapter(mAdapter);
        progressBar.setVisibility(View.VISIBLE);

        AppDatabase db = DatabaseSingleton.getInstance(getContext());
        SessionDao sessionDao = db.sessionDao();
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        if(sessionsFuture != null) {
            sessionsFuture.cancel(true);
        }
        sessionsFuture = sessionDao.getSessionsWithDevices();
        sessionsFuture.addListener(() -> {
            try {
                List<SessionWithDevices> sessions = sessionsFuture.get();
                // If the user taps the navigation bar icon twice it will cause this listener to mess up.
                mAdapter = new HistoryAdapter(sessions, getActivity());
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    mHistoryRecyclerView.setAdapter(mAdapter);
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, service);
    }

}