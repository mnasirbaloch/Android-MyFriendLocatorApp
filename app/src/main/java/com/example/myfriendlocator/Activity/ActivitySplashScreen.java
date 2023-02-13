package com.example.myfriendlocator.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfriendlocator.R;
import com.example.myfriendlocator.Util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
public class ActivitySplashScreen extends AppCompatActivity {
    Thread background;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
         background = new Thread() {
            public void run() {
                try {
                    // Thread will sleep for 5 seconds
                    sleep(2000);
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                        Util.updateUI(ActivitySplashScreen.this, ActivityMaps.class);
                        finish();
                    } else {
                        Util.updateUI(ActivitySplashScreen.this, ActivityLogin.class);
                        finish();
                    }
                } catch (Exception e) {
                    Log.e("SplashScreen Exception",""+e.getMessage());
                }
            }
        };
        background.start();
    }
    @Override
    public void finish() {
        try{
            background.stop();
        }catch (Exception e){Log.e("Exception",""+e.getMessage());
        }
        super.finish();
    }
}