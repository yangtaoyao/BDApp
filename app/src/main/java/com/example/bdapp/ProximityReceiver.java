package com.example.bdapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

public class ProximityReceiver extends BroadcastReceiver {
    boolean isEnter;

    @Override
    public void onReceive(Context context, Intent intent) {
        isEnter = intent.getBooleanExtra( LocationManager.KEY_PROXIMITY_ENTERING, false);
        if(isEnter)
            Toast.makeText(context, "你已到达目的地附近", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, "你已离开目的地附近", Toast.LENGTH_LONG).show();

        SharedPreferences sp= context.getSharedPreferences("User", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isEnter", isEnter);
        editor.commit();
        Boolean content = sp.getBoolean("isEnter", false);
//        Toast.makeText(context, "isEnter:"+content, Toast.LENGTH_LONG).show();
    }
}
