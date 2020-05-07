package com.routinew.android.moodtracker.ViewModels;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.routinew.android.moodtracker.Data.MoodRepository;
import com.routinew.android.moodtracker.POJO.Mood;
import com.routinew.android.moodtracker.Utilities.CalendarUtilities;
import com.routinew.android.moodtracker.Utilities.CalendarUtilities.GraphRange;
import com.routinew.android.moodtracker.widget.MoodUpdateIntentService;

import java.util.Calendar;

import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class MoodViewModel extends ViewModel {


    private final MoodRepository mMoodRepository;

    // this transformation handles the link for graphing moods.
    private final MutableLiveData<GraphRange> moodDateRange = new MutableLiveData<>(); // start with two weeks
    final private LiveData<List<Mood>> reportMoods = Transformations.switchMap(moodDateRange, new Function<GraphRange, LiveData<List<Mood>>>() {
        @Override
        public LiveData<List<Mood>> apply(GraphRange range) {
            return mMoodRepository.getMoodRange(CalendarUtilities.startDateOfGraphRange(range));
        }
    });


    private final MutableLiveData<Calendar> selectedDate = new MutableLiveData<>();

    // will always point to today's date -- needed in case we go to tomorrow while in the app.
    private final MutableLiveData<Calendar> todaysDate = new MutableLiveData<>();

    private final MutableLiveData<Mood> currentMood = new MutableLiveData<>();

    // does the current mood need to be flushed out?
    private boolean currentMoodIsDirty;

    // this will handle the check to see if the date has changed
    private Handler handler = new Handler();
    private static final long DELAY_TIME = TimeUnit.MINUTES.toMillis(2);

    private Runnable updateTodaysDate = new Runnable() {
        @Override
        public void run() {
            Timber.d("Updating the date");
            todaysDate.setValue(CalendarUtilities.today());
            selectedDate.setValue(CalendarUtilities.today());
            handler.postDelayed(updateTodaysDate, DELAY_TIME);
        }
    };

    /**
     * when model is instantiated,
     * or user has changed, call this method to reset everything.
     */
    private void initializeData() {
        // attempt to pull the new mood when the date changes.
        selectedDate.observeForever(new Observer<Calendar>() {
            @Override
            public void onChanged(@Nullable Calendar newDate) {
                if (null == newDate) return;

                Timber.d("selectedDate: date change: %s/%s/%s", newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(Calendar.DATE));

                mMoodRepository.getMoodAtDate(newDate, currentMood);
            }
        });

        this.selectedDate.setValue(CalendarUtilities.today()); // today
        this.todaysDate.setValue(CalendarUtilities.today());

        this.moodDateRange.setValue(CalendarUtilities.GraphRange.GRAPH_2_WEEKS);

        handler.postDelayed(updateTodaysDate, DELAY_TIME);


    }


    public MoodViewModel(MoodRepository moodRepository) {
        this.mMoodRepository = moodRepository;
        initializeData();
    }


    /**
     * find the currently selected date for initializing the horizontal calendar
     * -- doesn't need to be observed as livedata.
     *
     * @return a calendar object of the selected date
     */
    public LiveData<Calendar> getSelectedDate() {
        return selectedDate;
    }

    /**
     * get the currently selected mood to show.
     *
     * @return
     */
    public LiveData<Mood> getSelectedMood() {
        return currentMood;
    }


    /**
     * what is the today's date of the model?
     * needed when date changes
     *
     * @return liveData of today's date
     */
    public LiveData<Calendar> getTodaysDate() {
        return todaysDate;
    }


    /**
     * get the currently selected range of moods
     *
     * @return
     */
    public LiveData<List<Mood>> getReportMoods() {
        return reportMoods;
    }

    /**
     * get a handle to change the report range
     *
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
    public void commitMood(Context context) {
        Mood mood = currentMood.getValue();
        if ((null != mood) && (!mood.isEmpty()) && currentMoodIsDirty) {
            mMoodRepository.writeMoodToDatabase(mood);

            // and let the widget know too
            MoodUpdateIntentService.startActionUpdateMoodWidgets(context);
        }
    }

    /**
     * given a calendar date, make it the main date
     * to retrieve the current mood.
     *
     * @param date
     */
    public void selectMood(Calendar date) {
        selectedDate.setValue(date);
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        // get rid of the handler
        handler.removeCallbacks(null);
    }
}
