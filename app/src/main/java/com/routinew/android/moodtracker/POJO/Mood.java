package com.routinew.android.moodtracker.POJO;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.routinew.android.moodtracker.Utilities.CalendarUtilities;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

import timber.log.Timber;

/**
 * stores the information regarding a mood - a score from -5 to +5, and a date attached.
 */
@IgnoreExtraProperties
public class Mood {
    // define the mood score range here.
    public static final int MOOD_MINIMUM = -5;
    public static final int MOOD_MAXIMUM = 5;
    public static final int EMPTY_MOOD = -99; // this is an ugly hack in order to allow firebase to not store an additional field to check for empty mood

    @NonNull
    private String date = "1975-12-31"; // in yyyy-MM-dd format -- dummy date that should never be used.

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
    public Mood(int moodScore, @NonNull String date) {
        if ((moodScore < MOOD_MINIMUM || moodScore > MOOD_MAXIMUM) && moodScore != EMPTY_MOOD)
            throw new IllegalArgumentException(String.format(Locale.getDefault(), "%d not between %d and %d ,and not empty.",
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

    @NonNull
    public String getDate() {
        return this.date;
    }

    // this isn't a getter for you, Firebase
    @Exclude
    @NonNull
    public Calendar getCalendarDate() {
        Calendar c =  CalendarUtilities.textDateToCalendar(this.date);
        if (null == c) {
            throw new IllegalStateException("Date field is invalid "+this.date);
            // there is no way we have this.date set improperly or to null.
        }

        return c;
    }

// --Commented out by Inspection START (2018/11/8, 13:13):
//    // only the serializer should use this!
//    public void setDate(String date) {
//        if (validateDate(date)) {
//            this.date = date;
//        }
//    }
// --Commented out by Inspection STOP (2018/11/8, 13:13)

// --Commented out by Inspection START (2018/11/8, 13:13):
//    // this isn't the setter you are looking for, Firebase
//    @Exclude
//    public void setDate(Calendar date) {
//        this.date = CalendarUtilities.calendarToTextDate(date);
//    }
// --Commented out by Inspection STOP (2018/11/8, 13:13)

    // necessary to determine if mood is actually empty.
    @Exclude
    public boolean isEmpty() {
        return (this.moodScore == EMPTY_MOOD);
    }

    @NonNull
    public String toString() {
        return String.format(Locale.getDefault(),"{Mood date=%s moodScore=%d}",date, moodScore);
    }

    private static final Comparator<Mood> sDateComparator = new Comparator<Mood>() {
        @Override
        public int compare(Mood o1, Mood o2) {
            return (int) (CalendarUtilities.textDateToLong(o1.date) - CalendarUtilities.textDateToLong(o2.date));
        }
    };

    public static Comparator<Mood> dateComparator() {
        return sDateComparator;
    }

    // These methods are for generating random moods for testing.

    /**
     * for getting a random mood score.
     * @return
     */
    private static int generateRandomMoodScore() {
        return new Random().nextInt((MOOD_MAXIMUM - MOOD_MINIMUM) + 1) + MOOD_MINIMUM;
    }

    /**
     * test method for generating moods
     * @param date
     * @param forceEmptyMood return an empty mood instead of a random mood score
     * @return generated mood for date.
     */
    public static Mood generateMood(Calendar date, boolean forceEmptyMood) {

        if (forceEmptyMood) {
            return new Mood(Mood.EMPTY_MOOD, date);
        }

        return new Mood(generateRandomMoodScore(), date);
    }



}
