package com.yourname.qrinventory.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.yourname.qrinventory.R;
import com.yourname.qrinventory.utils.SessionManager;

public class ProfileFragment extends Fragment {

    public ProfileFragment() { super(R.layout.fragment_profile); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context ctx = requireContext();

        ((TextView) view.findViewById(R.id.tv_name)).setText(SessionManager.getName(ctx));
        ((TextView) view.findViewById(R.id.tv_email)).setText(SessionManager.getEmail(ctx));
        ((TextView) view.findViewById(R.id.tv_role)).setText(
                SessionManager.isAdmin(ctx) ? "Store manager (Admin)" : "Customer");

        // Implicit intents (CO2): no permission needed, the system apps do the work
        view.findViewById(R.id.btn_call).setOnClickListener(v ->
                startSafely(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+918012345678"))));
        view.findViewById(R.id.btn_maps).setOnClickListener(v ->
                startSafely(new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "geo:12.9716,77.5946?q=12.9716,77.5946(" + Uri.encode("QR Mart Supermarket") + ")"))));
        view.findViewById(R.id.btn_store_map).setOnClickListener(v ->
                startActivity(new Intent(ctx, StoreMapActivity.class)));

        // Dark mode toggle (CO3)
        MaterialSwitch dark = view.findViewById(R.id.switch_dark);
        boolean isDark = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        dark.setChecked(isDark);
        dark.setOnCheckedChangeListener((b, checked) -> {
            int mode = checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            SessionManager.saveNightMode(ctx, mode);
            AppCompatDelegate.setDefaultNightMode(mode);
        });

        view.findViewById(R.id.btn_logout).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(ctx)
                        .setTitle("Log out?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Log out", (d, w) -> {
                            SessionManager.logout(ctx);
                            Intent i = new Intent(ctx, LoginActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        }).show());
    }

    private void startSafely(Intent i) {
        try { startActivity(i); }
        catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "No app available for this action", Toast.LENGTH_SHORT).show();
        }
    }
}