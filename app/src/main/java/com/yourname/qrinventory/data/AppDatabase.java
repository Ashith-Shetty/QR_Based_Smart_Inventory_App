package com.yourname.qrinventory.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.yourname.qrinventory.model.BasketItem;
import com.yourname.qrinventory.model.Order;
import com.yourname.qrinventory.model.OrderItem;
import com.yourname.qrinventory.model.Product;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Product.class, BasketItem.class, Order.class, OrderItem.class},
        version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProductDao productDao();
    public abstract BasketDao basketDao();
    public abstract OrderDao orderDao();

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService IO = Executors.newFixedThreadPool(4);

    public static AppDatabase get(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(ctx.getApplicationContext(),
                                    AppDatabase.class, "inventory.db")
                            .fallbackToDestructiveMigration()   // fine during development
                            .addCallback(new RoomDatabase.Callback() {
                                @Override public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    IO.execute(() -> SeedData.insert(INSTANCE));
                                }
                            }).build();
                }
            }
        }
        return INSTANCE;
    }
}