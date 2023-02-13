package com.example.myfriendlocator.Service;
import static com.example.myfriendlocator.Activity.ActivityMaps.canSetShowDialog;
import static com.example.myfriendlocator.Activity.ActivityMaps.circle;
import static com.example.myfriendlocator.Activity.ActivityMaps.gMap;
import static com.example.myfriendlocator.Activity.ActivityMaps.isServiceRunning;
import static com.example.myfriendlocator.Activity.ActivityMaps.shouldShowFinishDialog;
import static com.example.myfriendlocator.Activity.ActivityMaps.travellerMarker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.myfriendlocator.Activity.ActivityLogin;
import com.example.myfriendlocator.Activity.ActivityMaps;
import com.example.myfriendlocator.Fragments.MapsFragment;
import com.example.myfriendlocator.Model.TrackRecord;
import com.example.myfriendlocator.R;
import com.example.myfriendlocator.Util.AppConstants;
import com.example.myfriendlocator.Util.Util;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
public class LocationUpdateService extends Service {
    Marker currentLocationMarker;
    private PolylineOptions polylineOptions = new PolylineOptions();
   public static Polyline polyline;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 3000;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    public static double distanceRemaining, timeRemaining, currentSpeed;
    double updateDistance=0;
    Location currentLocation;
    double cameraZoomLevel;
    private int myCounter;
    @Override
    public void onCreate() {
        super.onCreate();
        initData();
    }
    //Location Callback
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            currentLocation = locationResult.getLastLocation();
            distanceRemaining = Math.round(SphericalUtil.computeDistanceBetween(ActivityMaps.senderLatLng, new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
            Log.e("beforeDistance", "distance: " + distanceRemaining);
            currentSpeed = locationResult.getLastLocation().getSpeed();
            timeRemaining = Double.parseDouble(String.valueOf((distanceRemaining / 1000) / (currentSpeed / 3.6)));
          timeRemaining =   Double.parseDouble(String.format("%.2f", timeRemaining));
            cameraZoomLevel =gMap.getCameraPosition().zoom;
            Log.e("ZoomLevel",gMap.getCameraPosition().zoom+"");
            try {
                if (!isServiceRunning) {
                    mFusedLocationClient.removeLocationUpdates(locationCallback);

                } else {
                    if(TrackRecord.polyLineRecord.size()!=0) {
                        LatLng previous = TrackRecord.polyLineRecord.get(TrackRecord.polyLineRecord.size() - 1);
                         updateDistance = Math.round(SphericalUtil.computeDistanceBetween(previous, new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
                    }
                    if (TrackRecord.polyLineRecord.size()==0 || updateDistance > 5 ) {
                        TrackRecord.polyLineRecord.add(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()));
                        if(myCounter==1){
                            ActivityMaps.travellerMarker.remove();
                            myCounter++;
                            travellerMarker.setPosition(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()));
                           ActivityMaps.addMarker(LocationUpdateService.this,gMap, new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()), "",null);
                        }
                        polyline = gMap.addPolyline(polylineOptions.add(TrackRecord.polyLineRecord.get(TrackRecord.polyLineRecord.size() - 1)).color(getResources().getColor(R.color.blue)));
                        if(currentLocationMarker==null) {
                            currentLocationMarker = gMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
                        }else{
                            currentLocationMarker.remove();
                            currentLocationMarker = gMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
                        }
                    }
                }
            } catch (Exception e) {
                    Log.e("AppException","LocationUpdateService: "+e.getMessage());
            }
            if(canSetShowDialog){shouldShowFinishDialog=true;}
        }
    };
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        prepareForegroundNotification();
        startLocationUpdates();
        myCounter=0;
        myCounter++;
        return START_NOT_STICKY;
    }
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }
        mFusedLocationClient.requestLocationUpdates(this.locationRequest,
                this.locationCallback, Looper.myLooper());
    }
    private void prepareForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    AppConstants.CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
        Intent notificationIntent = new Intent(this, ActivityMaps.class);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this,
                    AppConstants.SERVICE_LOCATION_REQUEST_CODE,
                    notificationIntent, PendingIntent.FLAG_MUTABLE);
        }else{
            pendingIntent = PendingIntent.getActivity(this,
                    AppConstants.SERVICE_LOCATION_REQUEST_CODE,
                    notificationIntent,0);
        }
        Notification notification = new NotificationCompat.Builder(this, AppConstants.CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentTitle(getString(R.string.app_notification_description))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(AppConstants.LOCATION_SERVICE_NOTIF_ID, notification);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void initData() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(getApplicationContext());
    }

}