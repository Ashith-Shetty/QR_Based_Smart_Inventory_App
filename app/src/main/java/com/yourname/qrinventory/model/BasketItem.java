package com.yourname.qrinventory.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "basket_items",
        foreignKeys = @ForeignKey(entity = Product.class, parentColumns = "id",
                childColumns = "productId", onDelete = ForeignKey.CASCADE),
        indices = {@Index("productId")})
public class BasketItem {
    @PrimaryKey(autoGenerate = true) public long id;
    public long productId;
    public int quantity;
}