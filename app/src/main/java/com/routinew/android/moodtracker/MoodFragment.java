package com.routinew.android.moodtracker;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.routinew.android.moodtracker.Data.MoodRepository;
import com.routinew.android.moodtracker.POJO.Mood;
import com.routinew.android.moodtracker.ViewModels.MoodViewModel;
import com.routinew.android.moodtracker.ViewModels.MoodViewModelFactory;

import java.util.Calendar;
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
    @BindView(R.id.greeting) TextView mGreeting; // greeting to show user is logged in
    @BindView(R.id.tv_calendar_date) TextView mCalendarDate;
    @BindView(R.id.moodSlider) SeekBar mMoodSlider;
    @BindView(R.id.button_lock_slider) ToggleButton mToggleLockSlider;
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
        setGreeting();
        handleCalendar();
        handleMoodSlider();
    }




    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MoodViewModelFactory moodViewModelFactory = new MoodViewModelFactory(MoodRepository.getInstance());

        mViewModel = ViewModelProviders.of(getActivity(),moodViewModelFactory).get(MoodViewModel.class);
        // TODO: Use the ViewModel

       mViewModel.getSelectedMood().observe(this, new Observer<Mood>() {
           @Override
           public void onChanged(@Nullable Mood mood) {
               updateUI(mood);
           }
       });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // clean up after butterknife
        unbinder.unbind();
    }

    /**
     * handles the updating of the mood's ui based on the mood score
     * @param mood
     */
    private void updateUI(Mood mood) {
        // handle the slider
        if (null != mood) {
            mMoodSlider.setProgress(mood.getMoodScore() + 5);
        }
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
    private void setGreeting() {
        String greetingString;
        greetingString = "Testing here.";

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        if (acct != null) {
//            String personName = acct.getDisplayName();
            greetingString = acct.getGivenName();
//            String personFamilyName = acct.getFamilyName();
//            String personEmail = acct.getEmail();
//            String personId = acct.getId();
//            Uri personPhoto = acct.getPhotoUrl();
        }

        mGreeting.setText(greetingString);
    }


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
                mViewModel.selectMood(date);
                determineIfMoodSliderShouldBeLocked();
            }

        });
    }

    /**
     * set up interactions with the mood slider seek bar.
     */
    private void lockMoodSlider(final boolean locked) {
        // this will lock the button
        mMoodSlider.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return locked;
            }
        });

    }

    /**
     * helper to see if the mood slider should be locked. This will be called
     * when the month is called to decide to lock or unlock the score
     */
    private void determineIfMoodSliderShouldBeLocked() {
        // if day isn't current
        mToggleLockSlider.setChecked(true);
        // if there is already a mood score

        // if there was a mood score entered, it is unlocked, and a sufficient amount of time
        // or the data shifted (for example, going to the other screen, navigating away from the
        // app, or going to the report tab

    }

    // helper to lock and commit the mood score. will be called whenever
    // there has been a new/changed score
    private void lockAndCommitMoodScore() {
        mViewModel.commitScore(); // put the new score in the database
        mToggleLockSlider.setChecked(true); // lock the slider
    }

    private void handleMoodSlider() {
        mMoodSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // this will renormalize the mood score
                    mViewModel.setSelectedMoodScore(progress + Mood.MOOD_MINIMUM);
                    // and set a timeout -- once this timeout goes off we commit the value
                    // and lock the value
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // as we start up, match the state of the slider to the state of the lock.
        lockMoodSlider(mToggleLockSlider.isChecked());
        mToggleLockSlider.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // match the lock on the mood slider
                lockMoodSlider(isChecked);
            }
        });
    }
}
