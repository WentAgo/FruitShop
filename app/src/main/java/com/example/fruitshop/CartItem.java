package com.example.fruitshop;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class CartItem {

    private String itemId;
    private String itemName;
    private double itemPrice;
    private long quantity;

    @ServerTimestamp
    private Date timestamp;

    public CartItem() {
    }

    public CartItem(String itemId, String itemName, double itemPrice, long quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.quantity = quantity;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }


    @PropertyName("timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedPrice() {
        return String.format("$%.2f", itemPrice);
    }

    public double getTotalItemPrice() {
        return itemPrice * quantity;
    }

    public String getFormattedTotalItemPrice() {
        return String.format("$%.2f", getTotalItemPrice());
    }
}