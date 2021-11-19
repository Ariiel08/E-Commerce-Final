package com.example.ecommerce_final.ui.splash;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ecommerce_final.MainActivity;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.ui.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private final static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();


        }, SPLASH_TIME_OUT);
    }
}