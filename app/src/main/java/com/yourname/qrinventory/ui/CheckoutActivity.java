package com.yourname.qrinventory.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.yourname.qrinventory.R;
import com.yourname.qrinventory.model.BasketLine;
import com.yourname.qrinventory.utils.ReportExporter;
import com.yourname.qrinventory.utils.ShareUtils;
import com.yourname.qrinventory.viewmodel.BasketViewModel;

import java.io.File;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private BasketViewModel vm;
    private double subtotal = 0;
    private String lastTxn;
    private File invoice;
    private TextView tvItems, tvSubtotal, tvDiscount, tvGst, tvTotal, tvSuccess;
    private CompoundButton cbDiscount;
    private Button btnPay, btnShare, btnView;
    private View summaryGroup, successGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        View root = findViewById(R.id.checkout_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvItems = findViewById(R.id.tv_items);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDiscount = findViewById(R.id.tv_discount);
        tvGst = findViewById(R.id.tv_gst);
        tvTotal = findViewById(R.id.tv_total);
        tvSuccess = findViewById(R.id.tv_success);
        cbDiscount = findViewById(R.id.cb_discount);
        btnPay = findViewById(R.id.btn_pay);
        btnShare = findViewById(R.id.btn_share);
        btnView = findViewById(R.id.btn_view);
        summaryGroup = findViewById(R.id.summary_group);
        successGroup = findViewById(R.id.success_group);

        vm = new ViewModelProvider(this).get(BasketViewModel.class);
        vm.lines.observe(this, lines -> {
            int n = 0;
            if (lines != null) for (BasketLine l : lines) n += l.quantity;
            tvItems.setText(n + " item(s) in basket");
        });
        vm.subtotal.observe(this, s -> { subtotal = s == null ? 0 : s; updateTotals(); });
        cbDiscount.setOnCheckedChangeListener((b, checked) -> updateTotals());

        btnPay.setOnClickListener(v -> {
            if (subtotal <= 0) {
                Snackbar.make(findViewById(R.id.checkout_root), "Basket is empty", Snackbar.LENGTH_LONG).show();
                return;
            }
            double discount = cbDiscount.isChecked() ? subtotal * 0.10 : 0;
            double total = (subtotal - discount) * 1.05;
            // Simulated UPI payment first; the order is only placed once the payment succeeds
            new UpiPaymentDialog(this, total, txn -> {
                lastTxn = txn;
                btnPay.setEnabled(false);
                vm.checkout(cbDiscount.isChecked() ? 10 : 0);
            }).show();
        });
        vm.orderResult.observe(this, this::onOrder);

        btnShare.setOnClickListener(v ->
                ShareUtils.shareFile(this, invoice, "application/pdf", "Your bill"));   // implicit intent
        btnView.setOnClickListener(v -> ShareUtils.viewFile(this, invoice, "application/pdf"));
        findViewById(R.id.btn_done).setOnClickListener(v -> finish());
    }

    private void updateTotals() {
        double disc = cbDiscount.isChecked() ? subtotal * 0.10 : 0;
        double gst = (subtotal - disc) * 0.05;
        tvSubtotal.setText(String.format(Locale.getDefault(), "Subtotal: ₹%.2f", subtotal));
        tvDiscount.setText(String.format(Locale.getDefault(), "Discount: -₹%.2f", disc));
        tvGst.setText(String.format(Locale.getDefault(), "GST (5%%): ₹%.2f", gst));
        tvTotal.setText(String.format(Locale.getDefault(), "Total: ₹%.2f", subtotal - disc + gst));
    }

    private void onOrder(Long id) {
        if (id == null) return;
        if (id < 0) {
            Snackbar.make(findViewById(R.id.checkout_root), "Basket is empty", Snackbar.LENGTH_LONG).show();
            return;
        }
        summaryGroup.setVisibility(View.GONE);
        successGroup.setVisibility(View.VISIBLE);
        tvSuccess.setText("Order #" + id + " placed. Preparing your bill…");

        ReportExporter.exportInvoice(this, id, (file, error) -> {
            if (error != null) {
                tvSuccess.setText("Order #" + id + " placed, but the bill failed: " + error);
                return;
            }
            invoice = file;
            tvSuccess.setText("Order #" + id + " placed"
                    + (lastTxn != null ? " (paid via UPI, " + lastTxn + ")" : "") + ". Your bill is ready.");
            btnShare.setEnabled(true);
            btnView.setEnabled(true);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}