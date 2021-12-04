package com.example.ecommerce_final.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartProducts> cartProducts = new ArrayList<>();

    public Cart(){

    }

    public Cart(List<CartProducts> cartProducts) {
        this.cartProducts = cartProducts;
    }

    public List<CartProducts> getProducts() {
        return cartProducts;
    }

    public void setProducts(List<CartProducts> products) {
        this.cartProducts = products;
    }

    public void setProduct(CartProducts product){
        cartProducts.add(product);
    }
}
