package com.routinew.android.moodtracker;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
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
import com.routinew.android.moodtracker.Utilities.CalendarUtilities;
import com.routinew.android.moodtracker.ViewModels.MoodViewModel;
import com.routinew.android.moodtracker.ViewModels.MoodViewModelFactory;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import timber.log.Timber;

public class MoodFragment extends Fragment {

    private static final int UPDATE_DELAY = 5000;
    private static final String TEXTVIEW_DATE_FORMAT = "E, MMM d, yyyy";

    private MoodViewModel mViewModel;

    // this class allows the timer to update the slider, on the UI thread.
    private final Handler mTimerHandler = new Handler();
    private final Runnable mDelayedMoodCommit = new Runnable() {
        @Override
        public void run() {
            // we are going to commit the mood - clear the slider unlock flag
            mKeepSliderUnlocked = false;
            lockAndCommitMood();
        }
    };

    private boolean mKeepSliderUnlocked = false;


    public static MoodFragment newInstance() {
        return new MoodFragment();
    }

    // butterknife
    @BindView(R.id.greeting) TextView mGreeting; // greeting to show user is logged in
    @BindView(R.id.tv_calendar_date) TextView mCalendarDate;
    @BindView(R.id.moodSlider) SeekBar mMoodSlider;
    @BindView(R.id.button_lock_slider) ToggleButton mToggleLockSlider;

    // handle for setting if database is offline.
    @BindView(R.id.mood_fragment_layout) ConstraintLayout mMoodFragmentLayout;
    @BindView(R.id.mood_data_offline) TextView mMoodDataOffline;
    private Unbinder unbinder;

    private HorizontalCalendar mHorizontalCalendar;

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
        handleMoodSlider();
    }




    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MoodViewModelFactory moodViewModelFactory = new MoodViewModelFactory(MoodRepository.getInstance());

        mViewModel = ViewModelProviders.of(requireActivity(),moodViewModelFactory).get(MoodViewModel.class);

        // handle if we're offline
        MoodRepository.databaseIsConnected().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isOnline) {
                handleIfOnline(isOnline);
            }
        });
        // update the ui when the mood changes
        mViewModel.getSelectedMood().observe(this, new Observer<Mood>() {
           @Override
           public void onChanged(@Nullable Mood mood) {
               determineIfMoodSliderShouldBeLocked(mood);
               updateUI(mood);
           }
       });


        // and we need to listen once to the loading of the mood value to set up whether the mood slider is locked.
        mViewModel.getSelectedMood().observe(this, new Observer<Mood>() {
            @Override
            public void onChanged(@Nullable Mood mood) {

                mViewModel.getSelectedMood().removeObserver(this);
            }
        });

        handleCalendar(); // create calendar

        // and give it the ability to change on date change
        mViewModel.getTodaysDate().observe(this, new Observer<Calendar>() {
            @Override
            public void onChanged(@Nullable Calendar calendar) {
                updateCalendarDate(calendar);
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
        if (null == mood) return; // guard against null mood

        if (mood.isEmpty()) {
            mMoodSlider.setProgress(0);
        } else {
            mMoodSlider.setProgress(mood.getMoodScore() - Mood.MOOD_MINIMUM);
        }
    }


    /**
     * handle the formatting of the calendar
     * @param date
     * @param position
     */
    private void formatDate(Calendar date, int position) {
        String selectedDateStr = DateFormat.format(TEXTVIEW_DATE_FORMAT, date).toString();
        mCalendarDate.setText(selectedDateStr);
        Timber.i("onDateSelected: %s - Position = %s", selectedDateStr ,position);
    }

    // private helper methods
    private void setGreeting() {
        String greetingString;
        greetingString = getString(R.string.not_logged_in);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(requireActivity());
        if (null == acct) {
            Timber.w("setGreeting: Not signed in!");
        }
        if (acct != null) {
//            String personName = acct.getDisplayName();
            greetingString = acct.getGivenName();
//            String personFamilyName = acct.getFamilyName();
//            String personEmail = acct.getEmail();
//            String personId = acct.getId();
//            Uri personPhoto = acct.getPhotoUrl();
        }

        mGreeting.setText(getString(R.string.greeting, greetingString));
    }


    /**
     * this will be called when the calendar needs to be reset due to date change
     */
    private void updateCalendarDate(Calendar newEndDate) {

        Timber.d("Set new calender end date: %s",newEndDate.toString());

        Calendar newStartDate = CalendarUtilities.today();
        newStartDate.setTimeInMillis(newEndDate.getTimeInMillis());
        newStartDate.add(Calendar.MONTH, -1);

        // in case the selected date would now be out of range, keep it as an exception.
        Calendar selectedDate = mViewModel.getSelectedDate().getValue();
        if (selectedDate.before(newStartDate)) {
            newStartDate.setTimeInMillis(selectedDate.getTimeInMillis());
        }

        mHorizontalCalendar.setRange(newStartDate, newEndDate);
        mHorizontalCalendar.refresh();

        mHorizontalCalendar.selectDate(newEndDate, true);
    }

    private void handleCalendar() {
        /* starts before 1 month from now */
        Calendar startDate = CalendarUtilities.today();
        startDate.add(Calendar.MONTH, -1);

        /* ends today */
        Calendar endDate = CalendarUtilities.today();

        // initialize calendar to default to today
        final Calendar defaultDate = mViewModel.getSelectedDate().getValue();
        // and prime field with today's date.
        formatDate(defaultDate,0);

        // the configuration of the calendar
        HorizontalCalendar.Builder builder = new HorizontalCalendar.Builder(requireActivity(), R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .defaultSelectedDate(defaultDate);


//        builder.configure()
//            .formatMiddleText("EEE, MMM dd")
//            .showTopText(false)
//            .showBottomText(false)
//        .end();

        mHorizontalCalendar = builder.build();

        mHorizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                formatDate(date, position); // handle the calendar display field.
                mViewModel.commitMood(requireActivity()); // flush the current mood to permanent storage if necessary
                mViewModel.selectMood(date);
            }

        });
    }

    /**
     * set up interactions with the mood slider seek bar.
     */
    @SuppressLint("ClickableViewAccessibility")
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
    private void determineIfMoodSliderShouldBeLocked(Mood mood) {
        // the slider unlocked flag is on -- have actively changed the value. don't lock.
        if (mKeepSliderUnlocked) {
            mToggleLockSlider.setChecked(false);
            return;
        }

        // if day isn't current
        boolean locked = false;

        if (null == mood) {
            // this shouldn't happen
            Timber.w("Mood in getSelectedMood() is null.");
            return;
        }

        Timber.d("determineIfMoodSliderShouldBeLocked: %s",mood.toString());
        if (mood.getCalendarDate().before(CalendarUtilities.today())) {
            // not the current date.
            locked = true;
        } else if (! mood.isEmpty()){
            // if there is already a mood score
            locked = true;
        }

        mToggleLockSlider.setChecked(locked);
    }

    // helper to lock and commit the mood score. will be called whenever
    // there has been a new/changed score
    private void lockAndCommitMood() {
        mKeepSliderUnlocked = false;
        mViewModel.commitMood(requireActivity()); // put the new score in the database
        // and in the case we destroy the fragment
        if (null != mToggleLockSlider) {
            mToggleLockSlider.setChecked(true); // lock the slider
        }
    }

    private void handleMoodSlider() {
        mMoodSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            // --Commented out by Inspection (2018/11/8, 13:13):private TimerTask currentTimerTask;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {

                   //
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Timber.i("handleMoodSlider: Start tracking touch gesture");
                // clear any previous pending mood commits and put this new one on.
                mTimerHandler.removeCallbacks(mDelayedMoodCommit);
                mKeepSliderUnlocked = true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Timber.i("handleMoodSlider: Stop tracking touch gesture");
                int finalMoodScore = seekBar.getProgress() + Mood.MOOD_MINIMUM; // this will renormalize the mood score
                mViewModel.setSelectedMoodScore(finalMoodScore);
                mTimerHandler.postDelayed( mDelayedMoodCommit, UPDATE_DELAY);
            }
        });

        // as we start up, match the state of the slider to the state of the lock.
        lockMoodSlider(mToggleLockSlider.isChecked());

        mToggleLockSlider.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // match the lock on the mood slider
                lockMoodSlider(isChecked);

                // assume that if the user clicks on the lock they want to commit the mood score.
                if (isChecked) {
                    mViewModel.commitMood(requireActivity());
                }
            }
        });
    }

    /**
     * this function is called as the database goes on and offline.  this will hopefully get smarter as time goes by
     * @param isOnline
     */
    private void handleIfOnline(boolean isOnline) {
        Timber.d("handleIfOnline: %b", isOnline);
        if (isOnline) {
            mMoodFragmentLayout.setVisibility(View.VISIBLE);
            mMoodDataOffline.setVisibility(View.GONE);
        } else {
            mMoodFragmentLayout.setVisibility(View.GONE);
            mMoodDataOffline.setVisibility(View.VISIBLE);
        }
    }
}
