package com.example.ecommerce_final.models;

import java.util.List;

public class CartProducts {

    private String uid;
    private double price;
    private int quantity;
    private String description;
    private String encodedImage;
    public List<Carousel> carousels;

    public CartProducts(String uid, double price, int quantity, String description, String encodedImage) {
        this.uid = uid;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.encodedImage = encodedImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEncodedImage() {
        return encodedImage;
    }

    public void setEncodedImage(String encodedImage) {
        this.encodedImage = encodedImage;
    }

    public List<Carousel> getCarousels() {
        return carousels;
    }

    public void setCarousels(List<Carousel> carousels) {
        this.carousels = carousels;
    }
}
