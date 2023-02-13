package com.example.myfriendlocator.Activity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfriendlocator.Fragments.MapsFragment;
import com.example.myfriendlocator.Interface.LocationTracker;
import com.example.myfriendlocator.Model.TrackRecord;
import com.example.myfriendlocator.R;
import com.example.myfriendlocator.Service.LocationUpdateService;
import com.example.myfriendlocator.Util.Util;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
public class ActivityMaps extends AppCompatActivity implements LocationTracker {
    private static final int FINE_PERMISSION_CODE = 01;
    private static final int COARSE_PERMISSION_LOCATION = 02;
    public static Intent trackIntent;
    public static Marker travellerMarker = null;
    public static  Circle circle;
    FragmentManager fragmentManager;
    String keyText;
    boolean isMyLocationClickable = true;
    int key;
    FragmentTransaction fragmentTransaction;
    Button btnMyLocation, btnShareMyLocation, btnMyProfile, btnTrackLocation, btnClearAll;
    LocationManager manager;
    FusedLocationProviderClient fusedLocationProviderClient;
    TextView tvTimeLeft, tvDistance, tvLabelTimeLeft, tvLabelDistance,tvSeperator,btnReCenter;
    Location senderLocation = null;
    AlertDialog alertDialog;
    LocationCallback mLocationCallback;
    DatabaseReference database;
    public static GoogleMap gMap;
    boolean checkDeepLink = true;
    Timer t;
    Uri deepLink;
    boolean isTrackable = false;
    public static LatLng senderLatLng;
    public static boolean isServiceRunning = false;
    boolean isMarkSet = false;
    public static boolean shouldShowFinishDialog = false;
    public static boolean canSetShowDialog = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Objects.requireNonNull(getSupportActionBar()).hide();
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentContainer, new MapsFragment(), "mapFragment");
        fragmentTransaction.commit();
        initializeView();
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
//        currentLocationUpdateListener();
        currentLocationUpdateListener2();
        database = FirebaseDatabase.getInstance().getReference("TrackData");
//        setting unnecessary fields visibility as gone
        tvDistance.setVisibility(View.GONE);
        tvTimeLeft.setVisibility(View.GONE);
        tvLabelDistance.setVisibility(View.GONE);
        tvLabelTimeLeft.setVisibility(View.GONE);
        buttonVisibilityNonTrackMode();
        t = new Timer();
        buildEndTrackingDialog();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    //The last location in the list is the newest
                    Location location = locationList.get(locationList.size() - 1);
                    ((MapsFragment) fragmentManager.findFragmentByTag("mapFragment")).setLocation(location);
                    senderLocation = location;
                    btnShareMyLocation.setVisibility(View.VISIBLE);
                }
            }
        };

        btnShareMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareLocation();
            }
        });
        btnMyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showProfileDialog(ActivityMaps.this).show();
            }
        });

    }

    //    Initializing the view declared in xml file (layout file)
    public void initializeView() {
        btnMyLocation = findViewById(R.id.btnMyCurrentLocation);
        btnShareMyLocation = findViewById(R.id.btnShareInfo);
        btnMyProfile = findViewById(R.id.btnShowMyProfile);
        btnTrackLocation = findViewById(R.id.btnTrackMe);
        btnClearAll = findViewById(R.id.btnClearAll);
        tvTimeLeft = findViewById(R.id.tvTimeDuration);
        tvDistance = findViewById(R.id.tvDistance);
        tvLabelTimeLeft = findViewById(R.id.tvLabelTimeDuration);
        tvLabelDistance = findViewById(R.id.tvLabelDistance);
        tvSeperator = findViewById(R.id.tvSeperator);
        btnReCenter = findViewById(R.id.btnRecenter);
        if (senderLocation == null) {
            btnShareMyLocation.setVisibility(View.GONE);
        }
        setTrackMeListener();
        setClearAllListener();
        setRecenterLinstener();
    }

    public void setRecenterLinstener(){
        btnReCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    gMap.animateCamera(CameraUpdateFactory.newLatLng(LocationUpdateService.polyline.getPoints().get(LocationUpdateService.polyline.getPoints().size() - 1)));
                }catch (Exception e) {
                    Log.e("recenter exception: ", e.getMessage() + "");
                }
                }

        });
    }
    //    function nto check if the given permission is granted or not, in case granted it will return true
    public boolean isPermissionAllowed(String permission) {
        if (ContextCompat.checkSelfPermission(ActivityMaps.this, permission)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    //   Function to set a event handler for locateMe button, it contains implementation which will set user current
//    location on map
//    public void currentLocationUpdateListener() {
//        btnMyLocation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (ActivityCompat.checkSelfPermission(ActivityMaps.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ActivityMaps.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(ActivityMaps.this, "please allow permission first to continue", Toast.LENGTH_SHORT).show();
//                    requestPermissions(
//                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
//                            01);
//
//                    return;
//                }
////              Checking if GPS is enabled or not
//                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                    buildAlertMessageNoGps();
//                    return;
//                }
//                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        ((MapsFragment) fragmentManager.findFragmentByTag("mapFragment")).setLocation(location);
//                        senderLocation = location;
//                        btnShareMyLocation.setVisibility(View.VISIBLE);
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(ActivityMaps.this, "Please Try Again", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
//    }
//Function which will handle the result (permission granted or not) by user.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(ActivityMaps.this, "Fine Permission Granted", Toast.LENGTH_SHORT).show();
                isMyLocationClickable=true;
            } else {
                Toast.makeText(ActivityMaps.this, "Fine Permission Denied, Please Allow Permission to Continue", Toast.LENGTH_SHORT).show();
                isMyLocationClickable=true;
            }
        } else if (requestCode == COARSE_PERMISSION_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(ActivityMaps.this, "Coarse Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ActivityMaps.this, "Coarse Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Dialog which will be shown when GPS is disabled
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    //   Method which will share current location of user to receiver (his/her friend)
    public void shareLocation() {
        if (senderLocation != null) {
            DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse("https://www.example.com/?lat=" + senderLocation.getLatitude() + "&long=" + senderLocation.getLongitude()+ "&name=" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName() + "&email="+FirebaseAuth.getInstance().getCurrentUser().getEmail()))
//                    Sending static location for testing purpose
//                    .setLink(Uri.parse("https://www.example.com/?lat=" + 29.3830 + "&long=" + 71.7155 + "&name=Habibi"))
                    .setDomainUriPrefix("https://myfriendlocator.page.link")
                    // Open links with this app on Android
                    .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
//                            .setFallbackUrl(Uri.parse("https://www.google.com/"))
                            .build())
                    .buildDynamicLink();
            Uri dynamicLinkUri = dynamicLink.getUri();
            Log.e("Uri", dynamicLinkUri.toString() + "");
            Util.shareLink(this, dynamicLink.getUri().toString());
        } else {
            Toast.makeText(this, "Please Calculate Your Coordinates First", Toast.LENGTH_LONG).show();
        }
    }
    //    method which will handle all the dynamic link
    public void handleDynamicLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        if (pendingDynamicLinkData != null || deepLink!=null) {
                            if(deepLink!=null){
                                Log.e("deepLinkCheck", deepLink.toString());
                                getLastKnownLocation();
                                btnClearAll.setVisibility(View.GONE);
                            }else {
//                                Toast.makeText(ActivityMaps.this, "non null", Toast.LENGTH_SHORT).show();
                                deepLink = pendingDynamicLinkData.getLink();
                                Log.e("deepLinkCheck", deepLink.toString());
                                getLastKnownLocation();
                                btnClearAll.setVisibility(View.GONE);
                            }
                        }
                        else {
//                            Toast.makeText(ActivityMaps.this, " null", Toast.LENGTH_SHORT).show();
                            Log.e("deepLinkCheck", "null nothing found");
                            btnTrackLocation.setVisibility(View.GONE);
                            btnClearAll.setVisibility(View.GONE);
                            btnReCenter.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("handleDynamicLink", "getDynamicLink:onFailure", e);
                    }
                });
    }
    public void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(ActivityMaps.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ActivityMaps.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(ActivityMaps.this, "please allow permission first to continue", Toast.LENGTH_SHORT).show();
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    01);
            return;
        }
//                            check if gps is enabled or not
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return;
        }
        fusedLocationProviderClient.getCurrentLocation(new CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).build(), new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        }).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
               startTrackProcess(location);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ActivityMaps.this, "Location Fetch Failed!. Please Check Your GPS and Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });
//        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
//            @Override
//            public void onSuccess(Location location) {
//                startTrackProcess(location);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
////                Toast.makeText(ActivityMaps.this, "Please Try Again", Toast.LENGTH_SHORT).show();
//            }
//        });
    }
    @Override
    public void startTrackProcess(Location location) {
        double senderLatitude,  senderLongitude;
        String senderName,senderEmail;
        // lets store the data we received from link in data base along with receiver location data as well.
        try {
             senderLatitude = Double.parseDouble(deepLink.getQueryParameter("lat"));
             senderLongitude = Double.parseDouble(deepLink.getQueryParameter("long"));
             senderName = deepLink.getQueryParameter("name");
             senderEmail = deepLink.getQueryParameter("email");
            senderLatLng = new LatLng(senderLatitude, senderLongitude);
        }catch (Exception e){Log.e("Exception",""+e.getMessage());
            Toast.makeText(this, "Error Occurred, restart app and try again", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (location != null) {
            double receiverLatitude = location.getLatitude();
            double receiverLongitude = location.getLongitude();
            btnMyProfile.setVisibility(View.GONE);
            btnMyLocation.setVisibility(View.GONE);
            btnShareMyLocation.setVisibility(View.GONE);
            btnTrackLocation.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);
            tvDistance.setVisibility(View.VISIBLE);
            tvLabelDistance.setVisibility(View.VISIBLE);
            tvTimeLeft.setVisibility(View.VISIBLE);
            tvLabelTimeLeft.setVisibility(View.VISIBLE);
            tvSeperator.setVisibility(View.VISIBLE);
            TrackRecord trackRecord;
            if(FirebaseAuth.getInstance().getCurrentUser()!=null && FirebaseAuth.getInstance().getCurrentUser().getDisplayName()!=null) {
                 trackRecord = new TrackRecord(senderLatitude, senderLongitude, senderName, senderEmail, receiverLatitude, receiverLongitude, FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            }else{
                trackRecord = new TrackRecord(senderLatitude, senderLongitude, senderName, senderEmail, receiverLatitude, receiverLongitude, "Anonymous");
            }
                     keyText = senderEmail+FirebaseAuth.getInstance().getCurrentUser().getEmail();
                     key = keyText.hashCode();
                    database.child(String.valueOf(key)).setValue(trackRecord).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
//                            Toast.makeText(ActivityMaps.this, "running", Toast.LENGTH_SHORT).show();
                            gMap.clear();
                            addMarker(ActivityMaps.this,gMap, new LatLng(senderLatitude, senderLongitude), senderName, null);
                         travellerMarker=  addMarker(ActivityMaps.this,gMap, new LatLng(receiverLatitude, receiverLongitude), "",null);
                            Util.setMapViewZoomLevel(gMap, new LatLng(senderLatitude, senderLongitude), new LatLng(receiverLatitude, receiverLongitude));
                            double distanceBetweenUsers = Math.round(SphericalUtil.computeDistanceBetween(new LatLng(receiverLatitude, receiverLongitude), new LatLng(receiverLatitude, receiverLongitude)));
                            Log.e("distanceCheck", distanceBetweenUsers + "");
                            tvDistance.setVisibility(View.VISIBLE);
                            tvTimeLeft.setVisibility(View.VISIBLE);
                            tvLabelDistance.setVisibility(View.VISIBLE);
                            tvLabelTimeLeft.setVisibility(View.VISIBLE);
                            isTrackable = true;
                            isMarkSet = true;
                            checkDeepLink=false;
                            deepLink=null;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            isTrackable = false;
                            isMarkSet = false;
                            Toast.makeText(ActivityMaps.this, "Please make sure you have working internet connection", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        }
    //    method to add marker on map
    public void addMarker(GoogleMap gmap, Location location, String name) {
        Bitmap bitmap;
        IconGenerator iconGenerator = new IconGenerator(this);
//    iconGenerator.setColor(IconGenerator.STYLE_RED);
        iconGenerator.setStyle(IconGenerator.STYLE_BLUE);
        String userName = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
        if (userName != null) {
            bitmap = iconGenerator.makeIcon(userName);
        } else {
            bitmap = iconGenerator.makeIcon("User1");
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MapsFragment.markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(latLng).title(
                "User1"
        );
        gmap.addMarker(MapsFragment.markerOptions);
        CameraUpdate currentLoc = CameraUpdateFactory.newLatLngZoom(
                latLng, MapsFragment.ZOOM_LEVEL);
        gmap.animateCamera(currentLoc);
    }
    public static Marker addMarker(Context context,GoogleMap gmap, LatLng latLng, String name, String user) {
        Marker marker;
//        if (user != null) {
////            userMarkerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(Util.getBitmap(this, R.drawable.ic_outline_circle_24))).position(latLng);
////       circle= gmap.addCircle(new CircleOptions().center(latLng).clickable(false).fillColor(R.color.white).strokeColor(R.color.blue).radius(4 * Math.pow(2.0, gMap.getCameraPosition().zoom)));
////            receiverMarker = gmap.addMarker(userMarkerOption);
//            CameraUpdate currentLoc = CameraUpdateFactory.newLatLng(
//                    latLng);
//            gmap.animateCamera(currentLoc);
//        } else {
            Bitmap bitmap;
            IconGenerator iconGenerator = new IconGenerator(context);
            iconGenerator.setStyle(IconGenerator.STYLE_BLUE);
            bitmap = iconGenerator.makeIcon(name);
            MapsFragment.markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(latLng).title("");
        marker=    gmap.addMarker(MapsFragment.markerOptions);
            CameraUpdate currentLoc = CameraUpdateFactory.newLatLng(
                    latLng);
            gmap.animateCamera(currentLoc);
            return  marker;
    }
    public void setTrackMeListener() {
        btnTrackLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTrackable) {
                    trackIntent = new Intent(getApplicationContext(), LocationUpdateService.class);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//                    checkDeepLink=false;
//                    deepLink=null;
                    startService(trackIntent);
                    btnClearAll.setVisibility(View.VISIBLE);
                    btnTrackLocation.setVisibility(View.GONE);
                    isServiceRunning = true;
//                  as the track process will start by this method now we don't need any track-me button so hide it
                    btnTrackLocation.setVisibility(View.GONE);
                    btnReCenter.setVisibility(View.VISIBLE);
                    t.scheduleAtFixedRate(new TimerTask() {
                                              @Override
                                              public void run() {
                                                  if(shouldShowFinishDialog && LocationUpdateService.distanceRemaining<=5){
                                                      tvLabelDistance.post(new TimerTask() {
                                                          @Override
                                                          public void run() {
                                                             alertDialog.show();
                                                          }
                                                      });
                                                  }
                                                  tvDistance.post(new TimerTask() {
                                                      @SuppressLint("SetTextI18n")
                                                      @Override
                                                      public void run() {
                                                          if (LocationUpdateService.distanceRemaining > 1000) {
                                                           tvDistance.setText((((int)LocationUpdateService.distanceRemaining/1000))+" KM");
                                                          } else {
                                                              tvDistance.setText((((int)LocationUpdateService.distanceRemaining) + " Meters"));
                                                          }
                                                      }
                                                  });
                                                  tvTimeLeft.post(new TimerTask() {
                                                      @SuppressLint("DefaultLocale")
                                                      @Override
                                                      public void run() {
                                                        tvTimeLeft.setText(Util.generateTimeInHourMinutes(LocationUpdateService.timeRemaining));
                                                      }
                                                  });
                                              }

                                          },
//Set how long before to start calling the TimerTask (in milliseconds)
                            0,
//Set the amount of time between each execution (in milliseconds)
                            2000);
                } else {
                    Toast.makeText(ActivityMaps.this, "Please wait unless start and destination points are mapped on map, in progress", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    //Setting a event listener for button clearAll
    public void setClearAllListener() {
        btnClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isServiceRunning) {
                    showCancelTrackNotification().show();
                }else{
                    Toast.makeText(ActivityMaps.this, "Please start the tracking process first to stop", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //    change visibility mode of buttons when user is not in track mode
    public void buttonVisibilityNonTrackMode() {
        btnMyLocation.setVisibility(View.VISIBLE);
        btnShareMyLocation.setVisibility(View.VISIBLE);
        btnMyProfile.setVisibility(View.VISIBLE);
        btnTrackLocation.setVisibility(View.GONE);
        btnReCenter.setVisibility(View.GONE);
        btnClearAll.setVisibility(View.GONE);
        tvDistance.setVisibility(View.GONE);
        tvLabelDistance.setVisibility(View.GONE);
        tvTimeLeft.setVisibility(View.GONE);
        tvLabelTimeLeft.setVisibility(View.GONE);
        tvSeperator.setVisibility(View.GONE);
    }
    //Method which will warn and ask user if user really wants to stop tracking process
    public AlertDialog showCancelTrackNotification() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("Confirmation")
                .setMessage("Click on Reached button if you reached to your destination otherwise click on Keep Tracking to " +
                        "keep tracking process alive")
                .setPositiveButton("Reached", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(LocationUpdateService.polyline!=null) {
                            FirebaseDatabase.getInstance().getReference("TrackData").child(String.valueOf(key)).child("polyLineRecord").setValue(LocationUpdateService.polyline.getPoints());
                        }
                        try {
                            stopService(trackIntent);
                            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                            t.cancel();
                        }catch (Exception e){
                            Log.e("StopServiceException",""+e.getMessage());
                        }
                        isServiceRunning = false;
                        gMap.clear();
                        Toast.makeText(ActivityMaps.this, "Location Tracking Service Stopped", Toast.LENGTH_SHORT).show();
//                        circle.remove();
                        try {
                            gMap.animateCamera(CameraUpdateFactory.zoomTo(12));
//                            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LocationUpdateService.polyline.getPoints().get(LocationUpdateService.polyline.getPoints().size()-1), 7));
                        }catch (Exception e) {
                            Log.e("ex", "ex" + e.getMessage());
                        }
                        buttonVisibilityNonTrackMode();
                    }
                }).setNegativeButton("Keep Tracking", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ActivityMaps.this, "Tracking Service Continues", Toast.LENGTH_SHORT).show();
                    }
                }).setCancelable(false)
                .create();
        return alertDialog;
    }

    //    function to fetch current location of user
    public void currentLocationUpdateListener2() {
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMyLocationClickable){
                    isMyLocationClickable = false;
                if (ActivityCompat.checkSelfPermission(ActivityMaps.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ActivityMaps.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(ActivityMaps.this, "please allow permission first to continue", Toast.LENGTH_SHORT).show();
                    requestPermissions(
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                            01);
                    isMyLocationClickable=true;
                    return;
                }
//              Checking if GPS is enabled or not
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                    isMyLocationClickable=true;
                    return;
                }
                fusedLocationProviderClient.getCurrentLocation(new CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).build(), new CancellationToken() {
                    @NonNull
                    @Override
                    public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                        isMyLocationClickable=true;
                        return null;
                    }

                    @Override
                    public boolean isCancellationRequested() {
                        isMyLocationClickable=true;
                        return false;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        ((MapsFragment) fragmentManager.findFragmentByTag("mapFragment")).setLocation(location);
                        senderLocation = location;
                        btnShareMyLocation.setVisibility(View.VISIBLE);
                        isMyLocationClickable=true;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ActivityMaps.this, "Location Fetch Failed!. Please Check Your GPS and Internet Connection", Toast.LENGTH_SHORT).show();
                        isMyLocationClickable=true;
                    }
                });
            }else{
                    Toast.makeText(ActivityMaps.this, "Please Wait, Process is Already Running", Toast.LENGTH_SHORT).show();
                }
        }
        });
    }
    @SuppressLint("CutPasteId")
    public void buildEndTrackingDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.reached_successfully_dialog, null);
        dialogBuilder.setView(dialogView);
        Button btnHome = dialogView.findViewById(R.id.btnHomeActivity);
        Button btnCloseApp = dialogView.findViewById(R.id.btnCloseApp);
        ImageButton closeDialog = dialogView.findViewById(R.id.btnCloseDialog);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t.cancel();
                if (isServiceRunning) {
                    stopService(trackIntent);
                    Util.updateUI(ActivityMaps.this, ActivityMaps.class);
                    //perform any action related to database
                    finish();
                }
            }
        });
        btnCloseApp.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               t.cancel();
                                               if (isServiceRunning) {
                                                   if(LocationUpdateService.polyline!=null) {
                                                       FirebaseDatabase.getInstance().getReference("TrackData").child(String.valueOf(key)).child("polyLineRecord").setValue(LocationUpdateService.polyline.getPoints()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                           @Override
                                                           public void onSuccess(Void unused) {
                                                               Toast.makeText(ActivityMaps.this, "Data Fed Successfully In Database", Toast.LENGTH_SHORT).show();
                                                               stopService(trackIntent);
                                                               t.cancel();
                                                               finishAffinity();
                                                               finish();
                                                               System.exit(0);
                                                           }
                                                       }).addOnFailureListener(new OnFailureListener() {
                                                           @Override
                                                           public void onFailure(@NonNull Exception e) {
                                                               stopService(trackIntent);
                                                               t.cancel();
                                                               finishAffinity();
                                                               finish();
                                                               System.exit(0);
                                                           }
                                                       });
                                                   }

                                               }
                                           }
                                       });
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shouldShowFinishDialog=false;
                canSetShowDialog=false;
                alertDialog.cancel();
            }
        });
             alertDialog = dialogBuilder.create();
    }
    @Override
    protected void onResume() {
        if(checkDeepLink) {
            handleDynamicLink();
        }
        super.onResume();

    }

}