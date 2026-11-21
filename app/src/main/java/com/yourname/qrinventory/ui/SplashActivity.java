package com.yourname.qrinventory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.yourname.qrinventory.MainActivity;
import com.yourname.qrinventory.R;
import com.yourname.qrinventory.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable go = () -> {
        Class<?> next = SessionManager.isLoggedIn(this) ? MainActivity.class : LoginActivity.class;
        startActivity(new Intent(this, next));   // explicit intent
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager.applySavedNightMode(this);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(go, 1500);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(go);
        super.onDestroy();
    }
}