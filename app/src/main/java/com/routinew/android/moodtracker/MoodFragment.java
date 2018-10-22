package com.routinew.android.moodtracker;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.routinew.android.moodtracker.ViewModels.MoodViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.model.CalendarEvent;
import devs.mulham.horizontalcalendar.utils.CalendarEventsPredicate;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import timber.log.Timber;

public class MoodFragment extends Fragment {

    private MoodViewModel mViewModel;

    public static MoodFragment newInstance() {
        return new MoodFragment();
    }

    // butterknife
    @BindView(R.id.tv_calendar_date) TextView mCalendarDate;
    Unbinder unbinder;

    HorizontalCalendar mHorizontalCalendar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.mood_fragment, container, false);
        // attach butterknife
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        handleCalendar();
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MoodViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // clean up after butterknife
        unbinder.unbind();
    }

    /**
     * handle the formatting of the calendar
     * @param date
     * @param position
     */
    private void formatDate(Calendar date, int position) {
        String selectedDateStr = DateFormat.format("EEE\n MMM d, yyyy", date).toString();
        mCalendarDate.setText(selectedDateStr);
        //Toast.makeText(getContext(), selectedDateStr + " selected!", Toast.LENGTH_SHORT).show();
        Timber.i("onDateSelected: %s - Position = %s", selectedDateStr ,position);
    }

    // private helper methods
    private void handleCalendar() {
        /* starts before 1 month from now */
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);

        /* ends today */
        Calendar endDate = Calendar.getInstance();
        //endDate.add(Calendar.MONTH, 1);

        // initialize calendar to default to today
        final Calendar defaultDate = Calendar.getInstance();
        // and prime field with today's date.
        formatDate(defaultDate,0);

        mHorizontalCalendar = new HorizontalCalendar.Builder(getActivity(), R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .defaultSelectedDate(defaultDate)
                .addEvents(new CalendarEventsPredicate() {

                    @Override
                    public List<CalendarEvent> events(Calendar date) {
                        // test the date and return a list of CalendarEvent to assosiate with this Date.
                        return null;
                    }

                })
                .build();

        mHorizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                formatDate(date, position); // handle the calendar display field.
            }

        });
    }
}
