package com.routinew.android.moodtracker.POJO;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

/**
 * stores the information regarding a mood - a score from -5 to +5, and a date attached.
 */
public class Mood {
    // define the mood score range here.
    public static final int MOOD_MINIMUM = -5;
    public static final int MOOD_MAXIMUM = 5;

    private long mDate; // in millis
    private int mMoodScore;

    // there might be more information here to use

    /**
     * Basic constructor to log a mood.
     * @param moodScore
     * @param millis -- timestamp
     */
    public Mood(int moodScore, long millis) {
        if (moodScore < MOOD_MINIMUM || moodScore > MOOD_MAXIMUM)
            throw new IllegalArgumentException(String.format(Locale.getDefault(), "%d not between %d and %d",
                    moodScore , MOOD_MINIMUM, MOOD_MAXIMUM));

        mMoodScore = moodScore;
        mDate = millis;
    }

    /**
     * feed a calendar for the date
     * @param moodScore
     * @param date
     */
    public Mood(int moodScore, Calendar date) {
        this(moodScore, date.getTimeInMillis());
    }

    // sets the mood to be for today
    public Mood(int moodScore) {
        this(moodScore, Calendar.getInstance());
    }


    // getters

    public int getMoodScore() {
        return mMoodScore;
    }

    public void setMoodScore(int moodScore) {
        if (moodScore < MOOD_MINIMUM || moodScore > MOOD_MAXIMUM)
            throw new IllegalArgumentException(String.format(Locale.getDefault(), "%d not between %d and %d",
                    moodScore , MOOD_MINIMUM, MOOD_MAXIMUM));

        mMoodScore = moodScore;
    }

    public Calendar getDate() {
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(mDate);
        return result;
    }


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
     * @return
     */
    public static Mood generateMood(Calendar date) {
        return new Mood(generateRandomMoodScore(), date);
    }

    private static Comparator<Mood> sDateComparator = new Comparator<Mood>() {
        @Override
        public int compare(Mood o1, Mood o2) {
            return (int) (o1.mDate - o2.mDate);
        }
    };

    public static Comparator<Mood> dateComparator() {
        return sDateComparator;
    }



}
