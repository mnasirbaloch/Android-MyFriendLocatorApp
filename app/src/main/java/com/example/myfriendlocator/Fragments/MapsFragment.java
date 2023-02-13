package com.example.myfriendlocator.Fragments;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myfriendlocator.Activity.ActivityMaps;
import com.example.myfriendlocator.Interface.LocationHandler;
import com.example.myfriendlocator.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
public class MapsFragment extends Fragment implements LocationHandler {
    public GoogleMap gMap;
    public static MarkerOptions markerOptions;
    public final static float ZOOM_LEVEL = 9.5f;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            gMap = googleMap;
            gMap.clear();
            LatLng VUBwp = new LatLng(30.3753,69.3451);
//            ((  ActivityMaps)getActivity()).addMarker(gMap,VUBwp,"Virtual University Bahawalpur",null);
            gMap.moveCamera(CameraUpdateFactory.newLatLng(VUBwp));
//           gMap.addMarker(markerOptions);
            gMap.animateCamera(CameraUpdateFactory.zoomTo(4));
            ActivityMaps.gMap=googleMap;
        }
    };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
//    method which will set current location of user when-ever user clicks on my-location button
    @Override
    public void setLocation(Location location) {
        if (location == null) {
            Toast.makeText(getActivity(), "Location Fetch Failed. Make sure you have GPS and Internet Service Enabled", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            gMap.clear();
            ((ActivityMaps) getActivity()).addMarker(gMap, location, FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            Objects.requireNonNull(gMap.addMarker(markerOptions)).setDraggable(true);
        } catch (Exception e) {
            Log.e("MapsFragment Exception",""+e.getMessage());
        }
    }
}