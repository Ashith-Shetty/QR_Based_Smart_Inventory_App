package com.yourname.qrinventory.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.widget.Toast;
import androidx.lifecycle.MutableLiveData;

public class SystemEventReceiver extends BroadcastReceiver {
    // Screens can observe these, e.g. to show an "Offline" banner or a low-battery warning
    public static final MutableLiveData<Boolean> online = new MutableLiveData<>();
    public static final MutableLiveData<Boolean> lowBattery = new MutableLiveData<>(false);

    // Handles the battery broadcasts; connectivity is handled by NetworkMonitor
    @Override
    public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        if (Intent.ACTION_BATTERY_LOW.equals(action)) {
            lowBattery.setValue(true);
            Toast.makeText(ctx, "Battery low: the camera scanner drains power", Toast.LENGTH_LONG).show();
        } else if (Intent.ACTION_BATTERY_OKAY.equals(action)) {
            lowBattery.setValue(false);
        }
    }

    public static boolean isOnline(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network n = cm.getActiveNetwork();
        if (n == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(n);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}