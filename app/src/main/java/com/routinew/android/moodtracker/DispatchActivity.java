package com.routinew.android.moodtracker;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import okhttp3.OkHttpClient;
import timber.log.Timber;

public class DispatchActivity extends AppCompatActivity {
/*
idea from https://android.jlelse.eu/login-and-main-activity-flow-a52b930f8351
 */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // install Timber Tree
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());

            // add the stetho diagnostic tools
            Stetho.initializeWithDefaults(this);
            new OkHttpClient.Builder()
                    .addNetworkInterceptor(new StethoInterceptor())
                    .build();
        }

        Intent activityIntent;
        // go straight to main if a token is stored
        if (null != user) {
            activityIntent = new Intent(this, MainActivity.class);
        } else {
            activityIntent = new Intent(this, LoginActivity.class);
        }

        startActivity(activityIntent);
        finish();
    }
}
