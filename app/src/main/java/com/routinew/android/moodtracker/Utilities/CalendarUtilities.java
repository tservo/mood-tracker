package com.routinew.android.moodtracker.Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class CalendarUtilities {
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    static {
        sDateFormat.setLenient(false);
    }

    /**
     * Set the date to midnight, for further use
     * @param calendar
     * @return
     */
    private static void setToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
    }

    /**
     * get the timestamp of the supplied calendar
     * @param calendar
     * @return
     */
    public static long calendarToLong(Calendar calendar) {
        setToMidnight(calendar);
        return calendar.getTimeInMillis();
    }

    /**
     * turns a calendar into a text date
     * @param calendar
     * @return
     */
    public static String calendarToTextDate(Calendar calendar) {
        setToMidnight(calendar);
        return sDateFormat.format(calendar.getTime());
    }

    /**
     * turns a text date into a calendar.  returns null if invalid text date.
     * @param textDate
     * @return
     */
    public static Calendar textDateToCalendar(String textDate) {
        try {
            Date date =  sDateFormat.parse(textDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        } catch (ParseException e) {
            Timber.w(e,"Invalid date %s",textDate);
            return null;
        }
    }

    public static long textDateToLong(String textDate) {
        try {
            Date date =  sDateFormat.parse(textDate);
            return date.getTime();
        } catch (ParseException e) {
            Timber.w(e,"Invalid date %s",textDate);
            return -1;
        }
    }


}
