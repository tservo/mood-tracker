package com.routinew.android.moodtracker.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import com.routinew.android.moodtracker.Data.MoodRepository;
import com.routinew.android.moodtracker.POJO.Mood;

import java.util.Calendar;

import java.util.List;

import timber.log.Timber;

public class MoodViewModel extends ViewModel {
    /**
     * for the graph
     */
    public enum GraphRange {
        GRAPH_2_WEEKS,
        GRAPH_1_MONTH,
        GRAPH_3_MONTHS,
        GRAPH_6_MONTHS,
        GRAPH_1_YEAR
    }


    private MoodRepository mMoodRepository;

    // this transformation handles the link for graphing moods.
    private MutableLiveData<GraphRange> moodDateRange = new MutableLiveData<>(); // start with two weeks
    private LiveData<List<Mood>> reportMoods;


    // this transformation retrieves the mood for the current date.
    private MutableLiveData<Calendar> selectedDate = new MutableLiveData<>();
    private MutableLiveData<Mood> currentMood = new MutableLiveData<>();

    // does the current mood need to be flushed out?
    private boolean currentMoodIsDirty;

    /**
     * when model is instantiated,
     * or user has changed, call this method to reset everything.
     */
    private void initializeData() {
        // attempt to pull the new mood when the date changes.
        selectedDate.observeForever(new Observer<Calendar>() {
            @Override
            public void onChanged(@Nullable Calendar newDate) {
                mMoodRepository.getMoodAtDate(newDate, currentMood);
            }
        });

        this.selectedDate.setValue(Calendar.getInstance()); // today


        this.moodDateRange.setValue(GraphRange.GRAPH_2_WEEKS);




//        this.reportMoods = Transformations.switchMap(moodDateRange, new Function<GraphRange, LiveData<List<Mood>>>() {
//            @Override
//            public LiveData<List<Mood>> apply(GraphRange range) {
//                Calendar startDate = Calendar.getInstance(); // today
//                switch (range) {
//                    case GRAPH_2_WEEKS:
//                        startDate.add(Calendar.WEEK_OF_YEAR, -2);
//                        break;
//                    case GRAPH_1_MONTH:
//                        startDate.add(Calendar.MONTH, -1);
//                        break;
//                    case GRAPH_3_MONTHS:
//                        startDate.add(Calendar.MONTH, -3);
//                        break;
//                    case GRAPH_6_MONTHS:
//                        startDate.add(Calendar.MONTH, -6);
//                        break;
//                    case GRAPH_1_YEAR:
//                        startDate.add(Calendar.YEAR, -1);
//                    default:
//                        Timber.w("mood range invalid -- setting to 2 weeks: %s", range.toString());
//                        startDate.add(Calendar.WEEK_OF_YEAR, -2);
//                }
//                return mMoodRepository.getMoodRange(startDate);
//            }
//        });

        this.reportMoods = new MutableLiveData<>();
    }

    public MoodViewModel(MoodRepository moodRepository) {
        this.mMoodRepository = moodRepository;
        initializeData();
    }


    /**
     * get the currently selected mood to show.
     * @return
     */
    public LiveData<Mood> getSelectedMood() {
        return currentMood;
    }

    /**
     * what is the current selected date of the model?
     * don't change it here -- use selectMood
     * @return
     */
    public LiveData<Calendar> getSelectedDate() {
        return selectedDate;
    }


    /**
     * get the currently selected range of moods
     * @return
     */
    public LiveData<List<Mood>> getReportMoods() {
        return reportMoods;
    }

    /**
     * get a handle to change the report range
     * @return
     */
    public MutableLiveData<GraphRange> getMoodDateRange() {
        return moodDateRange;
    }

    public void setSelectedMoodScore(int newScore) {
        if (newScore == Mood.EMPTY_MOOD || newScore < Mood.MOOD_MINIMUM || newScore > Mood.MOOD_MAXIMUM) {
            Timber.w("setSelectedMoodScore: invalid score %d", newScore);
        }
        
        Mood mood = currentMood.getValue(); // get snapshot

        // we don't have a mood yet for this date -- make one!
        if (null == mood) {
            mood = new Mood(newScore, selectedDate.getValue());
        } else {
            mood.setMoodScore(newScore); // just update it
        }

        // we changed it so update things
        currentMood.setValue(mood);
        currentMoodIsDirty = true;
    }

    /**
     * this method will commit the current mood to the database.
     * if the current mood has changed from the last read.
     */
    public void commitMood() {
        Mood mood = currentMood.getValue();
        if ((null != mood) && (!mood.isEmpty()) && currentMoodIsDirty) {
            mMoodRepository.writeMoodToDatabase(mood);
        }
    }

    /**
     * given a calendar date, make it the main date
     * to retrieve the current mood.
     * @param date
     */
    public void selectMood(Calendar date) {
        selectedDate.setValue(date);
    }


}
