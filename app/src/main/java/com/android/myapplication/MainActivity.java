package com.android.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "EmailPassword";
    private TextView mStatusTextView, mDetailTextView, createAcc, loginTV, heading;
    private EditText mEmailField, mPasswordField;
    private Button loginBtn, registerBtn, signOutBtn, verifyEmailBtn;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //let's connect our elements' nicknames with elements
        mStatusTextView = (TextView) findViewById(R.id.mStatusTextView);
        mDetailTextView = (TextView) findViewById(R.id.mDetailTextView);
        mEmailField     = (EditText) findViewById(R.id.mEmailField);
        mPasswordField  = (EditText) findViewById(R.id.mPasswordField);
        loginBtn        = (Button)   findViewById(R.id.login);
        registerBtn     = (Button)   findViewById(R.id.register);
        createAcc       = (TextView) findViewById(R.id.createAccTV);
        loginTV         = (TextView) findViewById(R.id.loginTV);
        heading         = (TextView) findViewById(R.id.heading);
        signOutBtn      = (Button)   findViewById(R.id.signOut);
        verifyEmailBtn  = (Button)   findViewById(R.id.verifyEmail);

        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email    = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();
                signIn(email,password);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email    = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();
                createAccount(email,password);
            }
        });

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });


        verifyEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmailVerification();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private boolean validateForm(){
        boolean valid  = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)){
            mEmailField.setError("Required!");
            valid = false;
        }else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)){
            mPasswordField.setError("Required!");
            valid = false;
        }else {
            mPasswordField.setError(null);
        }
        return valid;
    }

    private void signIn(String email, String password){
        if (!validateForm()){
            return;
        }

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mStatusTextView.setText(getString(R.string.success));
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                    if (!task.isSuccessful()) {
                        mStatusTextView.setText(getString(R.string.auth_error));
                        updateUI(null);
                    }
                }
            });
    }

    private void createAccount(String email, String password){
        if (!validateForm()) {
            return;
        }

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        // [END_EXCLUDE]
                    }
                });
    }

    public void alreadyRegistered(View view){
        heading.setText(getString(R.string.register_screen));
        loginTV.setVisibility(View.VISIBLE);
        registerBtn.setVisibility(View.VISIBLE);
        createAcc.setVisibility(View.GONE);
        loginBtn.setVisibility(View.GONE);
        mStatusTextView.setText("");
        mDetailTextView.setText("");
    }

    public void loginScreen(View view){
        heading.setText(getString(R.string.login_screen));
        loginTV.setVisibility(View.GONE);
        registerBtn.setVisibility(View.GONE);
        createAcc.setVisibility(View.VISIBLE);
        loginBtn.setVisibility(View.VISIBLE);
        mStatusTextView.setText("");
        mDetailTextView.setText("");
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.signUpInStuff).setVisibility(View.GONE);
            findViewById(R.id.accountStuff).setVisibility(View.VISIBLE);

            heading.setText(R.string.account_screen);

            if (!user.isEmailVerified())
                verifyEmailBtn.setVisibility(View.VISIBLE);
            else
                verifyEmailBtn.setVisibility(View.GONE);
        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.signUpInStuff).setVisibility(View.VISIBLE);
            findViewById(R.id.accountStuff).setVisibility(View.GONE);
            heading.setText(getString(R.string.login_screen));
        }
    }

    private void sendEmailVerification() {
        // Disable button
        verifyEmailBtn.setEnabled(false);

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button
                        verifyEmailBtn.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,
                                    "Verification email sent to " + user.getEmail() + ". Please sign in after verifying your email.",
                                    Toast.LENGTH_SHORT).show();
                            signOut();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(MainActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    /*private void updateUI(FirebaseUser user){
        if (user != null){

        }

    }*/
}
