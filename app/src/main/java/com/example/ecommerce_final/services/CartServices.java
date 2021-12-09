package com.example.ecommerce_final.services;

import com.example.ecommerce_final.models.CarouselProducts;
import com.example.ecommerce_final.models.Cart;
import com.example.ecommerce_final.models.CartProducts;

import java.util.ArrayList;
import java.util.List;

public class CartServices {
    private static CartServices instance;
    private Cart cart = new Cart();

    private CartServices(){

    }

    public static CartServices getInstance(){
        if(instance == null){
            instance = new CartServices();
        }
        return instance;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void setCartProducts(CartProducts product){
        cart.setProduct(product);
    }

    public void setCarouselProducts(CarouselProducts carouselProducts){
        cart.setCarouselProduct(carouselProducts);
    }
}
