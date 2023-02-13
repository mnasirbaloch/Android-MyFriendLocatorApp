package com.example.myfriendlocator.Activity;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfriendlocator.R;
import com.example.myfriendlocator.Util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class ActivitySignup extends AppCompatActivity {
    EditText etUserName,etUserGmail,etUserPassword,etUserPasswordVerify;
    Button btnSignUp;
    FirebaseAuth mAuth;
    TextView btnGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        try {
            ColorDrawable colorDrawable
                    = new ColorDrawable(getResources().getColor(R.color.defaultColor));
            // Set BackgroundDrawable
            getSupportActionBar().setBackgroundDrawable(colorDrawable);
        }catch (Exception e){Log.e("Exception",e.getMessage()+"");}
        initializeViws();
        mAuth = FirebaseAuth.getInstance();
        setEventHandler();
    }

    //    initializing all view
    public void initializeViws(){
        etUserName = findViewById(R.id.etUserName);
        etUserGmail = findViewById(R.id.etUserEmail);
        etUserPassword = findViewById(R.id.etUserPassword);
        etUserPasswordVerify = findViewById(R.id.etUserPasswordVerify);
        btnSignUp=findViewById(R.id.btnSignup);
        btnGoToLogin=findViewById(R.id.btnGoToLoginActivity);
    }
    //    validate edittext
    public String validateField(EditText editText){
        if(editText.getText().length()==0){
            editText.setFocusable(true);
            editText.requestFocus();
            editText.setError("Please enter valid information");
            return null;
        }else{
            return editText.getText().toString();
        }
    }
    //    signup button click listener
    public void createUser() {
        String name = validateField(etUserName);
        if(name==null){return;}
        String email = validateField(etUserGmail);
        if(email== null){return;}
        String password = validateField(etUserPassword);
        if(password==null){return;}
        String confirmPassword = validateField(etUserPasswordVerify);
        if(confirmPassword==null){return;}
        if(password.length()<6){
            etUserPassword.setFocusable(true);
            etUserPassword.requestFocus();
            etUserPassword.setError("Password must be greater than 6 characters");
            return;
        }else if(!password.equals(confirmPassword)){
            etUserPasswordVerify.requestFocus();
            etUserPasswordVerify.setError("Password did not matches, please check it again");
        }else
        {
            if (isValidEmail(email)) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.e("SignUpLog", "Account Created Successfully. Please Login to Continue");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if(user!=null) {
                                       user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(ActivitySignup.this, "Verification Email send successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        Util.showAccountCreatedDialog(ActivitySignup.this).show();
                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.e("SignUpLog", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(ActivitySignup.this, " Account not created -> " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                                }
                            }
                        });
            }else{
                etUserGmail.requestFocus();
                etUserGmail.setError("Invalid Email. Please provide valid email");
            }
        }
    }

//    button click listener
    public void setEventHandler(){
        btnGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.updateUI(ActivitySignup.this,ActivityLogin.class);

            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });
    }
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}