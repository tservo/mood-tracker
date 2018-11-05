package com.routinew.android.moodtracker.Widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.routinew.android.moodtracker.Data.MoodRepository;
import com.routinew.android.moodtracker.POJO.Mood;

import java.util.Calendar;
import java.util.Random;

import timber.log.Timber;

/**
 * this intent service will allow for updating the mood widget on demand.
 */

public class MoodUpdateIntentService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_UPDATE_MOOD_WIDGETS =
            "com.routinew.android.moodtracker.Widget.action.UPDATE_MOOD_WIDGETS";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.routinew.android.moodtracker.Widget.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.routinew.android.moodtracker.Widget.extra.PARAM2";

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
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_MOOD_WIDGETS.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionUpdateMoodWidgets();
            } // else if (ACTION_BAZ.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//                handleActionBaz(param1, param2);
//            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateMoodWidgets() {
        // TODO: Handle action Foo
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, MoodWidget.class));

        Query query = MoodRepository.getMoodQueryForWidget();

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
                    mood = new Mood(Calendar.getInstance());
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
