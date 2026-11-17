package com.yourname.qrinventory.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "products", indices = {@Index(value = "qrCode", unique = true)})
public class Product {
    @PrimaryKey(autoGenerate = true) public long id;
    public String qrCode;          // e.g. "PROD-1042"
    public String name;
    public String category;
    public double price;
    public int stock;
    public int lowStockThreshold;
    public long expiryDate;        // epoch millis, 0 = none
    public String imageUrl;
}