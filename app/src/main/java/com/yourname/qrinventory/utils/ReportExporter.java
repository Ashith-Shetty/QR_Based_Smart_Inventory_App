package com.yourname.qrinventory.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.yourname.qrinventory.data.AppDatabase;
import com.yourname.qrinventory.model.Order;
import com.yourname.qrinventory.model.OrderItem;
import com.yourname.qrinventory.model.Product;
import java.io.File;
import java.util.List;

public class ReportExporter {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    // error == null means success
    public interface ResultCallback<T> { void onResult(T result, String error); }

    public static void exportInvoice(Context ctx, long orderId, ResultCallback<File> cb) {
        Context app = ctx.getApplicationContext();
        AppDatabase.IO.execute(() -> {
            try {
                AppDatabase db = AppDatabase.get(app);
                Order o = db.orderDao().findById(orderId);
                if (o == null) { MAIN.post(() -> cb.onResult(null, "Order not found")); return; }
                List<OrderItem> items = db.orderDao().itemsOf(orderId);
                File f = PdfInvoiceGenerator.create(app, o, items);
                MAIN.post(() -> cb.onResult(f, null));
            } catch (Exception e) {
                MAIN.post(() -> cb.onResult(null, "Could not create invoice: " + e.getMessage()));
            }
        });
    }

    public static void exportInventoryCsv(Context ctx, ResultCallback<File> cb) {
        Context app = ctx.getApplicationContext();
        AppDatabase.IO.execute(() -> {
            try {
                List<Product> all = AppDatabase.get(app).productDao().getAllNow();
                File f = CsvUtils.exportProducts(app, all, "inventory");
                MAIN.post(() -> cb.onResult(f, null));
            } catch (Exception e) {
                MAIN.post(() -> cb.onResult(null, "Could not export CSV: " + e.getMessage()));
            }
        });
    }

    public static void exportLowStockCsv(Context ctx, ResultCallback<File> cb) {
        Context app = ctx.getApplicationContext();
        AppDatabase.IO.execute(() -> {
            try {
                List<Product> low = AppDatabase.get(app).productDao().lowStockNow();
                File f = CsvUtils.exportProducts(app, low, "low_stock");
                MAIN.post(() -> cb.onResult(f, null));
            } catch (Exception e) {
                MAIN.post(() -> cb.onResult(null, "Could not export report: " + e.getMessage()));
            }
        });
    }

    public static void importInventoryCsv(Context ctx, Uri uri, ResultCallback<Integer> cb) {
        Context app = ctx.getApplicationContext();
        AppDatabase.IO.execute(() -> {
            try {
                int n = CsvUtils.importProducts(app, uri, AppDatabase.get(app));
                MAIN.post(() -> cb.onResult(n, null));
            } catch (Exception e) {
                MAIN.post(() -> cb.onResult(null, "Could not import CSV: " + e.getMessage()));
            }
        });
    }
}