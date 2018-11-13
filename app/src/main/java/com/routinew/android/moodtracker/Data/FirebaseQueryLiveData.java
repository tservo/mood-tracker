package com.routinew.android.moodtracker.Data;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import timber.log.Timber;

/**
 * This class is from
 * https://firebase.googleblog.com/2017/12/using-android-architecture-components.html
 */

class FirebaseQueryLiveData extends LiveData<DataSnapshot> {

    private final Query query;
    private final MyValueEventListener listener = new MyValueEventListener();

    public FirebaseQueryLiveData(Query query) {
        this.query = query;
    }

// --Commented out by Inspection START (2018/11/8, 13:13):
//    public FirebaseQueryLiveData(DatabaseReference ref) {
//        this.query = ref;
//    }
// --Commented out by Inspection STOP (2018/11/8, 13:13)

    @Override
    protected void onActive() {
        Timber.d("onActive");
        query.addValueEventListener(listener);
    }

    @Override
    protected void onInactive() {
        Timber.d( "onInactive");
        query.removeEventListener(listener);
    }

    private class MyValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            setValue(dataSnapshot);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Timber.e(databaseError.toException(), "Can't listen to query %s", query);
        }
    }
}
