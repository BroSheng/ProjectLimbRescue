package com.example.projectlimbrescue;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class HistoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView mSessionIDTextView;
    private final TextView mDateTextView;
    private long mSession;

    public HistoryHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.list_item_history, parent, false));
        itemView.setOnClickListener(this);

        mSessionIDTextView = itemView.findViewById(R.id.history_id);
        mDateTextView = itemView.findViewById(R.id.history_date);
    }

    public void bind(HistoryAdapter.SessionInfo sessionInfo) {
        mSession = sessionInfo.id;
        mSessionIDTextView.setText(sessionInfo.arm);
        mDateTextView.setText(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US)
                .format(sessionInfo.date));
    }

    // launch graph activity to display session
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), DataAnalysisActivity.class);
        intent.putExtra("SESSION_ID", mSession);
        v.getContext().startActivity(intent);
    }
}