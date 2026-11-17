package com.yourname.qrinventory.service;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.yourname.qrinventory.data.AppDatabase;
import com.yourname.qrinventory.model.Product;
import java.util.List;

public class StockCheckWorker extends Worker {
    public StockCheckWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
    }

    @NonNull @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        AppDatabase db = AppDatabase.get(ctx);
        NotificationHelper.createChannel(ctx);

        List<Product> low = db.productDao().lowStockNow();
        long in3Days = System.currentTimeMillis() + 3L * 86400000L;
        List<Product> expiring = db.productDao().expiringBefore(in3Days);

        if (!low.isEmpty()) {
            NotificationHelper.show(ctx, 1001, "Low stock: " + low.size() + " items", names(low));
        }
        if (!expiring.isEmpty()) {
            NotificationHelper.show(ctx, 1002, "Expiring within 3 days: " + expiring.size() + " items", names(expiring));
        }
        return Result.success();
    }

    private static String names(List<Product> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size() && i < 5; i++) {
            if (i > 0) sb.append(", ");
            sb.append(list.get(i).name);
        }
        if (list.size() > 5) sb.append(" and ").append(list.size() - 5).append(" more");
        return sb.toString();
    }
}
