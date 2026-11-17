package com.yourname.qrinventory.utils;

import android.content.Context;
import android.net.Uri;
import com.yourname.qrinventory.data.AppDatabase;
import com.yourname.qrinventory.model.Product;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CsvUtils {
    private static final String HEADER = "qrCode,name,category,price,stock,lowStockThreshold,expiryDate";

    public static File exportProducts(Context ctx, List<Product> products, String prefix) throws IOException {
        File dir = new File(ctx.getCacheDir(), "exports");
        if (!dir.exists()) dir.mkdirs();
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(new Date());
        File file = new File(dir, prefix + "_" + stamp + ".csv");
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            w.write(HEADER);
            w.newLine();
            for (Product p : products) {
                w.write(esc(p.qrCode) + "," + esc(p.name) + "," + esc(p.category) + ","
                        + p.price + "," + p.stock + "," + p.lowStockThreshold + "," + p.expiryDate);
                w.newLine();
            }
        }
        return file;
    }

    // Existing QR codes are updated, new ones are added. Returns the number of rows imported.
    // Call on a background thread.
    public static int importProducts(Context ctx, Uri uri, AppDatabase db) throws IOException {
        InputStream in = ctx.getContentResolver().openInputStream(uri);
        if (in == null) throw new IOException("Cannot open file");
        int count = 0;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
            r.readLine();                            // skip header row
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                List<String> f = split(line);
                if (f.size() < 7) continue;
                try {
                    String qr = f.get(0).trim();
                    if (qr.isEmpty()) continue;
                    Product p = db.productDao().findByQr(qr);
                    boolean isNew = (p == null);
                    if (isNew) p = new Product();
                    p.qrCode = qr;
                    p.name = f.get(1).trim();
                    p.category = f.get(2).trim();
                    p.price = Double.parseDouble(f.get(3).trim());
                    p.stock = Integer.parseInt(f.get(4).trim());
                    p.lowStockThreshold = Integer.parseInt(f.get(5).trim());
                    p.expiryDate = Long.parseLong(f.get(6).trim());
                    if (isNew) db.productDao().insert(p); else db.productDao().update(p);
                    count++;
                } catch (NumberFormatException ignored) {
                    // skip rows with bad numbers
                }
            }
        }
        return count;
    }

    private static String esc(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static List<String> split(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') { sb.append('"'); i++; }
                    else inQuotes = false;
                } else sb.append(ch);
            } else {
                if (ch == '"') inQuotes = true;
                else if (ch == ',') { out.add(sb.toString()); sb.setLength(0); }
                else sb.append(ch);
            }
        }
        out.add(sb.toString());
        return out;
    }
}
