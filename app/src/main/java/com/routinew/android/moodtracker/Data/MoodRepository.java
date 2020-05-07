package com.routinew.android.moodtracker.Data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.routinew.android.moodtracker.POJO.Mood;

import java.util.Calendar;
import java.util.List;

public interface MoodRepository {

    LiveData<List<Mood>> getMoodRange(Calendar calendar);

    void getMoodAtDate(Calendar calendar, MutableLiveData<Mood> moodMutableLiveData);

    void writeMoodToDatabase(Mood mood);
}
