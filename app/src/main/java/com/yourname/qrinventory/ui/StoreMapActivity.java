package com.yourname.qrinventory.ui;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.location.LocationListenerCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yourname.qrinventory.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.Locale;

public class StoreMapActivity extends AppCompatActivity {

    private static class Store {
        final String name, address, phone;
        final double lat, lon;
        Store(String name, String address, String phone, double lat, double lon) {
            this.name = name; this.address = address; this.phone = phone;
            this.lat = lat; this.lon = lon;
        }
    }

    // Demo branch locations (fixed data)
    private static final Store[] STORES = {
            new Store("QR Mart MG Road", "MG Road, Bengaluru", "+91 80 1234 5678", 12.9716, 77.5946),
            new Store("QR Mart Koramangala", "80 Feet Road, Koramangala, Bengaluru", "+91 80 1234 5679", 12.9352, 77.6245),
            new Store("QR Mart Indiranagar", "100 Feet Road, Indiranagar, Bengaluru", "+91 80 1234 5680", 12.9784, 77.6408),
            new Store("QR Mart Jayanagar", "4th Block, Jayanagar, Bengaluru", "+91 80 1234 5681", 12.9299, 77.5826)
    };

    private MapView map;
    private TextView tvNearest;
    private LocationManager locationManager;
    private Marker meMarker;
    private Store nearest = STORES[0];
    private boolean permissionAsked = false;

    private final LocationListenerCompat listener = this::onLocation;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (hasLocationPermission()) startLocation();
                else tvNearest.setText("Location permission denied, so only the store locations are shown.");
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // osmdroid setup: user agent, and a tile cache inside the app's own storage
        IConfigurationProvider cfg = Configuration.getInstance();
        cfg.load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        cfg.setUserAgentValue(getPackageName());
        File base = new File(getCacheDir(), "osmdroid");
        cfg.setOsmdroidBasePath(base);
        cfg.setOsmdroidTileCache(new File(base, "tiles"));

        setContentView(R.layout.activity_store_map);
        View root = findViewById(R.id.map_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvNearest = findViewById(R.id.tv_nearest);
        findViewById(R.id.btn_directions).setOnClickListener(v -> openDirections(nearest));

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(12.0);
        map.getController().setCenter(new GeoPoint(12.9540, 77.6000));

        for (Store s : STORES) {
            Marker m = new Marker(map);
            m.setPosition(new GeoPoint(s.lat, s.lon));
            m.setTitle(s.name);
            m.setSnippet(s.address + "\n" + s.phone);
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(m);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        if (hasLocationPermission()) {
            startLocation();
        } else if (!permissionAsked) {
            permissionAsked = true;
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
        if (locationManager != null) locationManager.removeUpdates(listener);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void startLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean any = false;
        for (String provider : new String[]{LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER}) {
            try {
                if (!locationManager.isProviderEnabled(provider)) continue;
                locationManager.requestLocationUpdates(provider, 3000L, 5f, listener, Looper.getMainLooper());
                any = true;
                Location last = locationManager.getLastKnownLocation(provider);
                if (last != null) onLocation(last);
            } catch (SecurityException ignored) {
                // only approximate permission granted: the GPS provider is not allowed, network is
            }
        }
        if (!any) tvNearest.setText("Location is turned off. Turn it on to find your nearest store.");
    }

    private void onLocation(Location loc) {
        GeoPoint me = new GeoPoint(loc.getLatitude(), loc.getLongitude());
        boolean first = (meMarker == null);
        if (first) {
            meMarker = new Marker(map);
            meMarker.setTitle("You are here");
            meMarker.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_mylocation));
            meMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            map.getOverlays().add(meMarker);
        }
        meMarker.setPosition(me);

        float best = Float.MAX_VALUE;
        Store bestStore = STORES[0];
        float[] out = new float[1];
        for (Store s : STORES) {
            Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), s.lat, s.lon, out);
            if (out[0] < best) { best = out[0]; bestStore = s; }
        }
        nearest = bestStore;
        tvNearest.setText(String.format(Locale.getDefault(),
                "Nearest store: %s (%.1f km away)", bestStore.name, best / 1000f));
        if (first && best < 50000f) map.getController().animateTo(me);   // recentre only if you are near the stores
        map.invalidate();
    }

    private void openDirections(Store s) {
        Uri uri = Uri.parse("geo:" + s.lat + "," + s.lon + "?q=" + s.lat + "," + s.lon
                + "(" + Uri.encode(s.name) + ")");
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No maps app available", Toast.LENGTH_SHORT).show();
        }
    }
}
