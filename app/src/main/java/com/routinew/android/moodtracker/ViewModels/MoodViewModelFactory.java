package com.routinew.android.moodtracker.ViewModels;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.routinew.android.moodtracker.Data.FirebaseRealtimeDatabase.FirebaseRealtimeDatabaseMoodRepository;

/**
 * based on ud851-Exercises T09b.10 -- ViewModelFactory
 */
public class MoodViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final FirebaseRealtimeDatabaseMoodRepository mFirebaseRealtimeDatabaseMoodRepository;

    public MoodViewModelFactory(FirebaseRealtimeDatabaseMoodRepository firebaseRealtimeDatabaseMoodRepository) {
        mFirebaseRealtimeDatabaseMoodRepository = firebaseRealtimeDatabaseMoodRepository;
    }

    @Override
    @NonNull public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MoodViewModel(mFirebaseRealtimeDatabaseMoodRepository);
    }
}
