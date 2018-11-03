package com.routinew.android.moodtracker;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class WidgetJobIntentService extends JobIntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.routinew.android.moodtracker.action.FOO";
    private static final String ACTION_BAZ = "com.routinew.android.moodtracker.action.BAZ";

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 7777;

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.routinew.android.moodtracker.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.routinew.android.moodtracker.extra.PARAM2";

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, WidgetJobIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

    }

}
