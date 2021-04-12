package com.example.projectlimbrescue.db;

import android.content.Context;

import androidx.room.Room;

public class DatabaseSingleton {

    private static AppDatabase instance = null;

    private DatabaseSingleton() { }

    public static AppDatabase getInstance(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context,
                    AppDatabase.class, "plr-db").build();
        }
        return instance;
    }
}
