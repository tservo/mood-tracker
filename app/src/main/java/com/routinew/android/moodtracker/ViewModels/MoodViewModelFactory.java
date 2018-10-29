package com.routinew.android.moodtracker.ViewModels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.routinew.android.moodtracker.Data.MoodRepository;

/**
 * based on ud851-Exercises T09b.10 -- ViewModelFactory
 */
public class MoodViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final MoodRepository mMoodRepository;

    public MoodViewModelFactory(MoodRepository moodRepository) {
        mMoodRepository = moodRepository;
    }

    @Override
    @NonNull public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MoodViewModel(mMoodRepository);
    }
}
