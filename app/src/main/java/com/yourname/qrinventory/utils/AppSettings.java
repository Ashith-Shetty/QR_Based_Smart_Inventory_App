package com.yourname.qrinventory.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {
    private static final String PREFS = "app_settings";
    private static final String KEY_ALERTS = "alerts_enabled";
    private static final String KEY_DAYS = "expiry_days";

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean alertsEnabled(Context c) { return prefs(c).getBoolean(KEY_ALERTS, true); }
    public static void setAlertsEnabled(Context c, boolean on) { prefs(c).edit().putBoolean(KEY_ALERTS, on).apply(); }

    public static int expiryDays(Context c) { return prefs(c).getInt(KEY_DAYS, 3); }
    public static void setExpiryDays(Context c, int days) { prefs(c).edit().putInt(KEY_DAYS, days).apply(); }
}