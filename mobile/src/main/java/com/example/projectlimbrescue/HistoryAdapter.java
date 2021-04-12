package com.example.projectlimbrescue;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectlimbrescue.db.AppDatabase;
import com.example.projectlimbrescue.db.DatabaseSingleton;
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.session.SessionWithDevices;
import com.example.shared.ReadingLimb;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Adapter class
public class HistoryAdapter extends RecyclerView.Adapter<HistoryHolder> {
    // all sessions
    private final List<SessionInfo> mAllSessions;
    // sessions from both limbs
    private final List<SessionInfo> mSessionsBoth;
    // sessions from left limb
    private final List<SessionInfo> mSessionsLeft;
    // sessions from right limb
    private final List<SessionInfo> mSessionsRight;
    // sessions used by the adapter, will be one of the above lists
    private List<SessionInfo> mSessions;
    // used to sort sessions
    Comparator<SessionInfo> mComparator;
    private final Activity mActivity;

    public class SessionInfo {
        long id;
        Date date;
        String arm;

        public SessionInfo(long id, Date date, String arm) {
            this.id = id;
            this.date = date;
            this.arm = arm;
        }
    }

    public HistoryAdapter(List<SessionWithDevices> sessions, Activity activity) {
        mActivity = activity;
        AppDatabase db = DatabaseSingleton.getInstance(activity.getApplicationContext());

        // pre-filter sessions
        mAllSessions = new LinkedList<>();
        mSessionsBoth = new LinkedList<>();
        mSessionsLeft = new LinkedList<>();
        mSessionsRight = new LinkedList<>();
        mSessions = mAllSessions;

        for (SessionWithDevices s : sessions) {
            try {
                List<Reading> leftReadings = db.readingDao().getReadingsForSessionIdAndLimb(s.session.sessionId,
                        ReadingLimb.LEFT_ARM).get();
                List<Reading> rightReadings = db.readingDao().getReadingsForSessionIdAndLimb(s.session.sessionId,
                        ReadingLimb.RIGHT_ARM).get();
                SessionInfo newSession;
                if(leftReadings.size() > 0 && rightReadings.size() > 0) {
                    newSession = new SessionInfo(s.session.sessionId, s.session.startTime, "Both Arms");
                    mSessionsBoth.add(newSession);
                } else if (leftReadings.size() > 0) {
                    newSession = new SessionInfo(s.session.sessionId, s.session.startTime, "Left Arm");
                    mSessionsLeft.add(newSession);
                } else {
                    newSession = new SessionInfo(s.session.sessionId, s.session.startTime, "Right Arm");
                    mSessionsRight.add(newSession);
                }
                mAllSessions.add(newSession);
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
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
        return new HistoryHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryHolder holder, int position) {
        holder.bind(mSessions.get(position));
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

    public void setSortDateMostRecent() {
        mComparator = (o1, o2) -> o1.date.compareTo(o2.date);
    }

    public void setSortDateLeastRecent() {
        mComparator = (o1, o2) -> o2.date.compareTo(o1.date);
    }

    public void sort() {
        mSessions.sort(mComparator);
    }
}
