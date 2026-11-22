package com.yourname.qrinventory.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import com.yourname.qrinventory.data.AppDatabase;
import com.yourname.qrinventory.model.Product;
import com.yourname.qrinventory.utils.CsvUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

// Foreground service: backs the inventory up to a CSV file in Downloads, with a progress notification
public class BackupService extends Service {

    // Screens observe these (Settings shows the result and disables the button while running)
    public static final MutableLiveData<String> status = new MutableLiveData<>();
    public static final MutableLiveData<Boolean> running = new MutableLiveData<>(false);

    private static final String CHANNEL_ID = "backup";
    private static final int NOTIF_ID = 2001;
    private static final int STAGES = 3;
    private static final long PACE_MS = 1200;   // short pause per stage so the progress is visible; set to 0 to remove

    private final AtomicBoolean busy = new AtomicBoolean(false);
    private final Handler main = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Backup", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Inventory backup progress");
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Must be called straight away after startForegroundService()
        Notification n = buildNotification("Starting backup...", 0);
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIF_ID, n);
        }
        if (!busy.compareAndSet(false, true)) return START_NOT_STICKY;   // a backup is already running
        running.postValue(true);
        AppDatabase.IO.execute(this::runBackup);
        return START_NOT_STICKY;
    }

    private void runBackup() {
        try {
            update("Reading inventory...", 0);
            pause();
            List<Product> all = AppDatabase.get(this).productDao().getAllNow();

            update("Writing " + all.size() + " products...", 1);
            File csv = CsvUtils.exportProducts(this, all, "inventory_backup");
            pause();

            update("Saving to Downloads...", 2);
            String where = saveToDownloads(csv);
            pause();
            update("Done", STAGES);

            String msg = all.size() + " products saved to " + where;
            status.postValue(msg);
            NotificationHelper.show(this, 1003, "Backup complete", msg);
        } catch (Exception e) {
            String msg = "Backup failed: " + e.getMessage();
            status.postValue(msg);
            NotificationHelper.show(this, 1003, "Backup failed", String.valueOf(e.getMessage()));
        } finally {
            busy.set(false);
            running.postValue(false);
            main.post(() -> {
                stopForeground(STOP_FOREGROUND_REMOVE);
                stopSelf();
            });
        }
    }

    private void update(String text, int stage) {
        getSystemService(NotificationManager.class).notify(NOTIF_ID, buildNotification(text, stage));
    }

    private Notification buildNotification(String text, int stage) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Backing up inventory")
                .setContentText(text)
                .setProgress(STAGES, stage, false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();
    }

    private void pause() {
        if (PACE_MS <= 0) return;
        try { Thread.sleep(PACE_MS); } catch (InterruptedException ignored) { }
    }

    // Copies the CSV into the public Downloads folder (MediaStore, no storage permission needed)
    private String saveToDownloads(File src) throws IOException {
        if (Build.VERSION.SDK_INT >= 29) {
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.Downloads.DISPLAY_NAME, src.getName());
            cv.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            cv.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
            if (uri == null) throw new IOException("Could not create a file in Downloads");
            try (InputStream in = new FileInputStream(src);
                 OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (out == null) throw new IOException("Could not open the file in Downloads");
                copy(in, out);
            }
            return "Downloads/" + src.getName();
        } else {
            File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (dir == null) throw new IOException("Storage is not available");
            File dest = new File(dir, src.getName());
            try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
                copy(in, out);
            }
            return dest.getAbsolutePath();
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
    }
}