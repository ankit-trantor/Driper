package com.haui.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.haui.map.CustemMaps;
import com.haui.object.NguoiTimXe;
import com.haui.object.User;
import com.haui.object.XeTimNguoi;

/**
 * Created by Duong on 10/23/2016.
 */

public class MyService extends Service implements LocationListener{
    private DatabaseReference database;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private String maSV;
    private CustemMaps custemMaps;
    private LocationManager mLocationManager;
    private com.haui.log.Log log;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        database = FirebaseDatabase.getInstance().getReference();
        Toast.makeText(this, "Đang chạy service", Toast.LENGTH_SHORT).show();
        log=new com.haui.log.Log(this);
        custemMaps=new CustemMaps(this);
        mLocationManager= (LocationManager) getSystemService(LOCATION_SERVICE);
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
       return custemMaps.getNameByLocation(lat,lng);
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
    public void upDateDB(final String child,final String item, final String valuse) {
        database.child(child).child(maSV).child(item).setValue(valuse, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(MyService.this, "upDateALL", Toast.LENGTH_SHORT).show();
                    Log.e("faker update","upDateALL");
                }
            }
        });
    }

    private Location locationOld;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==111) {
                Location location;
                 location= (Location) msg.obj;
                if (location.distanceTo(locationOld)>100){
                    locationOld= location;
                    upDateALL();
                }
            }else  if(msg.what==1010){
                maSV= (String) msg.obj;
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400,1000, MyService.this);

            }
        }
    };

    private void upDateALL() {
        database.child("XeTimNguoi").child(maSV).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                XeTimNguoi xeTimNguoi =dataSnapshot.getValue(XeTimNguoi.class);
                if (xeTimNguoi !=null){
                    upDateDB("XeTimNguoi","location/lat",""+locationOld.getLatitude());
                    upDateDB("XeTimNguoi","location/lng",""+locationOld.getLongitude());
                    upDateDB("XeTimNguoi","viTri",getNameByLocation(locationOld.getLatitude(),locationOld.getLongitude()));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        database.child("NguoiTimXe").child(maSV).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                NguoiTimXe nguoiTimXe =dataSnapshot.getValue(NguoiTimXe.class);
                if (nguoiTimXe !=null){
                    upDateDB("NguoiTimXe","location/lat",""+locationOld.getLatitude());
                    upDateDB("NguoiTimXe","location/lng",""+locationOld.getLongitude());
                    upDateDB("NguoiTimXe","viTri",getNameByLocation(locationOld.getLatitude(),locationOld.getLongitude()));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if (locationOld==null){
            locationOld=location;
            upDateALL();
        }else {
            Message message=new Message();
            message.what=111;
            message.obj=location;
            handler.sendMessage(message);
        }
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

