package com.example.projectlimbrescue;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.projectlimbrescue.db.AppDatabase;
import com.example.projectlimbrescue.db.DatabaseSingleton;
import com.example.projectlimbrescue.db.device.DeviceContainsSensorDao;
import com.example.projectlimbrescue.db.device.DeviceDao;
import com.example.projectlimbrescue.db.reading.ReadingDao;
import com.example.projectlimbrescue.db.sensor.SensorDao;
import com.example.projectlimbrescue.db.session.SessionDao;
import com.example.projectlimbrescue.db.session.SessionMeasuresSensor;
import com.example.projectlimbrescue.db.session.SessionMeasuresSensorDao;
import com.example.projectlimbrescue.db.session.SessionReadsFromDeviceDao;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {

    AppDatabase db;

    DeviceContainsSensorDao deviceContainsSensorDao;
    DeviceDao deviceDao;
    ReadingDao readingDao;
    SensorDao sensorDao;
    SessionDao sessionDao;
    SessionMeasuresSensorDao sessionMeasuresSensorDao;
    SessionReadsFromDeviceDao sessionReadsFromDeviceDao;

    Button clearDataButton;
    Button clearDataConfirmButton;
    TextView clearDataWarning;

    ProgressBar spinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = DatabaseSingleton.getInstance(this.getContext());
        deviceContainsSensorDao = db.deviceContainsSensorDao();
        deviceDao = db.deviceDao();
        readingDao = db.readingDao();
        sensorDao = db.sensorDao();
        sessionDao = db.sessionDao();
        sessionMeasuresSensorDao = db.sessionMeasuresSensorDao();
        sessionReadsFromDeviceDao = db.sessionReadsFromDeviceDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        clearDataButton = (Button) view.findViewById(R.id.clear_button);
        clearDataButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clear_data(view);
                    }
                }
        );

        clearDataWarning = (TextView) view.findViewById(R.id.clear_data_warning);

        clearDataConfirmButton = (Button) view.findViewById(R.id.confirm_clear_button);
        clearDataConfirmButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clear_data_confirm(view);
                    }
                }
        );

        spinner = view.findViewById(R.id.progressBar1);

        return view;
    }

    // this method effectively just makes the "confirm" button and warning visible.
    public void clear_data(View view) {
        clearDataButton.setVisibility(View.INVISIBLE);
        clearDataWarning.setVisibility(View.VISIBLE);
        clearDataConfirmButton.setVisibility(View.VISIBLE);
    }

    // this method does the actual heavy lifting to clear the database.
    public void clear_data_confirm(View view) {

        ListenableFuture<List<Integer>> allDeletes = Futures.allAsList(deviceContainsSensorDao.deleteAll(),
            sessionMeasuresSensorDao.deleteAll(),
            sessionReadsFromDeviceDao.deleteAll(),
            readingDao.deleteAll(),
            deviceDao.deleteAll(),
            sensorDao.deleteAll(),
            sessionDao.deleteAll());
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        allDeletes.addListener(() -> {
            this.getActivity().runOnUiThread(() -> {
                clearDataButton.setVisibility(View.VISIBLE);
                clearDataWarning.setVisibility(View.INVISIBLE);

                spinner.setVisibility(View.INVISIBLE);
            });
        }, service);
        clearDataConfirmButton.setVisibility(View.INVISIBLE);
        spinner.setVisibility(View.VISIBLE);
    }
}