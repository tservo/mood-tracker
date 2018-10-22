package com.routinew.android.moodtracker.POJO;

import java.time.LocalDate;
import java.util.Calendar;

/**
 * stores the information regarding a mood - a score from -5 to +5, and a date attached.
 */
public class Mood {
    private Calendar mDate;
    private int mMoodScore;

    // there might be more information here to use

    /**
     * Basic constructor to log a mood.
     * @param moodScore
     * @param date
     */
    public Mood(int moodScore, Calendar date) {
        if (moodScore < -5 || moodScore > 5)  throw new IllegalArgumentException(moodScore + " not between -5 and 5!");

        mMoodScore = moodScore;
        mDate = date;
    }

    // sets the mood to be for today
    public Mood(int moodScore) {
        this(moodScore, Calendar.getInstance());
    }

    public int getMoodScore() {
        return mMoodScore;
    }

    public Calendar getDate() {
        return mDate;
    }

}
