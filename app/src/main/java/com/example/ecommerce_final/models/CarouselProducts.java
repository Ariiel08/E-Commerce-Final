package com.example.ecommerce_final.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CarouselProducts implements Serializable {

    @Embedded
    public Product product;
    @Relation(
            parentColumn = "id",
            entityColumn = "product",
            entity = Carousel.class
    )
    public List<Carousel> carousels;

    public CarouselProducts() {
        this.product = new Product();
        this.carousels = new ArrayList<>();
    }

    public CarouselProducts(Product product, List<Carousel> carousels) {
        this.product = product;
        this.carousels = carousels;
    }
}
