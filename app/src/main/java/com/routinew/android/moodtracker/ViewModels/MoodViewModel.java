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

import timber.log.Timber;

public class MoodViewModel extends ViewModel {
    /**
     * for the graph
     */

    public static final int GRAPH_2_WEEKS = 1;
    public static final int GRAPH_1_MONTH = 2;
    public static final int GRAPH_3_MONTHS = 3;
    public static final int GRAPH_6_MONTHS = 4;
    public static final int GRAPH_1_YEAR = 5;


    // TODO: Implement the ViewModel

    private MoodRepository mMoodRepository;

    private int moodDateRange = GRAPH_2_WEEKS; // start with two weeks
    private LiveData<List<Mood>> moods;

    private LiveData<Mood> currentMood;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public MoodViewModel(MoodRepository moodRepository) {
        mMoodRepository = moodRepository;
    }

    /**
     * get a list of moods -- should be able to specify by a date range
     * @return link to a range of moods
     */
    public LiveData<List<Mood>> getMoods() {
        if (null == moods) {
            moods = mMoodRepository.getMoods();
        }
        return moods;
    }


    public void setMoodDateRange(int range) {
        Calendar endDate = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        switch(range) {
            case GRAPH_2_WEEKS:
                startDate.add(Calendar.WEEK_OF_YEAR,-2);
                break;
            case GRAPH_1_MONTH:
                startDate.add(Calendar.MONTH,-1);
                break;
            case GRAPH_3_MONTHS:
                startDate.add(Calendar.MONTH,-3);
                break;
            case GRAPH_6_MONTHS:
                startDate.add(Calendar.MONTH, -6);
                break;
            case GRAPH_1_YEAR:
                startDate.add(Calendar.YEAR, -1);
            default:
                Timber.w("mood range invalid -- setting to 2 weeks: %d",range);
                startDate.add(Calendar.WEEK_OF_YEAR, -2);

        }
        mMoodRepository.setMoodDateRange(startDate,endDate);
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
        mMoodRepository.commitScore();
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
