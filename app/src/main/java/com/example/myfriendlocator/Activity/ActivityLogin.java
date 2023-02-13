package com.example.myfriendlocator.Activity;
import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfriendlocator.R;
import com.example.myfriendlocator.Util.Util;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;
public class ActivityLogin extends AppCompatActivity {
    EditText etUserEmail, etUserPassword;
    TextView tvSignup,btnForgotPassword;
    Button btnLogin;
    SignInButton bt_sign_in;
    BeginSignInRequest signInRequest;
    private FirebaseAuth mAuth;
    SignInClient oneTapClient;
    boolean isClickAble=true;
    static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        Changing the color of support action bar
        try {
            ColorDrawable colorDrawable
                    = new ColorDrawable(getResources().getColor(R.color.defaultColor));
            // Set BackgroundDrawable
            getSupportActionBar().setBackgroundDrawable(colorDrawable);
        }catch (Exception e){Log.e("Exception",e.getMessage()+"");}
        initializeView();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        bt_sign_in = findViewById(R.id.bt_sign_in);
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.default_web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();
        setEventHandler();

    }
    //    function to initialize all the views
    private void initializeView() {
        etUserEmail = findViewById(R.id.etUserEmail);
        etUserPassword = findViewById(R.id.etUserPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvCreateAccount);
        btnForgotPassword=findViewById(R.id.tvForgotPassword);
    }
    //    function to sing-in user using email and password
    public void loginUserWithEmailPass() {
//    lets check if user is already signed-in
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mAuth.signOut();
        } else {
            String email = validateField(etUserEmail);
            if(email==null || !ActivitySignup.isValidEmail(email)){
                etUserEmail.requestFocus();
                etUserEmail.setError("Please enter correct email address");
                return;
            }
            String password = validateField(etUserPassword);
            if(password==null){
                etUserPassword.requestFocus();
                etUserPassword.setError("Password can't be empty");
                return;
            }
            btnLogin.setVisibility(View.INVISIBLE);
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    btnLogin.setVisibility(View.VISIBLE);
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "Logged In Successfully");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if(user.isEmailVerified()){
                                        Toast.makeText(ActivityLogin.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                                        Util.updateUI(ActivityLogin.this, ActivitySplashScreen.class);
                                    btnLogin.setVisibility(View.VISIBLE);
                                } else {
                                        Util.showEmailNotVerifiedAlert(ActivityLogin.this).show();
                                    }
                                }else{
                                        // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(ActivityLogin.this, "Login Failure -> " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    btnLogin.setVisibility(View.VISIBLE);
                                    }
                            }
                        });
        }
    }
    //    method to check if given edittext field is empty or not, if not then simply return the value of that edit text
    public String validateField(EditText editText) {
        if (editText.getText().length() == 0) {
            editText.setFocusable(true);
            editText.requestFocus();
            editText.setError("Please enter valid information");
            return null;
        } else {
            return editText.getText().toString();
        }
    }
    //  function which will set event listener for all the button available on ActivityLogin
    public void setEventHandler() {
        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.updateUI(ActivityLogin.this, ActivitySignup.class);
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserWithEmailPass();
            }
        });
        bt_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isClickAble) {
                    googleSignInImplementation();
                }else{
                    Toast.makeText(ActivityLogin.this, "In Progress Please Wait", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showPasswordResetDialog(ActivityLogin.this).show();
            }
        });
    }

//    overriding onStart method to check if user is loggedin or not in case user is logged in simply shift user to main activity
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Toast.makeText(this, "verified", Toast.LENGTH_SHORT).show();
            Util.updateUI(ActivityLogin.this, ActivitySplashScreen.class);
        }else{
            Toast.makeText(this, "unverified", Toast.LENGTH_SHORT).show();
        }
    }
//    Implementing Google OneTap SignIn
    public void googleSignInImplementation() {
        isClickAble = false;
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        try {
                            startIntentSenderForResult(
                                    result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                                    null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            isClickAble=true;
                            Log.e(TAG, "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // No Google Accounts found. Just continue presenting the signed-out UI.
                        Log.d(TAG, e.getLocalizedMessage()+"");
                        Toast.makeText(ActivityLogin.this, "Attempt Failed! Please Try Again", Toast.LENGTH_SHORT).show();
                        isClickAble=true;
                    }
                });


    }

//OnActivityResult will handle all the response generate by the permission launcher
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_ONE_TAP:
                try {
                    SignInCredential googleCredential = oneTapClient.getSignInCredentialFromIntent(data);
                    String idToken = googleCredential.getGoogleIdToken();
                    if (idToken !=  null) {
                        // Got an ID token from Google. Use it to authenticate
                        // with Firebase.
                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                        mAuth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "signInWithCredential:success");
                                            isClickAble=true;
                                            Util.updateUI(ActivityLogin.this, ActivitySplashScreen.class);
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                                            Toast.makeText(ActivityLogin.this, "Error Occurred! Check your internet connection", Toast.LENGTH_SHORT).show();
                                            isClickAble=true;
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ActivityLogin.this, "Error Occurred! Please Try Again", Toast.LENGTH_SHORT).show();
                                        isClickAble=true;
                                    }
                                });
                    }else{
                        Toast.makeText(this, "Error Occured! Please Try Again", Toast.LENGTH_SHORT).show();
                        isClickAble=true;
                    }
                } catch (ApiException e) {
                    isClickAble=true;
                    // ...
                }
                break;
        }
    }

}

