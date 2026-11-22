package com.yourname.qrinventory.ui;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yourname.qrinventory.R;

import java.util.Locale;
import java.util.Random;

// Simulated UPI payment: asks for a UPI ID, shows a processing step, then a success screen with a transaction id
public class UpiPaymentDialog {

    public interface OnPaid { void onPaid(String transactionId); }

    private static final String UPI_PATTERN = "[a-zA-Z0-9._-]{2,}@[a-zA-Z]{2,}";

    private final Activity activity;
    private final double amount;
    private final OnPaid callback;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public UpiPaymentDialog(Activity activity, double amount, OnPaid callback) {
        this.activity = activity;
        this.amount = amount;
        this.callback = callback;
    }

    public void show() {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View v = LayoutInflater.from(activity).inflate(R.layout.sheet_upi_payment, null);
        dialog.setContentView(v);

        TextView tvAmount = v.findViewById(R.id.tv_pay_amount);
        ChipGroup apps = v.findViewById(R.id.chip_apps);
        TextInputLayout til = v.findViewById(R.id.til_upi);
        TextInputEditText et = v.findViewById(R.id.et_upi);
        Button btnPay = v.findViewById(R.id.btn_pay_now);
        View form = v.findViewById(R.id.pay_form);
        View progress = v.findViewById(R.id.pay_progress);
        View success = v.findViewById(R.id.pay_success);
        TextView tvTxn = v.findViewById(R.id.tv_txn);

        tvAmount.setText(String.format(Locale.getDefault(), "₹%.2f", amount));

        btnPay.setOnClickListener(x -> {
            String upi = et.getText() == null ? "" : et.getText().toString().trim();
            if (!upi.matches(UPI_PATTERN)) {
                til.setError("Enter a valid UPI ID, for example name@okbank");
                return;
            }
            til.setError(null);
            Chip chip = v.findViewById(apps.getCheckedChipId());
            String app = chip == null ? "UPI" : chip.getText().toString();

            dialog.setCancelable(false);   // no dismissing while the payment is processing
            form.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);

            handler.postDelayed(() -> {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                String txn = String.format(Locale.US, "UPI%09d", 100000000 + new Random().nextInt(900000000));
                progress.setVisibility(View.GONE);
                success.setVisibility(View.VISIBLE);
                tvTxn.setText("Transaction ID: " + txn + "\nPaid via " + app + " from " + upi);

                handler.postDelayed(() -> {
                    if (activity.isFinishing() || activity.isDestroyed()) return;
                    dialog.dismiss();
                    callback.onPaid(txn);
                }, 1500);
            }, 2000);
        });

        dialog.show();
    }
}