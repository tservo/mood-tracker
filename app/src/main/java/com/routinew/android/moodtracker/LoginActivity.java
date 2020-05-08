package com.routinew.android.moodtracker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.routinew.android.moodtracker.Data.FirebaseRealtimeDatabase.FirebaseRealtimeDatabaseMoodRepository;
import com.routinew.android.moodtracker.Utilities.QuoteOfDayConnector;
import com.routinew.android.moodtracker.databinding.ActivityLoginBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {

    private static final int RC_SIGN_IN = 405;


    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    private ActivityLoginBinding mBinding;

    // UI references.
    private SignInButton mSignInButton;
    ImageView mBackgroundView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        //updateActivity(mAuth.getCurrentUser());

        // here we showcase an asynctask to load a background image from quote of the day
        // FIXME put text to attribute it.
        mSignInButton = mBinding.signin;
        mSignInButton.setEnabled(false);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 signIn();
                                             }
                                         }

        );

        mBackgroundView = mBinding.imageBackground;
        new AsyncTask<Void,Void,String>() {

            @Override
            protected String doInBackground(Void... voids) {
                return QuoteOfDayConnector.getBackgroundURL(LoginActivity.this);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (null == s) {
                    Timber.w("onPostExecute: no background URL found!");
                    Toast.makeText(LoginActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                    mSignInButton.setEnabled(true); // couldn't find the background pic, try to show the login button anyway.
                }
                if (null != s) {
                    Glide.with(LoginActivity.this)
                            .load(s)
                            .into(mBackgroundView);

                    mSignInButton.setEnabled(true);
                }


            }
        }.execute();

    }

    /**
     * handle the logged in/logged out activity display
     * @param account
     */
    private void updateActivity(FirebaseUser account) {
        if (null != account) {
            // we aren't signed in - we need to sign in
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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

    /**
     * handle what happened with the sign in result
     * @param completedTask the google signin result task
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        // possibilities
        // 1. not signed in to google account
        // 2. google account not tied to firebase user account
        // 3. google account -> firebase user account. good.
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Timber.w("signInResult:failed code = %d", e.getStatusCode());
            updateActivity(null);
        }
    }

    /**
     * handle steps to authorize to firebase with google account
     * @param account the google signin account needed
     */
    private void firebaseAuthWithGoogle(@Nullable GoogleSignInAccount account) {
        // in the case we couldn't get a google account to authorize with
        if (null == account) {
            Timber.w("firebaseAuthWithGoogle: no google account to authorize");
            return;
        }

        Timber.d("firebaseAuthWithGoogle: %s", account.getId());


        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // sign in success
                            Timber.d("signInWithCredential: success");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            // get a proper repository set up
                            FirebaseRealtimeDatabaseMoodRepository.getInstance();

                            // and here we go
                            updateActivity(firebaseUser);

                        } else {
                            Timber.w("signInWithCredential: failure");
                            updateActivity(null);
                        }
                    }
                });

    }



}

