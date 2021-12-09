package com.example.ecommerce_final.services;

import android.net.Uri;
import com.example.ecommerce_final.models.Carousel;


public class CarouselUpload {
    public Uri uri;
    public Carousel carousel;

    public CarouselUpload(Uri uri, Carousel carousel) {
        this.uri = uri;
        this.carousel = carousel;
    }
}
