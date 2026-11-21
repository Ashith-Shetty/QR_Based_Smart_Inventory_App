package com.yourname.qrinventory.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.print.PrintHelper;

import com.yourname.qrinventory.R;
import com.yourname.qrinventory.utils.QrUtils;

public class QrCodeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.qr_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String qr = getIntent().getStringExtra("qr");
        String name = getIntent().getStringExtra("name");
        Bitmap bmp = QrUtils.qr(qr, 800);

        ((ImageView) findViewById(R.id.iv_qr)).setImageBitmap(bmp);
        ((TextView) findViewById(R.id.tv_code)).setText(qr);
        ((TextView) findViewById(R.id.tv_name)).setText(name);

        findViewById(R.id.btn_print).setOnClickListener(v -> {
            PrintHelper helper = new PrintHelper(this);
            helper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            helper.printBitmap("QR " + qr, bmp);   // system print dialog (also "Save as PDF")
        });
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}