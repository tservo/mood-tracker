package com.routinew.android.moodtracker.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.routinew.android.moodtracker.POJO.Mood;

import java.util.List;

public class MoodViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    private MutableLiveData<List<Mood>> moods;

    public LiveData<List<Mood>> getMoods() {
        if (moods == null) {
            moods = new MutableLiveData<List<Mood>>();
            loadMoods();
        }
        return moods;
    }

    private void loadMoods() {

    }
}
