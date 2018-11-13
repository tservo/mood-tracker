package com.routinew.android.moodtracker.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.widget.RemoteViews;

import com.routinew.android.moodtracker.DispatchActivity;
import com.routinew.android.moodtracker.POJO.Mood;
import com.routinew.android.moodtracker.R;

import timber.log.Timber;

/**
 * Implementation of App widget functionality.
 */
public class MoodWidget extends AppWidgetProvider {

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        Mood mood, int appWidgetId) {


        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.mood_widget);

        updateWidgetLayoutFromMood(context, views, mood);

        // this should log us into the app.
        Intent appIntent = new Intent(context,DispatchActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                0);
        views.setOnClickPendingIntent(R.id.widget_top, appPendingIntent);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    /**
     * handles updating the widget layout from the mood specified.
     * @param context
     * @param views
     * @param mood
     */
    private static void updateWidgetLayoutFromMood(Context context, RemoteViews views, Mood mood) {
        int moodIconResId;
        int actionIconResId;

        Resources res = context.getResources();
        int backgroundColor = R.color.moodEmpty;
        String contentDescription;

        if (null == mood || mood.isEmpty()) {
            moodIconResId = R.drawable.ic_person_mood_log;
            actionIconResId = R.drawable.ic_add_black_24dp;
            contentDescription = context.getString( (null == mood) ? R.string.not_logged_in : R.string.mood_not_set);
        } else {
            int moodScore = mood.getMoodScore();

            if (moodScore < -2) {
                moodIconResId = R.drawable.ic_sentiment_dissatisfied_black_24dp;
            } else if (moodScore < 1) {
                moodIconResId=R.drawable.ic_sentiment_neutral_black_24dp;
            } else if (moodScore < 3) {
                moodIconResId=R.drawable.ic_sentiment_satisfied_black_24dp;
            } else {
                moodIconResId = R.drawable.ic_sentiment_very_satisfied_black_24dp;
            }
            actionIconResId = R.drawable.ic_edit_black_24dp;

            // the resource arrays.
           TypedArray colors = res.obtainTypedArray(R.array.mood_colors);
           String[] moodDescriptions = res.getStringArray(R.array.mood_scale);

            // normalize to 0..10 for array
            int indexedScore = moodScore - Mood.MOOD_MINIMUM; // reset to a 0-10 scale


            backgroundColor = colors.getResourceId(indexedScore, R.color.moodEmpty);
            colors.recycle();

            Timber.d("updateWidgetLayoutFromMood: backgroundColor: %s", backgroundColor);
        }

        setImageViewVectorDrawable(context, views, R.id.appwidget_moodIcon, moodIconResId);
       // views.setString(R.id.appwidget_moodIcon, "setContentDescription", contentDescription);
        setImageViewVectorDrawable(context, views, R.id.appwidget_action, actionIconResId);

        setBackground(context, views, backgroundColor);

    }

    /**
     * helper method to generate the background and to tint the other views correctly.
     * @param context
     * @param views
     * @param backgroundColor
     */
    private static void setBackground(Context context, RemoteViews views, int backgroundColor) {
        int backgroundRGB = context.getResources().getColor(backgroundColor);

        int contrastColor = (ColorUtils.calculateLuminance(backgroundRGB) < 0.5) ?
                Color.WHITE: Color.BLACK;

        views.setInt(R.id.appwidget_moodIcon,"setColorFilter",contrastColor);
        views.setInt(R.id.appwidget_action, "setColorFilter", contrastColor);

        views.setInt(R.id.appwidget_background,"setBackgroundResource", backgroundColor);
    }

    /**
     * helper method: this trick comes from
     * https://stackoverflow.com/questions/35633410/appcompat-23-2-use-vectordrawablecompat-with-remoteviews-appwidget-on-api21
     * @param context
     * @param views
     * @param viewResId the resource id of the view in which to draw
     * @param drawableResId the resource id of the vector drawable
     */
    private static void setImageViewVectorDrawable(Context context, RemoteViews views,
                                                   int viewResId, int drawableResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            views.setImageViewResource(viewResId, drawableResId);
        } else {
            Drawable d = ContextCompat.getDrawable(context, drawableResId);
            if (null != d) {
                Bitmap b = Bitmap.createBitmap(d.getIntrinsicWidth(),
                        d.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(b);
                d.setBounds(0, 0, c.getWidth(), c.getHeight());
                d.draw(c);
                views.setImageViewBitmap(viewResId, b);
            }
        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        MoodUpdateIntentService.startActionUpdateMoodWidgets(context);
    }

    public static void updateMoodWidgets(Context context, AppWidgetManager appWidgetManager, Mood mood, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, mood, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

