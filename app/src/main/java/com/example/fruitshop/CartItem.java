package com.example.fruitshop;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class CartItem {

    private String itemId; // Matches the field in your Firestore document
    private String itemName; // Matches 'itemName' in Firestore
    private double itemPrice;  // Matches 'itemPrice' in Firestore (stored as a number)
    private long quantity;     // Matches 'quantity' in Firestore
    private String imageUrl;   // Optional: if you store this in the cart document for direct display
    // If you don't store imageUrl in the cart item, you might need to fetch it
    // from your main "products" collection using the itemId.

    @ServerTimestamp
    private Date timestamp;    // Matches 'timestamp' in Firestore

    // Default constructor is required for Firestore data mapping
    public CartItem() {
    }

    public CartItem(String itemId, String itemName, double itemPrice, long quantity, String imageUrl) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.quantity = quantity;
        this.imageUrl = imageUrl; // Can be null if not used/stored directly in cart
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @PropertyName("timestamp") // Ensure correct mapping if getter name differs from field
    public Date getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // --- Helper methods for display ---

    /**
     * Returns the price formatted as a currency string.
     * Consider using NumberFormat for locale-specific currency formatting.
     */
    public String getFormattedPrice() {
        return String.format("$%.2f", itemPrice);
    }

    /**
     * Returns the total price for this cart item (itemPrice * quantity).
     */
    public double getTotalItemPrice() {
        return itemPrice * quantity;
    }

    public String getFormattedTotalItemPrice() {
        return String.format("$%.2f", getTotalItemPrice());
    }
}