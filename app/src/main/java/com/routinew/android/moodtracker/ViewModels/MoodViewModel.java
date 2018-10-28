package com.routinew.android.moodtracker.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.routinew.android.moodtracker.POJO.Mood;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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


    private MutableLiveData<List<Mood>> moods;
    private MutableLiveData<Mood> currentMood;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private HashMap<String, Mood> mMoodTable = new HashMap<>(); // for testing purposes

    public MoodViewModel() {
       super();
       mockMoods();
    }

    public LiveData<List<Mood>> getMoods() {
        if (moods == null) {
            moods = new MutableLiveData<>();
            loadMoods();
        }
        return moods;
    }

    private void loadMoods() {
        moods.setValue(new ArrayList<>(mMoodTable.values())); // convert to list
    }

    public LiveData<Mood> getCurrentMood() {
        if (currentMood == null) {
            currentMood = new MutableLiveData<>();
            loadCurrentMood();
        }

        return currentMood;
    }

    public void setCurrentMoodScore(int newScore) {
        // this is where we'd lock down the storage to do this


        Mood m = currentMood.getValue();
        m.setMoodScore(newScore);
        currentMood.setValue(m);
    }

    /**
     * tests if there is a mood score for the current day.
     * @return is there a mood score for the current day?
     */
    public boolean doesCurrentMoodScoreExist() {
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
        currentMood.postValue(mMoodTable.get(dateFormat(date)));
    }

    private void loadCurrentMood() {
        Mood mood = mMoodTable.get(dateFormat(Calendar.getInstance()));
        currentMood.setValue(mood);
    }


    // helper to get a hash code for the calendar
    private String dateFormat(Calendar calendar) {
        // for the hash map

        return mDateFormat.format(calendar.getTime());
    }
    // create a list of moods for the past month
    private void mockMoods() {
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, -30);

        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DATE,+1);



        // here we store the moods by date.
        while (startDate.before(currentDate)) {
            mMoodTable.put(dateFormat(startDate),Mood.generateMood(startDate));
            startDate.add(Calendar.DATE, +1); // next day
        }
    }

    private void writeMood() {

    }
}
