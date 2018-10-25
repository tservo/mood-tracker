package com.routinew.android.moodtracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.LinearLayout;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import timber.log.Timber;


public class MainActivity extends AppCompatActivity {

    /*
     The google sign-in code comes from https://developers.google.com/identity/sign-in/android/sign-in
     */
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @BindView(R.id.container)  ViewPager mViewPager;

    private static final int NUM_TABS = 2; // mood, and graph
    private static final int MOOD_TAB = 0;
    private static final int GRAPH_TAB = 1;

    private static final int RC_SIGN_IN = 405;

    @BindView(R.id.tab_layout)  TabLayout mTabLayout;
    @BindView(R.id.toolbar)  Toolbar mToolbar;
    @BindView(R.id.signin) SignInButton mSignInButton;
    @BindView(R.id.sign_out_button) MaterialButton mSignoutButton;
    @BindView(R.id.signin_screen) LinearLayout mSigninScreen;
    @BindView(R.id.logged_in_screen) LinearLayout mLoggedInScreen;

    @OnClick({R.id.signin, R.id.sign_out_button}) public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signin:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            // ...
        }
    }

    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // install Timber Tree
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());

            // add the stetho diagnostic tools
            Stetho.initializeWithDefaults(this);
            new OkHttpClient.Builder()
                    .addNetworkInterceptor(new StethoInterceptor())
                    .build();
        }

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();



        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager() );

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // and the tab layout with the view pager!
        mTabLayout.setupWithViewPager(mViewPager);
        // and assign the icons

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
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // helper methods for event handlers:


    /**
     * handle what happened with the sign in result
     * @param completedTask
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Timber.w("signInResult:failed code = %d", e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (null == account) {
            // we aren't signed in - we need to sign in
            mLoggedInScreen.setVisibility(View.GONE);
            mSigninScreen.setVisibility(View.VISIBLE);
            mSignoutButton.setVisibility(View.GONE);
        } else {
            // we're signed in
            mLoggedInScreen.setVisibility(View.VISIBLE);
            mSigninScreen.setVisibility(View.GONE);
            mSignoutButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * handle the sign in
     */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // we've signed out, so no account to pass.
                        updateUI(null);
                    }
                });
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        // member variables



        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case MOOD_TAB: return MoodFragment.newInstance();
                case GRAPH_TAB: return GraphFragment.newInstance();

                default:
                    Timber.w("Illegal position for getItem: %d",position);
                    return null;
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case MOOD_TAB: return getResources().getString(R.string.label_mood);
                case GRAPH_TAB: return getResources().getString(R.string.label_graph);

                default: return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_TABS; // two pages
        }
    }
}
