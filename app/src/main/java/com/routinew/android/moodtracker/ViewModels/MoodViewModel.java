package com.routinew.android.moodtracker.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.routinew.android.moodtracker.Data.MoodRepository;
import com.routinew.android.moodtracker.POJO.Mood;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MoodViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    /*
       private static final DatabaseReference MOOD_REF =
        FirebaseDatabase.getInstance().getReference("/moods");

    private final FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(MOOD_REF);

    @NonNull
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }
     */
    private MoodRepository mMoodRepository;

    private LiveData<List<Mood>> moods;
    private LiveData<Mood> currentMood;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private HashMap<String, Mood> mMoodTable = new HashMap<>(); // for testing purposes

    public MoodViewModel(MoodRepository moodRepository) {
        mMoodRepository = moodRepository;
    }

    /**
     * get a list of moods -- should be able to specify by a date range
     * @return link to a range of moods
     */
    public LiveData<List<Mood>> getMoods() {
        if (moods == null) {
            moods = mMoodRepository.getMoods();
        }
        return moods;
    }

    /**
     * get the currently selected mood to show.
     * @return
     */
    public LiveData<Mood> getSelectedMood() {
        if (currentMood == null) {
            currentMood = mMoodRepository.getSelectedMood();
        }
        return currentMood;
    }

    public void setSelectedMoodScore(int newScore) {
        // this is where we'd lock down the storage to do this
        mMoodRepository.setSelectedMoodScore(newScore);
    }

    /**
     * tests if there is a mood score for the current day.
     * @return is there a mood score for the current day?
     */
    public boolean doesSelectedMoodScoreExist() {
        return true; // we're still testing with data, so yes it does.
    }

    /**
     * this method will commit the current mood to the database.
     */
    public void commitScore() {

    }

    /**
     * given a calendar date, retrieve the mood associated with it.
     * @param date
     */
    public void selectMood(Calendar date) {
        // for now just retrieve
        mMoodRepository.selectMood(date);
    }


}
