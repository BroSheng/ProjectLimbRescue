package com.example.projectlimbrescue;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
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
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.reading.ReadingDao;
import com.example.projectlimbrescue.db.sensor.SensorDao;
import com.example.projectlimbrescue.db.session.Session;
import com.example.projectlimbrescue.db.session.SessionDao;
import com.example.projectlimbrescue.db.session.SessionMeasuresSensor;
import com.example.projectlimbrescue.db.session.SessionMeasuresSensorDao;
import com.example.projectlimbrescue.db.session.SessionReadsFromDeviceDao;
import com.example.projectlimbrescue.db.session.SessionWithReadings;
import com.example.shared.ReadingLimb;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SettingsFragment extends Fragment {

    AppDatabase db;

    ListeningExecutorService service;

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

    Button exportAllButton;

    ProgressBar spinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = DatabaseSingleton.getInstance(this.getContext());
        service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

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

        exportAllButton = (Button) view.findViewById(R.id.export_all_button);
        exportAllButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        export_all_data(view);
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
        allDeletes.addListener(() -> {
            this.getActivity().runOnUiThread(() -> {
                clearDataButton.setVisibility(View.VISIBLE);
                clearDataWarning.setVisibility(View.INVISIBLE);
                clearDataConfirmButton.setEnabled(true);
                clearDataConfirmButton.setVisibility(View.INVISIBLE);
                spinner.setVisibility(View.INVISIBLE);
            });
        }, service);
        clearDataConfirmButton.setEnabled(false);
        spinner.setVisibility(View.VISIBLE);
    }

    public void export_all_data(View view) {
        ListenableFuture<List<Session>> allSessionsFuture = sessionDao.getSessions();
        allSessionsFuture.addListener(() -> {
            try {

                String zipfilename = "plr_data_export.zip";
                FileOutputStream zipFileOutputStream = getContext().openFileOutput(zipfilename, Context.MODE_PRIVATE);
                ZipOutputStream zipOutputStream = new ZipOutputStream(zipFileOutputStream);
                List<Session> allSessions = allSessionsFuture.get();

                for (Session session : allSessions) {
                    long sessionId = session.sessionId;
                    ListenableFuture<List<List<Reading>>> readingsEachLimb = Futures.allAsList(
                            readingDao.getReadingsForSessionIdAndLimb(sessionId, ReadingLimb.LEFT_ARM),
                            readingDao.getReadingsForSessionIdAndLimb(sessionId, ReadingLimb.RIGHT_ARM));

                    // it's relatively ok to busy-wait here since we're already on a non-UI thread.
                    // Causes a lot less programmer headache than trying to write each session asynchronously.
                    readingsEachLimb.get();
                    List<Reading> leftReadings = readingsEachLimb.get().get(0);
                    List<Reading> rightReadings = readingsEachLimb.get().get(1);

                    // readings for each limb have been collected; now the actual saving of the data
                    StringBuilder data = new StringBuilder();
                    data.append("Limb,Time,Value");
                    for (Reading leftReading : leftReadings) {
                        data.append("\n" + ReadingLimb.LEFT_ARM.name() + "," + leftReading.time + "," + leftReading.value);
                    }
                    for (Reading rightReading : rightReadings) {
                        data.append("\n" + ReadingLimb.RIGHT_ARM.name() + "," + rightReading.time + "," + rightReading.value);
                    }
                    Timestamp sessionTime = session.startTime;
                    String fileSuffix = new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss", Locale.US).format(sessionTime);
                    String filename = "session_" + fileSuffix + ".csv";

                    ZipEntry zipEntry = new ZipEntry(filename);
                    zipOutputStream.putNextEntry(zipEntry);
                    zipOutputStream.write(data.toString().getBytes());
                    zipOutputStream.closeEntry();
                }

                Context context = this.getContext().getApplicationContext();
                File file = new File(this.getContext().getFilesDir(), zipfilename);
                Uri path = FileProvider.getUriForFile(context, "com.example.projectlimbrescue.fileprovider", file);
                Intent fileIntent = new Intent(Intent.ACTION_SEND);
                fileIntent.setDataAndType(path, "archive/zip");
                fileIntent.putExtra(Intent.EXTRA_SUBJECT,"PLR Export Data");
                fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                startActivity(Intent.createChooser(fileIntent, "Export PLR Data"));

                spinner.setVisibility(View.INVISIBLE);

                zipOutputStream.close();
                zipFileOutputStream.close();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, service);

        spinner.setVisibility(View.VISIBLE);
    }
}