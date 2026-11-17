package com.yourname.qrinventory.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import com.yourname.qrinventory.model.Order;
import com.yourname.qrinventory.model.OrderItem;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfInvoiceGenerator {
    private static final int W = 595, H = 842, M = 40;   // A4 at 72 dpi, margin

    public static File create(Context ctx, Order order, List<OrderItem> items) throws IOException {
        File dir = new File(ctx.getCacheDir(), "invoices");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, "invoice_" + order.id + ".pdf");

        Paint normal = new Paint(Paint.ANTI_ALIAS_FLAG);
        normal.setTextSize(12);
        Paint bold = new Paint(normal);
        bold.setTypeface(Typeface.DEFAULT_BOLD);
        Paint big = new Paint(bold);
        big.setTextSize(22);
        Paint normalR = new Paint(normal);
        normalR.setTextAlign(Paint.Align.RIGHT);
        Paint boldR = new Paint(bold);
        boldR.setTextAlign(Paint.Align.RIGHT);

        PdfDocument doc = new PdfDocument();
        int pageNo = 1;
        PdfDocument.Page page = doc.startPage(new PdfDocument.PageInfo.Builder(W, H, pageNo).create());
        Canvas c = page.getCanvas();

        String date = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date(order.timestamp));
        c.drawText("QR Smart Inventory Store", M, 60, big);
        c.drawText("Invoice #" + order.id, M, 90, bold);
        c.drawText(date, M, 108, normal);
        c.drawLine(M, 122, W - M, 122, normal);
        int y = columnHeader(c, bold, boldR, 142);

        for (OrderItem it : items) {
            if (y > H - 160) {                       // start a new page when full
                doc.finishPage(page);
                pageNo++;
                page = doc.startPage(new PdfDocument.PageInfo.Builder(W, H, pageNo).create());
                c = page.getCanvas();
                y = columnHeader(c, bold, boldR, 60);
            }
            String name = it.productName.length() > 34 ? it.productName.substring(0, 34) + "..." : it.productName;
            c.drawText(name, M, y, normal);
            c.drawText(String.valueOf(it.quantity), 360, y, normalR);
            c.drawText(money(it.unitPrice), 450, y, normalR);
            c.drawText(money(it.unitPrice * it.quantity), W - M, y, normalR);
            y += 20;
        }

        c.drawLine(M, y - 8, W - M, y - 8, normal);
        y += 12;
        y = totalRow(c, "Subtotal", order.subtotal, normal, normalR, y);
        if (order.discount > 0) y = totalRow(c, "Discount", -order.discount, normal, normalR, y);
        y = totalRow(c, "GST (5%)", order.tax, normal, normalR, y);
        y = totalRow(c, "TOTAL", order.total, bold, boldR, y + 4);
        c.drawText("Thank you for shopping with us!", M, y + 40, normal);

        doc.finishPage(page);
        try (FileOutputStream out = new FileOutputStream(file)) {
            doc.writeTo(out);
        } finally {
            doc.close();
        }
        return file;
    }

    private static int columnHeader(Canvas c, Paint bold, Paint boldR, int y) {
        c.drawText("Item", M, y, bold);
        c.drawText("Qty", 360, y, boldR);
        c.drawText("Price", 450, y, boldR);
        c.drawText("Amount", W - M, y, boldR);
        c.drawLine(M, y + 8, W - M, y + 8, bold);
        return y + 30;
    }

    private static int totalRow(Canvas c, String label, double value, Paint left, Paint right, int y) {
        c.drawText(label, 360, y, left);
        c.drawText(money(value), W - M, y, right);
        return y + 20;
    }

    private static String money(double v) {
        return String.format(Locale.US, "Rs. %.2f", v);
    }
}
