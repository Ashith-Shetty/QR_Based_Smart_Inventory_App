package com.yourname.qrinventory;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import androidx.core.content.ContextCompat;
import com.yourname.qrinventory.service.NotificationHelper;
import com.yourname.qrinventory.service.SystemEventReceiver;
import com.yourname.qrinventory.service.WorkScheduler;

public class InventoryApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createChannel(this);
        WorkScheduler.schedule(this);

        SystemEventReceiver.online.setValue(SystemEventReceiver.isOnline(this));
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        ContextCompat.registerReceiver(this, new SystemEventReceiver(), filter,
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }
}
