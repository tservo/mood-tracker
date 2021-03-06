package com.routinew.android.moodtracker.Data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.routinew.android.moodtracker.POJO.Mood;
import com.routinew.android.moodtracker.Utilities.CalendarUtilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import timber.log.Timber;

/**
 * singleton class - may use Dagger 2 in the future to handle this
 * clearinghouse between the view model(s) and the data source(s)
 */
public class MoodRepository {

    // singleton instance
    private static MoodRepository sMoodRepository;

    // set up a livedata to listen to whether the database is online or not.
    private final static MutableLiveData<Boolean> sIsConnected = new MutableLiveData<>();

    public static LiveData<Boolean> databaseIsConnected() { return sIsConnected;}

    // singleton constructor
    private MoodRepository() {
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true); // offline persistence is useful for this app.
        } catch (DatabaseException e) {
            // there might be an issue with the instance being used before the persistence being enabled.
            // catch it, log it, and move on.
            Timber.w(e);
        }

        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                sIsConnected.setValue(connected);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Timber.w("Listener was cancelled at .info/connected");
            }
        });
        //mockMoods();
    }

    private static DatabaseReference getMoodDatabaseRef() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = (user == null) ? "no_user" : user.getUid();
        return FirebaseDatabase.getInstance().getReference("/users/" + uid + "/moods");
    }


    /**
     * get the singleton here
     */
    public static MoodRepository getInstance() {
        if (null == sMoodRepository) {
            sMoodRepository = new MoodRepository();
        }
        return sMoodRepository;
    }

    // these get listeners to the two queries we need.

    // utility method to map a data snapshot to a list of moods
    private List<Mood> getMoodsFromDataSnapshot(DataSnapshot dataSnapshot) {
        ArrayList<Mood> moods = new ArrayList<>();
        for (DataSnapshot child: dataSnapshot.getChildren()) {
            moods.add(child.getValue(Mood.class));
        }

        Collections.sort(moods,Mood.dateComparator());
        return moods;
    }

    /**
     * get a list of moods that correspond to a query of date specifed => today
     * @param calendar the date specified
     * @return a LiveData object that will automatically monitor the query as necessary.
     */

    public LiveData<List<Mood>> getMoodRange(Calendar calendar) {
        // make a new livedata object that follows the query specified
        Query query = getMoodDatabaseRef().orderByChild("date").startAt(CalendarUtilities.calendarToTextDate(calendar));
        final FirebaseQueryLiveData queryLiveData = new FirebaseQueryLiveData(query);
        final MediatorLiveData<List<Mood>> moodsLiveData = new MediatorLiveData<>();

        // https://firebase.googleblog.com/2017/12/using-android-architecture-components_20.html
        moodsLiveData.addSource(queryLiveData, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable final DataSnapshot dataSnapshot) {

                if (null != dataSnapshot) {
                    Timber.i("Value: %s", dataSnapshot.toString());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            moodsLiveData.postValue(getMoodsFromDataSnapshot(dataSnapshot));
                        }
                    }).run();
                } else {
                    moodsLiveData.setValue(new ArrayList<Mood>()); // nothing, just give 0 items
                }
            }
        });


        return moodsLiveData;
    }

    /**
     * get the specified Mood at the date specified.  It will be able to be modified,
     * and doesn't need to be automatically updated to listen into this query.
     *
     * @param calendar the date specified, the live data to update
     * @return a mutable live data with the mood specified.
     */
    public void getMoodAtDate(final Calendar calendar, final MutableLiveData<Mood> moodMutableLiveData) {
        Query query = getMoodDatabaseRef().child(CalendarUtilities.calendarToTextDate(calendar));
        // use this empty mood as a placeholder until we have pulled the correct date.
        moodMutableLiveData.setValue(new Mood(calendar));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                 Mood mood = dataSnapshot.getValue(Mood.class);
                 // if we found a mood, replace it with this one.
                 if (null != mood) {
                     moodMutableLiveData.setValue(mood);
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {
                 Timber.w(databaseError.toException(), "getMoodAtDate:onCancelled");
             }

         }
        );
    }

    public static Query getMoodQueryForWidget() {
        if (null == FirebaseAuth.getInstance().getCurrentUser()) {
            Timber.i("getMoodQueryForWidget: Not logged in");
            return null;
        }

        return getMoodDatabaseRef().child(CalendarUtilities.calendarToTextDate(Calendar.getInstance()));
    }

    public void writeMoodToDatabase(Mood mood) {
        DatabaseReference db = getMoodDatabaseRef();

        db.child(String.valueOf(mood.getDate())).setValue(mood, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

            }
        });
    }





//    ////////////////////////
//    // mock functions!
//
    // helper to get a hash code for the calendar
    private String dateFormat(Calendar calendar) {
        // for the hash map

        return CalendarUtilities.calendarToTextDate(calendar);
    }
    // create a list of moods for the past month

    private void mockMoods() {
        Calendar startDate = CalendarUtilities.today();
        startDate.add(Calendar.YEAR, -1);

        Calendar currentDate = CalendarUtilities.today();

        HashMap<String,Mood> mocks = new HashMap<>();

        // here we store the moods by date.
        while (startDate.before(currentDate)) {
            mocks.put(dateFormat(startDate),generateMood(startDate));
            startDate.add(Calendar.DATE, +1); // next day
        }

        // and commit them.
        DatabaseReference db = getMoodDatabaseRef();
        db.setValue(mocks);
    }



    /**
     * for getting a random mood score.
     * @return
     */
    private static int generateRandomMoodScore() {
        return new Random().nextInt((Mood.MOOD_MAXIMUM - Mood.MOOD_MINIMUM) + 1) + Mood.MOOD_MINIMUM;
    }

    /**
     * test method for generating moods
     * @param date
     * @param forceEmptyMood return an empty mood instead of a random mood score
     * @return generated mood for date.
     */
    private static Mood generateMood(Calendar date, boolean forceEmptyMood) {

        if (forceEmptyMood) {
            return new Mood(date);
        }

        return new Mood(generateRandomMoodScore(), date);
    }

    private static Mood generateMood(Calendar date) {
        return generateMood(date, false);
    }
}
