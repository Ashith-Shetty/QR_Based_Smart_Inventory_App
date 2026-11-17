package com.yourname.qrinventory.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.yourname.qrinventory.model.BasketItem;
import com.yourname.qrinventory.model.BasketLine;
import com.yourname.qrinventory.model.CategoryStock;
import com.yourname.qrinventory.model.DailySales;
import com.yourname.qrinventory.model.Order;
import com.yourname.qrinventory.model.OrderItem;
import com.yourname.qrinventory.model.Product;
import com.yourname.qrinventory.model.TopSeller;

import java.util.ArrayList;
import java.util.List;

public class InventoryRepository {
    private final AppDatabase db;
    public InventoryRepository(Context ctx) { db = AppDatabase.get(ctx); }

    public interface Callback<T> { void onResult(T result); }

    // ---------- LiveData getters (for ViewModels / screens) ----------
    public LiveData<List<Product>> getProducts() { return db.productDao().getAll(); }
    public LiveData<List<Product>> searchProducts(String q) { return db.productDao().search(q); }
    public LiveData<List<Product>> productsByCategory(String c) { return db.productDao().byCategory(c); }
    public LiveData<List<String>> getCategories() { return db.productDao().categories(); }
    public LiveData<List<BasketLine>> getBasketLines() { return db.basketDao().getLines(); }
    public LiveData<List<Order>> getOrders() { return db.orderDao().getAll(); }
    public LiveData<List<CategoryStock>> getStockByCategory() { return db.productDao().stockByCategory(); }
    public LiveData<List<TopSeller>> getTopSellers() { return db.orderDao().topSellers(); }
    public LiveData<List<DailySales>> getDailySales() { return db.orderDao().dailySales(); }

    // ---------- Product CRUD ----------
    public void addProduct(Product p) { AppDatabase.IO.execute(() -> db.productDao().insert(p)); }
    public void updateProduct(Product p) { AppDatabase.IO.execute(() -> db.productDao().update(p)); }
    public void deleteProduct(Product p) { AppDatabase.IO.execute(() -> db.productDao().delete(p)); }

    // ---------- Basket ----------
    // Called after a QR scan. The callback runs on a background thread, so post to the UI thread in the caller.
    public void addScannedToBasket(String qr, Callback<String> cb) {
        AppDatabase.IO.execute(() -> {
            Product p = db.productDao().findByQr(qr);
            if (p == null) { cb.onResult("Unknown product"); return; }
            BasketItem existing = db.basketDao().findByProduct(p.id);
            int newQty = (existing == null ? 0 : existing.quantity) + 1;
            if (newQty > p.stock) { cb.onResult("Only " + p.stock + " in stock"); return; }
            if (existing == null) {
                BasketItem b = new BasketItem();
                b.productId = p.id; b.quantity = 1;
                db.basketDao().insert(b);
            } else {
                existing.quantity = newQty;
                db.basketDao().update(existing);
            }
            cb.onResult("Added: " + p.name);
        });
    }

    public void changeQuantity(BasketLine line, int newQty) {
        AppDatabase.IO.execute(() -> {
            if (newQty <= 0) { db.basketDao().remove(line.basketId); return; }
            if (newQty > line.stock) return;
            BasketItem b = new BasketItem();
            b.id = line.basketId; b.productId = line.productId; b.quantity = newQty;
            db.basketDao().update(b);
        });
    }

    public void removeFromBasket(long basketId) { AppDatabase.IO.execute(() -> db.basketDao().remove(basketId)); }
    public void clearBasket() { AppDatabase.IO.execute(() -> db.basketDao().clear()); }

    // ---------- Checkout ----------
    public void checkout(double discountPercent, Callback<Long> cb) {   // returns order id, or -1 if empty
        AppDatabase.IO.execute(() -> {
            long orderId = db.runInTransaction(() -> {
                List<BasketItem> items = db.basketDao().getAllNow();
                if (items.isEmpty()) return -1L;
                double subtotal = 0;
                List<OrderItem> orderItems = new ArrayList<>();
                for (BasketItem b : items) {
                    Product p = db.productDao().findById(b.productId);
                    subtotal += p.price * b.quantity;
                    p.stock -= b.quantity;                       // reduce inventory
                    db.productDao().update(p);
                    OrderItem oi = new OrderItem();
                    oi.productName = p.name; oi.unitPrice = p.price; oi.quantity = b.quantity;
                    orderItems.add(oi);
                }
                double discount = subtotal * discountPercent / 100.0;
                double tax = (subtotal - discount) * 0.05;       // 5% GST
                Order o = new Order();
                o.timestamp = System.currentTimeMillis();
                o.subtotal = subtotal; o.discount = discount; o.tax = tax;
                o.total = subtotal - discount + tax;
                long id = db.orderDao().insertOrder(o);
                for (OrderItem oi : orderItems) oi.orderId = id;
                db.orderDao().insertItems(orderItems);
                db.basketDao().clear();
                return id;
            });
            cb.onResult(orderId);   // now runs after the transaction has committed
        });
    }

    // ---------- Used by reports (PDF/CSV), next batch ----------
    public List<OrderItem> getOrderItems(long orderId) { return db.orderDao().itemsOf(orderId); }   // call on a background thread
    public Order getOrder(long orderId) { return db.orderDao().findById(orderId); }                  // call on a background thread
}
