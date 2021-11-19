package com.example.ecommerce_final.ui.login;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;
import com.example.ecommerce_final.MainActivity;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvRegister.setOnClickListener(view -> {
            RegisterFragment registerFragment = new RegisterFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(binding.container.getId(), registerFragment);
            fragmentTransaction.commit();
        });

        binding.btnLogin.setOnClickListener(v -> {
            if(binding.txtLoginEmail.getText().toString().equals("admin") && binding.txtLoginPassword.getText().toString().equals("admin")){
                Intent intent = new Intent(binding.getRoot().getContext(), MainActivity.class);
                intent.putExtra("username", "admin");
                intent.putExtra("email", "admin");
                startActivity(intent);
                finish();
            }

        });
    }
}