package com.yourname.qrinventory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.yourname.qrinventory.MainActivity;
import com.yourname.qrinventory.R;
import com.yourname.qrinventory.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private boolean registerMode = false;
    private TextView tvSubtitle;
    private View tilName, tilAdmin, roleGroup;
    private TextInputEditText etName, etEmail, etPassword, etAdmin;
    private Button btnSubmit, btnToggle;
    private MaterialButtonToggleGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        SessionManager.seedDemoUsers(this);

        tvSubtitle = findViewById(R.id.tv_subtitle);
        tilName = findViewById(R.id.til_name);
        tilAdmin = findViewById(R.id.til_admin);
        roleGroup = findViewById(R.id.role_group);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etAdmin = findViewById(R.id.et_admin);
        btnSubmit = findViewById(R.id.btn_submit);
        btnToggle = findViewById(R.id.btn_toggle);
        group = findViewById(R.id.role_group);

        group.addOnButtonCheckedListener((g, id, checked) -> updateUi());
        btnToggle.setOnClickListener(v -> { registerMode = !registerMode; updateUi(); });
        btnSubmit.setOnClickListener(v -> submit());
        updateUi();
    }

    private void updateUi() {
        boolean adminChosen = group.getCheckedButtonId() == R.id.btn_admin;
        tilName.setVisibility(registerMode ? View.VISIBLE : View.GONE);
        roleGroup.setVisibility(registerMode ? View.VISIBLE : View.GONE);
        tilAdmin.setVisibility(registerMode && adminChosen ? View.VISIBLE : View.GONE);
        tvSubtitle.setText(registerMode ? "Create your account" : "Welcome back");
        btnSubmit.setText(registerMode ? "Create account" : "Log in");
        btnToggle.setText(registerMode ? "Already registered? Log in" : "New here? Create an account");
    }

    private void submit() {
        String email = text(etEmail), password = text(etPassword);
        String error;
        if (registerMode) {
            String role = group.getCheckedButtonId() == R.id.btn_admin
                    ? SessionManager.ROLE_ADMIN : SessionManager.ROLE_CUSTOMER;
            error = SessionManager.register(this, text(etName), email, password, role, text(etAdmin));
        } else {
            error = SessionManager.login(this, email, password);
        }
        if (error != null) {
            Snackbar.make(findViewById(R.id.login_root), error, Snackbar.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // no back to login
        startActivity(i);
        finish();
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString();
    }
}