package com.routinew.android.moodtracker.Data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.routinew.android.moodtracker.POJO.Mood;

import java.util.Calendar;
import java.util.List;

public class RoomDatabaseMoodRepository implements MoodRepository {

    private static RoomDatabaseMoodRepository sRoomDatabaseMoodRepository;

    // set up a livedata to listen to whether the database is online or not.
    private final static MutableLiveData<Boolean> sIsConnected = new MutableLiveData<>();

    public static LiveData<Boolean> databaseIsConnected() { return sIsConnected;}

    private RoomDatabaseMoodRepository() {
        sIsConnected.setValue(true);

    }

    @Override
    public LiveData<List<Mood>> getMoodRange(Calendar calendar) {
        return null;
    }

    @Override
    public void getMoodAtDate(Calendar calendar, MutableLiveData<Mood> moodMutableLiveData) {

    }

    @Override
    public void writeMoodToDatabase(Mood mood) {

    }
}
