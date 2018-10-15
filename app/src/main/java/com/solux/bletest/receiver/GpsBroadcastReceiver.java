package com.solux.bletest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import com.solux.bletest.listener.GpsStateListener;

/**
 * Create by qindl
 * on 2018/9/15
 */
public class GpsBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (enabled){
            mGpsStateListener.on();
        } else {
            mGpsStateListener.off();
        }
    }

    public GpsStateListener mGpsStateListener;

    public void setGpsStateListener(GpsStateListener gpsStateListener){
        this.mGpsStateListener = gpsStateListener;
    }

}
