package com.routinew.android.moodtracker.Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

public class CalendarUtilities {
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

    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    static {
        sDateFormat.setLenient(false);
    }

    /**
     * Set the date to midnight, for further use
     * @param calendar
     * @return
     */
    public static void setToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
    }

    /**
     * this method returns a calendar with today's date standardized at midnight
     * @return calendar with time at 0:00:00.000
     */
    public static Calendar today() {
        Calendar c = Calendar.getInstance();
        setToMidnight(c);
        return c;
    }


// --Commented out by Inspection START (2018/11/8, 13:13):
//    /**
//     * get the timestamp of the supplied calendar
//     * @param calendar
//     * @return
//     */
//    public static long calendarToLong(Calendar calendar) {
//        setToMidnight(calendar);
//        return calendar.getTimeInMillis();
//    }
// --Commented out by Inspection STOP (2018/11/8, 13:13)

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


    /**
     * utility method to get a calendar object with the graphRange specified.
     * @param graphRange the graph range
     * @return the calendar object with the start date.
     */
    public static Calendar startDateOfGraphRange(GraphRange graphRange) {
        Calendar startDate = Calendar.getInstance(); // today
        switch (graphRange) {
            case GRAPH_2_WEEKS:
                startDate.add(Calendar.WEEK_OF_YEAR, -2);
                break;
            case GRAPH_1_MONTH:
                startDate.add(Calendar.MONTH, -1);
                break;
            case GRAPH_3_MONTHS:
                startDate.add(Calendar.MONTH, -3);
                break;
            case GRAPH_6_MONTHS:
                startDate.add(Calendar.MONTH, -6);
                break;
            case GRAPH_1_YEAR:
                startDate.add(Calendar.YEAR, -1);
            default:
                Timber.w("mood range invalid -- setting to 2 weeks: %s", graphRange.toString());
                startDate.add(Calendar.WEEK_OF_YEAR, -2);
        }

        return startDate;
    }
}
