package com.example.ecommerce_final.ui.splash;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ecommerce_final.MainActivity;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.services.PrefManager;
import com.example.ecommerce_final.ui.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private PrefManager session;

    private final static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        session = new PrefManager(getApplicationContext());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            session.checkLogin();

            if(session.isLoggedIn()){
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }

            finish();


        }, SPLASH_TIME_OUT);
    }
}