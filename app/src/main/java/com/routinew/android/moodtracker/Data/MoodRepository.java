package com.routinew.android.moodtracker.Data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.routinew.android.moodtracker.POJO.Mood;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import timber.log.Timber;

/**
 * singleton class - may use Dagger 2 in the future to handle this
 * clearinghouse between the view model(s) and the data source(s)
 */
public class MoodRepository {

    // singleton instance
    private static MoodRepository sMoodRepository;



    private MutableLiveData<List<Mood>> moods;
    private MutableLiveData<Mood> currentMood;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private HashMap<String, Mood> mMoodTable = new HashMap<>(); // for testing purposes

    // singleton constructor
    private MoodRepository() {
        // singleton!
        mockMoods(); // prime the data
    }


    /**
     * retrieve instance of getInstance, and create it if necessary.
     * @return
     */
    public static MoodRepository getInstance() {
        if (null == sMoodRepository) {
            sMoodRepository = new MoodRepository();
        }

        return sMoodRepository;
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

    public LiveData<Mood> getSelectedMood() {
        if (currentMood == null) {
            currentMood = new MutableLiveData<>();
            loadSelectedMood();
        }

        return currentMood;
    }

    public void setSelectedMoodScore(int newScore) {
        // this is where we'd lock down the storage to do this


        Mood m = currentMood.getValue();
        if (null != m) {
            m.setMoodScore(newScore);
            currentMood.setValue(m);
        } else {
            Timber.w("setSelectedMoodScore: current mood is null!");
        }

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
        currentMood.postValue(mMoodTable.get(dateFormat(date)));
    }

    private void loadSelectedMood() {
        Mood mood = mMoodTable.get(dateFormat(Calendar.getInstance()));
        currentMood.setValue(mood);
    }

    /**
     * will commit mood to storage.
     */
    private void writeMood() {

    }


    ////////////////////////
    // mock functions!

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
            mMoodTable.put(dateFormat(startDate),generateMood(startDate));
            startDate.add(Calendar.DATE, +1); // next day
        }
    }



    /**
     * for getting a random mood score.
     * @return
     */
    private int generateRandomMoodScore() {
        return new Random().nextInt((Mood.MOOD_MAXIMUM - Mood.MOOD_MINIMUM) + 1) + Mood.MOOD_MINIMUM;
    }

    /**
     * test method for generating moods
     * @param date
     * @return
     */
    private Mood generateMood(Calendar date) {
        return new Mood(generateRandomMoodScore(), date);
    }
}
