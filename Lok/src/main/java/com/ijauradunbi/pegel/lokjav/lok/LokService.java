package com.ijauradunbi.pegel.lokjav.lok;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LokService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "LokService";
    private static final String defaultUploadSitePancanaka = "http://pancanaka.net/gpstracker/updatelocation.php";
    private static final String defaultUploadSite = "https://www.websmithing.com/gpstracker/updatelocation.php";
    private boolean processNow = false;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    public LokService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if (!processNow) {
            processNow = true;
            startTracking();
        }
        return START_NOT_STICKY;
    }

    private void startTracking() {
        Log.d(TAG, "startTracking");

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS)
            Log.d(TAG, "unable to connect.");
        else {
            Log.d(TAG, "startTracking, creating googleApiClient.");
            googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            Log.d(TAG, "startTracking, connecting googleApiClient to the server.");
            if (!googleApiClient.isConnecting() || !googleApiClient.isConnected()) googleApiClient.connect();
            Log.d(TAG, "startTracking, googleApiClient connected.");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Klien di suspend");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Checking location.");
        if (location == null) {
            Log.e(TAG, "Ndak dapat cari lokasi.");
            return;
        }
        Log.e(TAG, "Posisi : " + location.getLatitude() + ", " + location.getLongitude() + ". Akurasi : " + location.getAccuracy());
        if (location.getAccuracy() > 500.0f) return;
        stopLocationUpdate();
        sendData(location);
    }

    // FIXME: 2016-01-29 Refaktor method sendData
    private void sendData(Location location) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date(location.getTime());

        SharedPreferences prefs = this.getSharedPreferences("lokjav", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        float totalDistance = prefs.getFloat("totalDistance", 0f);
        boolean firstTimePosition = prefs.getBoolean("firstTimePosition", true);

        if (firstTimePosition) editor.putBoolean("firstTimePosition", false);
        else {
            Location prevLocation = new Location("");
            prevLocation.setLatitude(prefs.getFloat("prefLat", 0f));
            prevLocation.setLongitude(prefs.getFloat("prefLong", 0f));

            float distance = location.distanceTo(prevLocation);
            totalDistance += distance;
            editor.putFloat("totalDistance", totalDistance);
        }
        editor.putFloat("prevLat", (float) location.getLatitude());
        editor.putFloat("prevLong", (float) location.getLongitude());
        editor.apply();

        final RequestParams requestParams = new RequestParams();
        requestParams.put("latitude", Double.toString(location.getLatitude()));
        requestParams.put("longitude", Double.toString(location.getLongitude()));
        requestParams.put("speed", Float.toString(location.getSpeed()));
        requestParams.put("date", URLEncoder.encode(dateFormat.format(date)));

        if (totalDistance > 0) requestParams.put("distance", totalDistance);
        else requestParams.put("distance", 0.0);

        requestParams.put("username", prefs.getString("username", ""));
        requestParams.put("phonenumber", prefs.getString("appId", ""));
        requestParams.put("sessionid", prefs.getString("sessionId", ""));
        requestParams.put("accuracy", Float.toString(location.getAccuracy()));
        requestParams.put("altitude", Double.toString(location.getAltitude()));
        requestParams.put("password", "parametrik2016");

        final AsyncHttpClient client = new AsyncHttpClient();

        client.get(defaultUploadSitePancanaka, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(TAG, "sukses.");
                LoopjHttpClient.debugLoop(TAG, "sendData sukses", defaultUploadSite, requestParams, responseBody, headers, statusCode, null);
                stopSelf();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, "gagal.");
                LoopjHttpClient.debugLoop(TAG, "sendData gagal", defaultUploadSite, requestParams, responseBody, headers, statusCode, error);
                client.get(defaultUploadSite, requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d(TAG, "sent to default web.");
                        LoopjHttpClient.debugLoop(TAG, "sendData sukses ke websmithing.", defaultUploadSite, requestParams, responseBody, headers, statusCode, null);
                        stopSelf();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.d(TAG, "you dun goofed, m8.");
                        LoopjHttpClient.debugLoop(TAG, "sendData gagal ke websmithing.", defaultUploadSite, requestParams, responseBody, headers, statusCode, null);
                        stopSelf();
                    }
                });
                stopSelf();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");

        stopLocationUpdate();
        stopSelf();
    }

    private void stopLocationUpdate() {
        if (googleApiClient != null && googleApiClient.isConnected())
            googleApiClient.disconnect();
    }
}
