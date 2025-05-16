package com.example.fruitshop;

public class Item {
    private String itemId;
    private int imageId;
    private String description;
    private String price;
    private String details;
    private String imageUrl;

    public Item() {
    }

    public Item(String itemId, int imageId, String description, String price, String details) {
        this.itemId = itemId;
        this.imageId = imageId;
        this.description = description;
        this.price = price;
        this.details = details;
    }

    public String getItemId() {
        return itemId;
    }

    public int getImageId() {
        return imageId;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getDetails() {
        return details;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}