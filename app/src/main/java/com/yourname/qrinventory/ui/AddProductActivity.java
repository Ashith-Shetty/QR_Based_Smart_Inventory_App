package com.yourname.qrinventory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.yourname.qrinventory.R;
import com.yourname.qrinventory.model.Product;
import com.yourname.qrinventory.network.ProductLookup;
import com.yourname.qrinventory.service.SystemEventReceiver;
import com.yourname.qrinventory.viewmodel.ProductViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AddProductActivity extends AppCompatActivity {

    private ProductViewModel vm;
    private TextInputEditText etQr, etName, etCategory, etPrice, etStock, etThreshold, etExpiry;
    private Button btnScan;
    private long editId = -1;
    private Product editing = null;
    private long expiryMillis = 0;
    private String imageUrl = "";
    private List<Product> all = new ArrayList<>();
    private boolean populated = false;

    // Scanner in "pick mode" returns the raw code instead of adding to the basket
    private final ActivityResultLauncher<Intent> scanLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == RESULT_OK && r.getData() != null) {
                    String code = r.getData().getStringExtra("code");
                    if (code != null) lookup(code);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editId = getIntent().getLongExtra("product_id", -1);
        getSupportActionBar().setTitle(editId >= 0 ? "Edit product" : "Add product");

        etQr = findViewById(R.id.et_qr);
        etName = findViewById(R.id.et_name);
        etCategory = findViewById(R.id.et_category);
        etPrice = findViewById(R.id.et_price);
        etStock = findViewById(R.id.et_stock);
        etThreshold = findViewById(R.id.et_threshold);
        etExpiry = findViewById(R.id.et_expiry);
        btnScan = findViewById(R.id.btn_scan_barcode);

        vm = new ViewModelProvider(this).get(ProductViewModel.class);
        vm.products.observe(this, list -> {
            all = list == null ? new ArrayList<>() : list;
            if (populated) return;
            populated = true;
            if (editId >= 0) {
                for (Product p : all) if (p.id == editId) { populate(p); break; }
            } else {
                etQr.setText(nextCode());
            }
        });

        btnScan.setOnClickListener(v ->
                scanLauncher.launch(new Intent(this, ScannerActivity.class).putExtra("pick_mode", true)));

        etExpiry.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Expiry date").build();
            picker.addOnPositiveButtonClickListener(sel -> {
                expiryMillis = sel;
                etExpiry.setText(formatDate(sel));
            });
            picker.show(getSupportFragmentManager(), "expiry");
        });
        etExpiry.setOnLongClickListener(v -> { etExpiry.setText(""); expiryMillis = 0; return true; });

        findViewById(R.id.btn_save).setOnClickListener(v -> save());
    }

    private String nextCode() {
        int max = 1000;
        for (Product p : all) {
            if (p.qrCode != null && p.qrCode.startsWith("PROD-")) {
                try { max = Math.max(max, Integer.parseInt(p.qrCode.substring(5))); }
                catch (NumberFormatException ignored) {}
            }
        }
        return "PROD-" + (max + 1);
    }

    private void populate(Product p) {
        editing = p;
        etQr.setText(p.qrCode);
        etName.setText(p.name);
        etCategory.setText(p.category);
        etPrice.setText(String.valueOf(p.price));
        etStock.setText(String.valueOf(p.stock));
        etThreshold.setText(String.valueOf(p.lowStockThreshold));
        imageUrl = p.imageUrl == null ? "" : p.imageUrl;
        expiryMillis = p.expiryDate;
        if (p.expiryDate > 0) etExpiry.setText(formatDate(p.expiryDate));
    }

    // REST call through Ashith's Retrofit wrapper (CO4)
    private void lookup(String code) {
        if (!SystemEventReceiver.isOnline(this)) {
            snack("You are offline. Enter the details manually.");
            etQr.setText(code);
            return;
        }
        btnScan.setEnabled(false);
        btnScan.setText("Looking up…");
        ProductLookup.lookup(code, (product, error) -> {
            btnScan.setEnabled(true);
            btnScan.setText("Scan packet barcode to auto-fill");
            if (error != null || product == null) {
                etQr.setText(code);
                snack("Not found online (" + error + "). Enter the details manually.");
                return;
            }
            etQr.setText(product.qrCode != null ? product.qrCode : code);
            etName.setText(product.name);
            etCategory.setText(product.category);
            imageUrl = product.imageUrl == null ? "" : product.imageUrl;
            snack("Filled from Open Food Facts. Now enter price and stock.");
            etPrice.requestFocus();
        });
    }

    private void save() {
        String qr = text(etQr), name = text(etName), cat = text(etCategory);
        if (qr.isEmpty()) { etQr.setError("Required"); return; }
        if (name.isEmpty()) { etName.setError("Required"); return; }
        double price;
        try { price = Double.parseDouble(text(etPrice)); }
        catch (NumberFormatException e) { etPrice.setError("Enter a price"); return; }
        int stock, threshold;
        try {
            stock = text(etStock).isEmpty() ? 0 : Integer.parseInt(text(etStock));
            threshold = text(etThreshold).isEmpty() ? 5 : Integer.parseInt(text(etThreshold));
        } catch (NumberFormatException e) { etStock.setError("Enter a whole number"); return; }

        for (Product p : all) {
            if (qr.equals(p.qrCode) && (editing == null || p.id != editing.id)) {
                etQr.setError("This QR code is already used");
                return;
            }
        }

        Product p = editing != null ? editing : new Product();
        p.qrCode = qr;
        p.name = name;
        p.category = cat.isEmpty() ? "General" : cat;
        p.price = price;
        p.stock = stock;
        p.lowStockThreshold = threshold;
        p.expiryDate = text(etExpiry).isEmpty() ? 0 : expiryMillis;
        p.imageUrl = imageUrl;

        if (editing != null) vm.update(p); else vm.add(p);
        Toast.makeText(this, "Saved " + name, Toast.LENGTH_SHORT).show();
        finish();
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private String formatDate(long utcMillis) {
        SimpleDateFormat f = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        f.setTimeZone(TimeZone.getTimeZone("UTC"));   // the date picker returns UTC midnight
        return f.format(new Date(utcMillis));
    }

    private void snack(String msg) {
        Snackbar.make(findViewById(R.id.add_root), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}