package com.yourname.qrinventory.service;

import android.content.Context;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class WorkScheduler {
    // Runs the stock/expiry check every 12 hours, even when the app is closed
    public static void schedule(Context ctx) {
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(StockCheckWorker.class, 12, TimeUnit.HOURS).build();
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                "stock_check", ExistingPeriodicWorkPolicy.KEEP, req);
    }

    // Runs the check once right now (for the demo and the "refresh" button)
    public static void runNow(Context ctx) {
        WorkManager.getInstance(ctx).enqueue(new OneTimeWorkRequest.Builder(StockCheckWorker.class).build());
    }
}
