package com.yourname.qrinventory.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.yourname.qrinventory.utils.AppSettings;

public class NetworkMonitor {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static boolean started = false;

    public static void start(Context ctx) {
        if (started) return;
        started = true;
        Context app = ctx.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest req = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        cm.registerNetworkCallback(req, new ConnectivityManager.NetworkCallback() {
            @Override public void onAvailable(Network network) { update(app, true); }
            @Override public void onLost(Network network) { update(app, SystemEventReceiver.isOnline(app)); }
        });
    }

    // Only reacts when the state actually changes, so you never get duplicate toasts
    private static void update(Context app, boolean now) {
        MAIN.post(() -> {
            Boolean before = SystemEventReceiver.online.getValue();
            if (before != null && before == now) return;
            SystemEventReceiver.online.setValue(now);
            Log.d("NetworkMonitor", "online = " + now);
            if (now) {
                Toast.makeText(app, "Back online", Toast.LENGTH_LONG).show();
                if (AppSettings.alertsEnabled(app)) WorkScheduler.runNow(app);
            } else {
                Toast.makeText(app, "You are offline", Toast.LENGTH_LONG).show();
            }
        });
    }
}
