package com.yourname.qrinventory.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {

    public interface OnShake { void onShake(); }

    private static final float THRESHOLD_G = 2.7f;     // how hard the shake must be
    private static final long MIN_GAP_MS = 1000;       // ignore repeats within 1 second

    private final OnShake listener;
    private long lastShake = 0;

    public ShakeDetector(OnShake listener) { this.listener = listener; }

    @Override
    public void onSensorChanged(SensorEvent e) {
        float gx = e.values[0] / SensorManager.GRAVITY_EARTH;
        float gy = e.values[1] / SensorManager.GRAVITY_EARTH;
        float gz = e.values[2] / SensorManager.GRAVITY_EARTH;
        double gForce = Math.sqrt(gx * gx + gy * gy + gz * gz);   // about 1.0 when the phone is still
        if (gForce > THRESHOLD_G) {
            long now = System.currentTimeMillis();
            if (now - lastShake > MIN_GAP_MS) {
                lastShake = now;
                listener.onShake();
            }
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}