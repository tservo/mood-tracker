package com.routinew.android.moodtracker.POJO;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.routinew.android.moodtracker.Utilities.CalendarUtilities;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;

import timber.log.Timber;

/**
 * stores the information regarding a mood - a score from -5 to +5, and a date attached.
 */
@IgnoreExtraProperties
public class Mood {
    // define the mood score range here.
    public static final int MOOD_MINIMUM = -5;
    public static final int MOOD_MAXIMUM = 5;
    public static final int EMPTY_MOOD = -99;

    private String date; // in yyyy-MM-dd format
    private int moodScore;

    // there might be more information here to use

    private boolean validateDate(String date)  {
        Calendar calendar = CalendarUtilities.textDateToCalendar(date);
        if (null == calendar)
            throw new IllegalArgumentException("Illegal date format: " + date);

        return true;
    }

    /**
     * Basic constructor to log a mood.
     * @param moodScore
     * @param date -- timestamp
     */
    public Mood(int moodScore, String date) {
        if (moodScore < MOOD_MINIMUM || moodScore > MOOD_MAXIMUM)
            throw new IllegalArgumentException(String.format(Locale.getDefault(), "%d not between %d and %d",
                    moodScore , MOOD_MINIMUM, MOOD_MAXIMUM));

        this.moodScore = moodScore;

        // validate date.
        if (validateDate(date)) {
            this.date = date;
        }

    }

    /**
     * feed a calendar for the date
     * @param moodScore
     * @param date
     */
    public Mood(int moodScore, Calendar date) {
        this(moodScore, CalendarUtilities.calendarToTextDate(date));
    }

    // sets the mood to be for today
    public Mood(int moodScore) {
        this(moodScore, Calendar.getInstance());
    }

    // this is for generating empty moods -- do not save these in the db
    // until they get a mood score
    public Mood(Calendar date) {
        // this can only be valid on an object creation, never changed to this
        this.moodScore = EMPTY_MOOD;
    }

    // for firebase setValue
    public Mood() {}

    // getters

    public int getMoodScore() {
        return moodScore;
    }

    public void setMoodScore(int moodScore) {
        if (moodScore == EMPTY_MOOD) {
            Timber.w("setMoodScore: tried to set score as empty mood");
            this.moodScore = moodScore;
            return;
        }

        if (moodScore < MOOD_MINIMUM || moodScore > MOOD_MAXIMUM)
            throw new IllegalArgumentException(String.format(Locale.getDefault(), "%d not between %d and %d",
                    moodScore , MOOD_MINIMUM, MOOD_MAXIMUM));

        this.moodScore = moodScore;
    }

    public String getDate() {
        return this.date;
    }

    // this isn't a getter for you, Firebase
    @Exclude
    public Calendar getCalendarDate() {
        return CalendarUtilities.textDateToCalendar(this.date);
    }

    // only the serializer should use this!
    public void setDate(String date) {
        if (validateDate(date)) {
            this.date = date;
        }
    }

    // this isn't the setter you are looking for, Firebase
    @Exclude
    public void setDate(Calendar date) {
        this.date = CalendarUtilities.calendarToTextDate(date);
    }

    // necessary to determine if mood is actually empty.
    public boolean isEmpty() {
        return (this.moodScore == EMPTY_MOOD);
    }

    @NonNull
    public String toString() {
        return String.format(Locale.getDefault(),"{Mood date=%s moodScore=%d}",date, moodScore);
    }

    private static Comparator<Mood> sDateComparator = new Comparator<Mood>() {
        @Override
        public int compare(Mood o1, Mood o2) {
            return (int) (CalendarUtilities.textDateToLong(o1.date) - CalendarUtilities.textDateToLong(o2.date));
        }
    };

    public static Comparator<Mood> dateComparator() {
        return sDateComparator;
    }



}
