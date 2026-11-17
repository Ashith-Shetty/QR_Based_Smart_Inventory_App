package com.yourname.qrinventory.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteQueryBuilder;

public class ProductProvider extends ContentProvider {
    public static final String AUTHORITY = "com.yourname.qrinventory.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/products");
    public static final Uri LOW_STOCK_URI = Uri.parse("content://" + AUTHORITY + "/products/low_stock");

    private static final int PRODUCTS = 1, PRODUCT_ID = 2, LOW_STOCK = 3;
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        MATCHER.addURI(AUTHORITY, "products", PRODUCTS);
        MATCHER.addURI(AUTHORITY, "products/low_stock", LOW_STOCK);
        MATCHER.addURI(AUTHORITY, "products/#", PRODUCT_ID);
    }

    @Override public boolean onCreate() { return true; }

    @Nullable @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SupportSQLiteDatabase db = AppDatabase.get(getContext()).getOpenHelper().getReadableDatabase();
        SupportSQLiteQueryBuilder b = SupportSQLiteQueryBuilder.builder("products");
        if (projection != null) b.columns(projection);
        if (sortOrder != null) b.orderBy(sortOrder);
        switch (MATCHER.match(uri)) {
            case PRODUCTS:
                b.selection(selection, selectionArgs);
                break;
            case PRODUCT_ID:
                b.selection("id = ?", new Object[]{uri.getLastPathSegment()});
                break;
            case LOW_STOCK:
                b.selection("stock <= lowStockThreshold", null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return db.query(b.create());
    }

    @Nullable @Override
    public String getType(@NonNull Uri uri) {
        switch (MATCHER.match(uri)) {
            case PRODUCT_ID: return "vnd.android.cursor.item/vnd." + AUTHORITY + ".product";
            case PRODUCTS:
            case LOW_STOCK: return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".product";
            default: return null;
        }
    }

    // Read-only provider: other apps can look at the products but not change them
    @Nullable @Override public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) { return null; }
    @Override public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] a) { return 0; }
    @Override public int update(@NonNull Uri uri, @Nullable ContentValues v, @Nullable String s, @Nullable String[] a) { return 0; }
}