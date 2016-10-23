package com.haui.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.haui.object.User;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Duong on 10/23/2016.
 */

public class MyService extends Service implements LocationListener{
    private DatabaseReference database;
    private Geocoder geocoder;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String maSV;
    private LocationManager mLocationManager;
    private com.haui.log.Log log;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        database = FirebaseDatabase.getInstance().getReference();
        log=new com.haui.log.Log(this);
        mLocationManager= (LocationManager) getSystemService(LOCATION_SERVICE);
        Log.e("faker","chay");
        geocoder = new Geocoder(this, Locale.getDefault());
        checkLogin(log.getID(),log.getPass());
        return START_STICKY;
    }
    public boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }
    private String getNameByLocation(double lat,double lng){
        //tìm kiếm vị trí
        try {
            List<Address> addresses = geocoder.getFromLocation(lat,lng,1);// getfromlocation trả vể list nên cần tạo 1 list
            if (addresses.size()==0){
                return "";
            }
            String name = addresses.get(0).getAddressLine(0);
            name +=" - " +addresses.get(0).getAddressLine(1);
            name +=" - " +addresses.get(0).getAddressLine(2);
            return name;
        } catch (IOException e) {

            return "";
        }

    }
    private void checkLogin(final String extra, final String stringExtra) {
        if (isOnline()) {
            try {
                database.child("users").child(extra).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                try {
                                    if (user.getPassWord().equals(stringExtra)&&user.getMaSV().equals(extra)) {
                                        maSV=user.getMaSV();
                                        Message message=new Message();
                                        message.obj=maSV;
                                        message.what=1010;
                                        handler.sendMessage(message);
                                        database.child("users").child(extra).removeEventListener(this);
                                        return;
                                    }
                                } catch (NullPointerException e) {
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                android.util.Log.e("faker", "onCancelled");
                            }
                        });
            } catch (NullPointerException e) {
            }
        }
    }
    public void upDateUser(final String item, final String valuse) {
        database.child("users").child(maSV).child(item).setValue(valuse, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400,1, MyService.this);
                    }else {
                }
            }
        });
    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==111) {
                Location location= (Location) msg.obj;
                Log.e("faker","chay"+location.getLatitude());
//                Intent intent = new Intent("my.location");
                // add data
//                intent.putExtra("lat",""+location.getLatitude());
//                intent.putExtra("lng",""+location.getLongitude());
                upDateUser("location/lat",""+location.getLatitude());
                upDateUser("location/lng",""+location.getLongitude());
                upDateUser("viTri",getNameByLocation(location.getLatitude(),location.getLongitude()));

//                LocalBroadcastManager.getInstance(MyService.this).sendBroadcast(intent);
//                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400,10000, MyService.this);
            }else if(msg.what==1010){
                maSV= (String) msg.obj;
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400,1, MyService.this);
            }


        }
    };
    @Override
    public void onLocationChanged(Location location) {
        Message message=new Message();
        message.what=111;
        message.obj=location;
        handler.sendMessage(message);
        mLocationManager.removeUpdates(this);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    }
