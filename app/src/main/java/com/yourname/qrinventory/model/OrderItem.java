package com.yourname.qrinventory.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "order_items",
        foreignKeys = @ForeignKey(entity = Order.class, parentColumns = "id",
                childColumns = "orderId", onDelete = ForeignKey.CASCADE),
        indices = {@Index("orderId")})
public class OrderItem {
    @PrimaryKey(autoGenerate = true) public long id;
    public long orderId;
    public String productName;     // snapshot, in case product is edited later
    public double unitPrice;
    public int quantity;
}