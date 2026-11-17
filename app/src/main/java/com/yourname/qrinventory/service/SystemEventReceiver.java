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

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        switch (action) {
            case ConnectivityManager.CONNECTIVITY_ACTION: {
                boolean now = isOnline(ctx);
                Boolean before = online.getValue();
                online.setValue(now);
                if (now && Boolean.FALSE.equals(before)) {
                    Toast.makeText(ctx, "Back online", Toast.LENGTH_SHORT).show();
                    WorkScheduler.runNow(ctx);   // re-run the stock/expiry check
                } else if (!now) {
                    Toast.makeText(ctx, "You are offline", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case Intent.ACTION_BATTERY_LOW:
                lowBattery.setValue(true);
                Toast.makeText(ctx, "Battery low: the camera scanner drains power", Toast.LENGTH_LONG).show();
                break;
            case Intent.ACTION_BATTERY_OKAY:
                lowBattery.setValue(false);
                break;
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