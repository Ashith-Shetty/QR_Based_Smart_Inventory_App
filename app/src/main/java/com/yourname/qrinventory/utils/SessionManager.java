package com.yourname.qrinventory.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;

public final class SessionManager {
    private static final String PREFS = "qrmart_auth";
    private static final String USER_PREFIX = "user_";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ADMIN_CODE = "QRMART-ADMIN";

    private SessionManager() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ---------- accounts ----------
    public static void seedDemoUsers(Context c) {
        SharedPreferences p = prefs(c);
        if (p.getBoolean("seeded", false)) return;
        saveUser(c, "Store Admin", "admin@qrmart.com", "admin123", ROLE_ADMIN);
        saveUser(c, "Demo Shopper", "shopper@qrmart.com", "shop123", ROLE_CUSTOMER);
        p.edit().putBoolean("seeded", true).apply();
    }

    /** @return an error message, or null on success (the user is then logged in). */
    public static String register(Context c, String name, String email, String password,
                                  String role, String adminCode) {
        email = email.trim().toLowerCase(Locale.ROOT);
        if (name.trim().isEmpty()) return "Enter your name";
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Enter a valid email";
        if (password.length() < 6) return "Password must be at least 6 characters";
        if (ROLE_ADMIN.equals(role) && !ADMIN_CODE.equals(adminCode.trim())) return "Wrong admin code";
        if (prefs(c).contains(USER_PREFIX + email)) return "An account with this email already exists";
        saveUser(c, name.trim(), email, password, role);
        startSession(c, name.trim(), email, role);
        return null;
    }

    /** @return an error message, or null on success. */
    public static String login(Context c, String email, String password) {
        email = email.trim().toLowerCase(Locale.ROOT);
        String json = prefs(c).getString(USER_PREFIX + email, null);
        if (json == null) return "No account found for this email";
        try {
            JSONObject o = new JSONObject(json);
            if (!hash(o.getString("salt"), password).equals(o.getString("hash"))) return "Incorrect password";
            startSession(c, o.getString("name"), email, o.getString("role"));
            return null;
        } catch (JSONException e) {
            return "Account data is corrupted";
        }
    }

    private static void saveUser(Context c, String name, String email, String password, String role) {
        try {
            byte[] saltBytes = new byte[16];
            new SecureRandom().nextBytes(saltBytes);
            StringBuilder salt = new StringBuilder();
            for (byte b : saltBytes) salt.append(String.format("%02x", b));
            JSONObject o = new JSONObject()
                    .put("name", name).put("role", role)
                    .put("salt", salt.toString()).put("hash", hash(salt.toString(), password));
            prefs(c).edit().putString(USER_PREFIX + email.toLowerCase(Locale.ROOT), o.toString()).apply();
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String hash(String salt, String password) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256")
                    .digest((salt + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // ---------- session ----------
    private static void startSession(Context c, String name, String email, String role) {
        prefs(c).edit().putString("s_name", name).putString("s_email", email).putString("s_role", role).apply();
    }
    public static boolean isLoggedIn(Context c) { return prefs(c).contains("s_email"); }
    public static String getName(Context c) { return prefs(c).getString("s_name", ""); }
    public static String getEmail(Context c) { return prefs(c).getString("s_email", ""); }
    public static boolean isAdmin(Context c) { return ROLE_ADMIN.equals(prefs(c).getString("s_role", ROLE_CUSTOMER)); }
    public static void logout(Context c) {
        prefs(c).edit().remove("s_name").remove("s_email").remove("s_role").apply();
    }

    // ---------- dark mode preference ----------
    public static void saveNightMode(Context c, int mode) { prefs(c).edit().putInt("night_mode", mode).apply(); }
    public static void applySavedNightMode(Context c) {
        AppCompatDelegate.setDefaultNightMode(
                prefs(c).getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
    }
}