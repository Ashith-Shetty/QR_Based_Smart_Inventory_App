package com.yourname.qrinventory.data;
import com.yourname.qrinventory.model.*;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface OrderDao {
    @Insert long insertOrder(Order o);
    @Insert void insertItems(List<OrderItem> items);

    @Query("SELECT * FROM orders ORDER BY timestamp DESC") LiveData<List<Order>> getAll();
    @Query("SELECT * FROM order_items WHERE orderId = :id") List<OrderItem> itemsOf(long id);
    @Query("SELECT * FROM orders WHERE id = :id") Order findById(long id);

    @Query("SELECT productName, SUM(quantity) AS totalSold FROM order_items " +
            "GROUP BY productName ORDER BY totalSold DESC LIMIT 5")
    LiveData<List<TopSeller>> topSellers();

    @Query("SELECT strftime('%Y-%m-%d', timestamp/1000, 'unixepoch') AS day, SUM(total) AS total " +
            "FROM orders GROUP BY day ORDER BY day")
    LiveData<List<DailySales>> dailySales();
}
