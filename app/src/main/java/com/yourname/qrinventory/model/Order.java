package com.yourname.qrinventory.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "orders")
public class Order {
    @PrimaryKey(autoGenerate = true) public long id;
    public long timestamp;
    public double subtotal, tax, discount, total;
}
