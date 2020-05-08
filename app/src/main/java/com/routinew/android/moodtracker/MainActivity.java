package com.routinew.android.moodtracker;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.LinearLayout;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.routinew.android.moodtracker.databinding.ActivityMainBinding;


import timber.log.Timber;


public class MainActivity extends AppCompatActivity {

    /*
     The google sign-in code comes from https://developers.google.com/identity/sign-in/android/sign-in
     */
    private static final int NUM_TABS = 2; // mood, and graph
    private static final int MOOD_TAB = 0;
    private static final int GRAPH_TAB = 1;

    private ActivityMainBinding mBinding;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    TabLayout mTabLayout;

    // --Commented out by Inspection (2018/11/8, 13:13):private static final int RC_SIGN_IN = 405;
    Toolbar mToolbar;
    LinearLayout mLoggedInScreen;
    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);
        // ButterKnife.bind(this);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        // do we have a user or do we need to go back?
        updateActivity(mAuth.getCurrentUser());

        // use the new view-binding here.
        mToolbar = mBinding.toolbar;
        setSupportActionBar(mToolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = mBinding.container;
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // and the tab layout with the view pager!
        mTabLayout = mBinding.tabLayout;
        mTabLayout.setupWithViewPager(mViewPager);
        // and assign the icons

        // likely not necessary
        mLoggedInScreen = mBinding.loggedInScreen;

        TabLayout.Tab moodTab = mTabLayout.getTabAt(MOOD_TAB);
        if (null != moodTab) {
            moodTab.setIcon(R.drawable.ic_person_mood_log);
        }

        TabLayout.Tab graphTab = mTabLayout.getTabAt(GRAPH_TAB);
        if (null != graphTab) {
            graphTab.setIcon(R.drawable.ic_show_chart_black_24dp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_signout) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // helper methods for event handlers:


    /**
     * handle the logged in/logged out UI display
     *
     * @param account
     */
    private void updateActivity(FirebaseUser account) {
        if (null == account) {
            // we aren't signed in - we need to sign in
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }


    /**
     * handle the sign out
     */
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // we've signed out, so no account to pass.
                        updateActivity(null);
                    }
                });
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class SectionsPagerAdapter extends FragmentPagerAdapter {
        // member variables

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case MOOD_TAB:
                    return MoodFragment.newInstance();
                case GRAPH_TAB:
                    return GraphFragment.newInstance();

                default:
                    Timber.w("Illegal position for getItem: %d", position);
                    return null;
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case MOOD_TAB:
                    return getResources().getString(R.string.label_mood);
                case GRAPH_TAB:
                    return getResources().getString(R.string.label_graph);

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_TABS; // two pages
        }
    }
}
