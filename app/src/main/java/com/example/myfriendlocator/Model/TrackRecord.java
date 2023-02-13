package com.example.myfriendlocator.Model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class TrackRecord {
    double receiverLatitude;
    double receiverLongitude;
    String receiverName;
    double senderLatitude;
    double senderLongitude;
    String senderName;
    String senderGmail;

    public static List<LatLng> getPolyLineRecord() {
        return polyLineRecord;
    }

    public static void setPolyLineRecord(List<LatLng> polyLineRecord) {
        TrackRecord.polyLineRecord = polyLineRecord;
    }

    public static List<LatLng> polyLineRecord = new ArrayList();
    public TrackRecord(double senderLatitude, double senderLongitude, String senderName, String senderGmail,double receiverLatitude, double receiverLongitude, String receiverName) {
        this.receiverLatitude = receiverLatitude;
        this.receiverLongitude = receiverLongitude;
        this.receiverName = receiverName;
        this.senderLatitude = senderLatitude;
        this.senderLongitude = senderLongitude;
        this.senderName = senderName;
        this.senderGmail = senderGmail;
    }

    public double getReceiverLatitude() {
        return receiverLatitude;
    }

    public void setReceiverLatitude(double receiverLatitude) {
        this.receiverLatitude = receiverLatitude;
    }

    public double getReceiverLongitude() {
        return receiverLongitude;
    }

    public void setReceiverLongitude(double receiverLongitude) {
        this.receiverLongitude = receiverLongitude;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public double getSenderLatitude() {
        return senderLatitude;
    }

    public void setSenderLatitude(double senderLatitude) {
        this.senderLatitude = senderLatitude;
    }

    public double getSenderLongitude() {
        return senderLongitude;
    }

    public void setSenderLongitude(double senderLongitude) {
        this.senderLongitude = senderLongitude;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderGmail() {
        return senderGmail;
    }

    public void setSenderGmail(String senderGmail) {
        this.senderGmail = senderGmail;
    }

}
