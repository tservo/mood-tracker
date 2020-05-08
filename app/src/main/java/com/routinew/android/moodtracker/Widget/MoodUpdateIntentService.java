package com.routinew.android.moodtracker.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.routinew.android.moodtracker.Data.FirebaseRealtimeDatabase.FirebaseRealtimeDatabaseMoodRepository;
import com.routinew.android.moodtracker.POJO.Mood;

import java.util.Calendar;

import timber.log.Timber;

/**
 * this intent service will allow for updating the mood widget on demand.
 */

public class MoodUpdateIntentService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_UPDATE_MOOD_WIDGETS =
            "com.routinew.android.moodtracker.widget.action.UPDATE_MOOD_WIDGETS";


    public MoodUpdateIntentService() {
        super("MoodUpdateIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionUpdateMoodWidgets(Context context) {
        Intent intent = new Intent(context, MoodUpdateIntentService.class);
        intent.setAction(ACTION_UPDATE_MOOD_WIDGETS);

        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_MOOD_WIDGETS.equals(action)) {

                handleActionUpdateMoodWidgets();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateMoodWidgets() {
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, MoodWidget.class));

        Query query = FirebaseRealtimeDatabaseMoodRepository.getMoodQueryForWidget();

        // likely no user -- can't send a mood to the widgets
        if (null == query) {
            MoodWidget.updateMoodWidgets(MoodUpdateIntentService.this, appWidgetManager, null, appWidgetIds);
            return;
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Mood mood = dataSnapshot.getValue(Mood.class);
                // if not, create a new, empty mood as a default
                if (null == mood) {
                    mood = new Mood(Mood.EMPTY_MOOD,Calendar.getInstance());
                }
                //Now update all widgets
                MoodWidget.updateMoodWidgets(MoodUpdateIntentService.this, appWidgetManager, mood, appWidgetIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.w(databaseError.toException(), "getMoodAtDate:onCancelled");
            }

        });
    }


}
