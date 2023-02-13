package com.example.myfriendlocator.Util;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.myfriendlocator.Activity.ActivityLogin;
import com.example.myfriendlocator.Activity.ActivityMaps;
import com.example.myfriendlocator.Activity.ActivitySignup;
import com.example.myfriendlocator.Activity.ActivitySplashScreen;
import com.example.myfriendlocator.Fragments.MapsFragment;
import com.example.myfriendlocator.R;
import com.example.myfriendlocator.Service.LocationUpdateService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
//Class which contains a utility function which are being used in most of the activities or fragment of app
public class Util {
    public static AlertDialog alertDialog = null;
    public static void updateUI(Activity activity, Class launchActivityClass) {
        Intent intent = new Intent(activity, launchActivityClass);
        activity.startActivity(intent);
        activity.finish();
    }
//    Function which will take a uri which contains location info and it will provide share activity to share it on
//    any app to his receiver friend
    public static void shareLink(Activity activity, String uri){
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Friend Locator");
            String shareMessage= "\nMy Location. Follow it and reach to me\n";
            shareMessage = shareMessage + uri;
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            activity.startActivity(Intent.createChooser(shareIntent, "Choose app to share"));
        } catch(Exception e) {
            //e.toString();
        }
    }
    public static Bitmap getBitmap(Context context, int drawableRes) {
        @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = context.getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    /* A function which is responsible for setting a map boundary it is necessary when user is in track mode
    so that we can set a boundary in which both the sender and receiver locations are visible
    */
//    this function will move the camera as well to given CameraUpdate points
    public static void setMapViewZoomLevel(GoogleMap gmap, LatLng origin, LatLng destination) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origin);
        builder.include(destination);
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 250);
        gmap.animateCamera(cu, new GoogleMap.CancelableCallback(){
            public void onCancel(){}
            public void onFinish(){
                CameraUpdate zout = CameraUpdateFactory.zoomBy(-0f);
                gmap.moveCamera(zout);
                gmap.getUiSettings().setMapToolbarEnabled(true);
            }
        });
    }
    public static String generateTimeInHourMinutes(double time){
        if(String.valueOf(time).length()>5){
            return "Updating";
        }
        boolean isHour = true;
        String hour,minutes;
        if(time>0 && time<1){
            double timeRemaining = LocationUpdateService.timeRemaining*60;
           return ((int) timeRemaining)+ " Minutes";
        }else{
            double timeRemaining = time;
                String timeString = String.valueOf(time);
                 hour = "";
                 minutes = "";
                for(int i=0;i<timeString.length();i++){
                    if(timeString.charAt(i)=='.'){
                        isHour=false;
                    }else if(!Character.isDigit(timeString.charAt(i))){
                    }else{
                        if(isHour){
                            hour+=hour+timeString.charAt(i);
                        }else{
                            minutes+=timeString.charAt(i);
                        }
                    }
                }
            }
        if(hour.length()!=0 && Integer.parseInt(hour)>100){
            return "Updating";
        }else {
            if(minutes.length()!=0 && Integer.parseInt(minutes) >=60){
                minutes="0";
            }
            return hour + " Hour " + minutes + " Minutes";
        }
        }
    public static void contactUsAction(Context context, String number) {
        Uri uri = Uri.parse("smsto:" +"+923013518700");
        Intent i = new Intent(Intent.ACTION_SENDTO, uri);
        i.setPackage("com.whatsapp");
        context.startActivity(Intent.createChooser(i, "Contact us"));
    }
//    function which will generate a dialog which will show usr information
public static AlertDialog showProfileDialog(Context context) {
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
    LayoutInflater inflater = ((ActivityMaps) context).getLayoutInflater();
    View dialogView = inflater.inflate(R.layout.profile_layout, null);
    dialogBuilder.setView(dialogView);
    CircleImageView circleImageView = dialogView.findViewById(R.id.circleImageUserAvatar);
    TextView tvUserName = dialogView.findViewById(R.id.tvUserName);
    TextView tvUserGmail = dialogView.findViewById(R.id.tvUserGmail);
    TextView tvUserMobile = dialogView.findViewById(R.id.tvUserMobile);
    ImageButton btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
    ImageButton btnLogout = dialogView.findViewById(R.id.btnLogout);
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    if (firebaseUser.getPhotoUrl() != null) {
        Picasso.with(context).load(firebaseUser.getPhotoUrl()).into(circleImageView);
    }else{
        circleImageView.setImageResource(R.drawable.profile_icon);
    }
    if (firebaseUser.getDisplayName() != null) {
        tvUserName.setText(firebaseUser.getDisplayName());
    } else {
        tvUserName.setText("Anonymous User");
    }
    tvUserGmail.setText(firebaseUser.getEmail() + "");
    if (firebaseUser.getPhoneNumber() != null) {
        tvUserMobile.setText(firebaseUser.getPhoneNumber()+"");
    } else {
        tvUserMobile.setText("Hidden");
    }
    btnCloseDialog.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            alertDialog.cancel();
        }
    });
    btnLogout.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(context, "Logout Successfully", Toast.LENGTH_SHORT).show();
            updateUI(((ActivityMaps)context), ActivityLogin.class);
            ((ActivityMaps) context).finish();
        }
    });
    alertDialog = dialogBuilder.create();
return  alertDialog;
}
//function to generate a dialog which will allow user to send password reset link
    public static AlertDialog showPasswordResetDialog(Context context) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((ActivityLogin) context).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.reset_password, null);
        dialogBuilder.setView(dialogView);
        EditText etUserEmail = dialogView.findViewById(R.id.etUserEmail);
        Button btnResetPassword = dialogView.findViewById(R.id.btnSendPasswordResetLink);
        Button btnCancel = dialogView.findViewById(R.id.btnCloseDialog);
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etUserEmail.getText().length()==0){
                    etUserEmail.setError("Please enter your email address to continue");
                    etUserEmail.requestFocus();
                    return;
                }else if (!ActivitySignup.isValidEmail(etUserEmail.getText().toString())){
                    etUserEmail.setError("Please enter valid email address to continue");
                    etUserEmail.requestFocus();
                    return;
                }
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String emailAddress = etUserEmail.getText().toString();
                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                    Toast.makeText(context, "Password resent link sent successfully, please check your inbox to reset your password. Don't forget to check spam folder in your email as well.", Toast.LENGTH_LONG).show();
                                    alertDialog.cancel();
                                }else{
                                    Toast.makeText(context, "Error Occurred: "+task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    alertDialog.cancel();
                                }
                            }
                        });
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        alertDialog = dialogBuilder.create();
        return  alertDialog;
    }
//    function to generate dialog when user account created successfully
    public static AlertDialog showAccountCreatedDialog(Context context) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((ActivitySignup) context).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.account_created_successfully_please_verify_gmail, null);
        dialogBuilder.setView(dialogView);
        ImageButton btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
        Button btnOpenGmail = dialogView.findViewById(R.id.btnGoToGmail);
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        btnOpenGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    try {
                        Toast.makeText(context, "Open Gmail", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                        context.startActivity(intent);
                    } catch (android.content.ActivityNotFoundException anfe) {
                       Log.e("Exception",""+anfe.getMessage());
                    }
                    alertDialog.cancel();
                }
        });
        alertDialog = dialogBuilder.create();
        return  alertDialog;
    }
//    function to generate dialog which will ask user to verify email to continue
    public static AlertDialog showEmailNotVerifiedAlert(Context context) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((ActivityLogin) context).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.warning_email_not_verified, null);
        dialogBuilder.setView(dialogView);
        ImageButton btnCloseDialog =dialogView.findViewById(R.id.btnCloseDialog);
                Button btnSendVerificationLink = dialogView.findViewById(R.id.btnSendVerificationLink);
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        btnSendVerificationLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        alertDialog.cancel();
                        showVerificationEmailSendSuccessfully(context).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        alertDialog.cancel();
                        Toast.makeText(context, "Error! Please check your internet connection and error message is: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        alertDialog = dialogBuilder.create();
        return  alertDialog;
    }
    //    function to generate dialog which will tell user link submitted successfully
    public static AlertDialog showVerificationEmailSendSuccessfully(Context context) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((ActivityLogin) context).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.verification_email_send_successfully, null);
        dialogBuilder.setView(dialogView);
        ImageButton btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
        TextView btnSendVerificationLink = dialogView.findViewById(R.id.btnResendLink);
        Button btnOpenGmail = dialogView.findViewById(R.id.btnOpenGmail);
        btnSendVerificationLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Link send successfully please check your inbox", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed! Please Try Again: ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        btnOpenGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Toast.makeText(context, "Open Gmail", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                    context.startActivity(intent);
                    alertDialog.cancel();
                } catch (android.content.ActivityNotFoundException anfe) {
                    Log.e("Exception",""+anfe.getMessage());
                }

            }
        });
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        alertDialog = dialogBuilder.create();
        return  alertDialog;
    }
}
