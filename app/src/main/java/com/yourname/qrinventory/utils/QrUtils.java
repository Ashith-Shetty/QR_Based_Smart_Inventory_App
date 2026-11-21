package com.yourname.qrinventory.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.yourname.qrinventory.model.Product;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class QrUtils {
    private QrUtils() {}

    public static Bitmap qr(String text, int size) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix m = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints);
            int[] px = new int[size * size];
            for (int y = 0; y < size; y++)
                for (int x = 0; x < size; x++)
                    px[y * size + x] = m.get(x, y) ? Color.BLACK : Color.WHITE;
            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            bmp.setPixels(px, 0, size, 0, 0, size, size);
            return bmp;
        } catch (WriterException e) {
            return null;
        }
    }

    /** One printable page-width sheet with every product's QR code and label (4 per row). */
    public static Bitmap sheet(List<Product> products) {
        int cols = 4, width = 1240, cellW = width / cols, cellH = 290, top = 70;
        int rows = (products.size() + cols - 1) / cols;
        Bitmap bmp = Bitmap.createBitmap(width, top + rows * cellH, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);

        Paint title = new Paint(Paint.ANTI_ALIAS_FLAG);
        title.setColor(Color.BLACK);
        title.setTextSize(34);
        title.setFakeBoldText(true);
        c.drawText("QR Mart - product codes", 30, 45, title);

        Paint code = new Paint(Paint.ANTI_ALIAS_FLAG);
        code.setColor(Color.BLACK);
        code.setTextAlign(Paint.Align.CENTER);
        code.setTextSize(26);
        code.setFakeBoldText(true);
        Paint name = new Paint(code);
        name.setTextSize(22);
        name.setFakeBoldText(false);

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            int cx = (i % cols) * cellW + cellW / 2;
            int y = top + (i / cols) * cellH;
            Bitmap q = qr(p.qrCode, 200);
            if (q != null) c.drawBitmap(q, cx - 100, y, null);
            c.drawText(p.qrCode, cx, y + 232, code);
            String n = p.name == null ? "" : p.name;
            if (n.length() > 22) n = n.substring(0, 21) + "…";
            c.drawText(n, cx, y + 260, name);
        }
        return bmp;
    }
}