package com.routinew.android.moodtracker.Data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.routinew.android.moodtracker.POJO.Mood;

import java.util.Calendar;
import java.util.List;

public interface MoodRepository {

    LiveData<List<Mood>> getMoodRange(Calendar calendar);

    void getMoodAtDate(Calendar calendar, MutableLiveData<Mood> moodMutableLiveData);

    void writeMoodToDatabase(Mood mood);
}
