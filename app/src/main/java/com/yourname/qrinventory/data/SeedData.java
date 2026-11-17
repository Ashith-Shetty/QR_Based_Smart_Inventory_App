package com.yourname.qrinventory.data;

import com.yourname.qrinventory.model.Order;
import com.yourname.qrinventory.model.OrderItem;
import com.yourname.qrinventory.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SeedData {
    public static void insert(AppDatabase db) {
        List<Product> list = new ArrayList<>();
        // Dairy
        list.add(make("PROD-1001", "Amul Milk 1L", "Dairy", 68, 40, 10, 5));
        list.add(make("PROD-1002", "Amul Butter 100g", "Dairy", 58, 30, 8, 90));
        list.add(make("PROD-1003", "Curd 400g", "Dairy", 40, 25, 8, 7));
        list.add(make("PROD-1004", "Paneer 200g", "Dairy", 95, 12, 5, 10));
        // Bakery
        list.add(make("PROD-1005", "Britannia Bread", "Bakery", 45, 25, 8, 3));
        list.add(make("PROD-1006", "Cream Buns (4 pack)", "Bakery", 50, 4, 6, 4));
        list.add(make("PROD-1007", "Rusk 200g", "Bakery", 35, 20, 5, 60));
        // Grocery
        list.add(make("PROD-1008", "Basmati Rice 5kg", "Grocery", 620, 15, 5, 365));
        list.add(make("PROD-1009", "Toor Dal 1kg", "Grocery", 165, 18, 6, 300));
        list.add(make("PROD-1010", "Sunflower Oil 1L", "Grocery", 140, 22, 8, 240));
        list.add(make("PROD-1011", "Sugar 1kg", "Grocery", 48, 35, 10, 365));
        list.add(make("PROD-1012", "Tata Salt 1kg", "Grocery", 28, 50, 10, 500));
        // Snacks
        list.add(make("PROD-1013", "Lays Classic 52g", "Snacks", 20, 60, 15, 120));
        list.add(make("PROD-1014", "Parle-G 200g", "Snacks", 20, 45, 12, 150));
        list.add(make("PROD-1015", "Maggi Noodles 4 pack", "Snacks", 56, 30, 10, 180));
        list.add(make("PROD-1016", "Dairy Milk 50g", "Snacks", 45, 3, 8, 200));
        // Beverages
        list.add(make("PROD-1017", "Coca-Cola 750ml", "Beverages", 40, 35, 10, 150));
        list.add(make("PROD-1018", "Tropicana Orange 1L", "Beverages", 110, 14, 5, 45));
        list.add(make("PROD-1019", "Tata Tea 250g", "Beverages", 130, 20, 6, 300));
        list.add(make("PROD-1020", "Bisleri Water 1L", "Beverages", 20, 80, 20, 365));
        // Household
        list.add(make("PROD-1021", "Surf Excel 1kg", "Household", 230, 16, 5, 0));
        list.add(make("PROD-1022", "Vim Dishwash Gel", "Household", 99, 18, 6, 0));
        list.add(make("PROD-1023", "Colgate Toothpaste", "Household", 85, 28, 8, 0));
        list.add(make("PROD-1024", "Lifebuoy Soap (3 pack)", "Household", 90, 2, 6, 0));
        db.productDao().insertAll(list);
        insertDemoSales(db);
    }

    // expiryDays = 0 means no expiry
    private static Product make(String qr, String name, String cat, double price,
                                int stock, int low, int expiryDays) {
        Product p = new Product();
        p.qrCode = qr; p.name = name; p.category = cat; p.price = price;
        p.stock = stock; p.lowStockThreshold = low;
        p.expiryDate = expiryDays == 0 ? 0 : System.currentTimeMillis() + expiryDays * 86400000L;
        return p;
    }
    // Fake order history for the last 7 days so the sales charts and order history have data in the demo
    public static void insertDemoSales(AppDatabase db) {
        List<Product> products = db.productDao().getAllNow();
        if (products.isEmpty()) return;
        Random rnd = new Random(42);
        long now = System.currentTimeMillis();
        for (int d = 6; d >= 0; d--) {
            int ordersToday = 2 + rnd.nextInt(3);
            for (int k = 0; k < ordersToday; k++) {
                List<OrderItem> items = new ArrayList<>();
                double subtotal = 0;
                int lines = 2 + rnd.nextInt(3);
                for (int i = 0; i < lines; i++) {
                    Product p = products.get(rnd.nextInt(products.size()));
                    OrderItem oi = new OrderItem();
                    oi.productName = p.name;
                    oi.unitPrice = p.price;
                    oi.quantity = 1 + rnd.nextInt(3);
                    subtotal += oi.unitPrice * oi.quantity;
                    items.add(oi);
                }
                Order o = new Order();
                o.timestamp = now - d * 86400000L - rnd.nextInt(6 * 3600000);
                o.subtotal = subtotal;
                o.discount = 0;
                o.tax = subtotal * 0.05;
                o.total = subtotal + o.tax;
                long id = db.orderDao().insertOrder(o);
                for (OrderItem oi : items) oi.orderId = id;
                db.orderDao().insertItems(items);
            }
        }
    }
}
