package com.example.ecommerce_final;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.ecommerce_final.databinding.ActivityMainBinding;
import com.example.ecommerce_final.models.User;
import com.example.ecommerce_final.services.FirebaseServices;
import com.example.ecommerce_final.services.NetResponse;
import com.example.ecommerce_final.services.PrefManager;
import com.example.ecommerce_final.ui.login.LoginActivity;
import com.google.android.material.navigation.NavigationView;
import com.mikhaellopez.circularimageview.CircularImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private PrefManager session;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        retrieveSession();

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_category, R.id.nav_product, R.id.nav_profile)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(item -> {
            session.logout();
            //After logout redirect user to Login Activity
            Intent intentLogout = new Intent(MainActivity.this, LoginActivity.class);
            //Staring Login Activity
            startActivity(intentLogout);
            //Finish all the Activities
            finish();
            return true;
        });

        TextView tvUsername = navigationView.getHeaderView(0).findViewById(R.id.tvUsername);
        TextView tvEmail = navigationView.getHeaderView(0).findViewById(R.id.tvEmail);
        ImageView imgProfile = navigationView.getHeaderView(0).findViewById(R.id.imgProfileNav);

        tvUsername.setText(user.getFirstName() + " " + user.getLastName());
        tvEmail.setText(user.getEmail());

        if (user.getPhoto() != null && !user.getPhoto().isEmpty()) {
            FirebaseServices.obtain().download(user.getPhoto(), new NetResponse<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    System.out.println(response.toString());
                    imgProfile.setImageBitmap(response);
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, t.getMessage());
                }
            });
        }
    }

    private void retrieveSession() {
        session = new PrefManager(getApplicationContext());
        user = session.getUserSession();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_cart:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(R.id.nav_cart);
                return true;

            case R.id.action_search:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(R.id.nav_search);
                return true;

            case R.id.action_notifications:
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(R.id.nav_notifications);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}