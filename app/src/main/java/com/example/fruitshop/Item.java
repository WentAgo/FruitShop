package com.example.fruitshop;

public class Item {
    private int imageId;
    private String description;
    private String price;
    private String details;

    public Item(int imageId,String description, String price, String details) {
        this.imageId = imageId;
        this.description = description;
        this.price = price;
        this.details = details;
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
}