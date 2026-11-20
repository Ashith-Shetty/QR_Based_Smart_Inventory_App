package com.yourname.qrinventory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yourname.qrinventory.R;

public class ScanFragment extends Fragment {
    public ScanFragment() { super(R.layout.fragment_scan); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_open_scanner).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ScannerActivity.class)));   // explicit intent
    }
}