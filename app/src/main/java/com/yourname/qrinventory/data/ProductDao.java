package com.yourname.qrinventory.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.yourname.qrinventory.model.CategoryStock;
import com.yourname.qrinventory.model.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) long insert(Product p);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertAll(List<Product> list);
    @Update void update(Product p);
    @Delete void delete(Product p);

    @Query("SELECT * FROM products ORDER BY name") LiveData<List<Product>> getAll();
    @Query("SELECT * FROM products WHERE name LIKE '%' || :q || '%' OR category LIKE '%' || :q || '%'")
    LiveData<List<Product>> search(String q);
    @Query("SELECT * FROM products WHERE name = :name LIMIT 1") Product findByName(String name);
    @Query("SELECT * FROM products WHERE category = :cat") LiveData<List<Product>> byCategory(String cat);
    @Query("SELECT * FROM products WHERE qrCode = :code LIMIT 1") Product findByQr(String code);
    @Query("SELECT * FROM products WHERE id = :id") Product findById(long id);
    @Query("SELECT DISTINCT category FROM products") LiveData<List<String>> categories();
    @Query("SELECT * FROM products WHERE stock <= lowStockThreshold") List<Product> lowStockNow();
    @Query("SELECT * FROM products WHERE expiryDate > 0 AND expiryDate < :limit") List<Product> expiringBefore(long limit);
    @Query("SELECT COUNT(*) FROM products") int count();

    // report queries
    @Query("SELECT category, SUM(stock) AS totalStock FROM products GROUP BY category")
    LiveData<List<CategoryStock>> stockByCategory();
    @Query("SELECT * FROM products") List<Product> getAllNow();   // for CSV export
}
