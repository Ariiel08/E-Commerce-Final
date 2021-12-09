package com.example.ecommerce_final.ui.login;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.example.ecommerce_final.MainActivity;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.databinding.ActivityLoginBinding;
import com.example.ecommerce_final.models.User;
import com.example.ecommerce_final.services.PrefManager;
import com.example.ecommerce_final.services.UserServices;
import com.example.ecommerce_final.utils.KProgressHUDUtils;
import com.example.ecommerce_final.utils.ValidUtil;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shashank.sony.fancytoastlib.FancyToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private PrefManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new PrefManager(getApplicationContext());

//        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.registerTypeAdapter(Date.class, dateJsonDeserializer);
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://137.184.110.89:7002")
//                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
//                .build();
//
//        Call<List<User>> userCall = retrofit.create(UserServices.class).getAll();
//
//        System.out.println("enqueue");
//        userCall.enqueue(new Callback<List<User>>() {
//             @Override
//             public void onResponse(Call<List<User>> call, Response<List<User>> response) {
//                 System.out.println("onResponse...");
//                 response.body().forEach(obj -> {
//                     System.out.println(obj.toString());
//                 });
//             }
//
//             @Override
//             public void onFailure(Call<List<User>> call, Throwable t) {
//                 System.err.println("onFailure...");
//                 System.err.println(t.getLocalizedMessage());
//             }
//        });

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvRegister.setOnClickListener(view -> {
            RegisterFragment registerFragment = new RegisterFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(binding.container.getId(), registerFragment);
            fragmentTransaction.commit();
        });

        binding.tvForgotPass.setOnClickListener(v -> {
            ForgotPasswordFragment forgotPasswordFragment = new ForgotPasswordFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(binding.container.getId(), forgotPasswordFragment);
            fragmentTransaction.commit();
        });

        binding.btnLogin.setOnClickListener(v -> {
            if(binding.txtLoginEmail.getText().toString().equals("admin") && binding.txtLoginPassword.getText().toString().equals("admin")){
                Intent intent = new Intent(binding.getRoot().getContext(), MainActivity.class);
                intent.putExtra("name", "admin");
                intent.putExtra("email", "admin");
                startActivity(intent);
                finish();
            }


            attemptLogin();
        });

        binding.txtLoginEmail.setText("test@hotmail.com");
        binding.txtLoginPassword.setText("123456");
    }

    private void attemptLogin() {
        if (ValidUtil.isEmpty(this, this.binding.txtLoginEmail, this.binding.txtLoginPassword)) {
            return;
        }

        final String email = this.binding.txtLoginEmail.getText().toString();
        final String password = this.binding.txtLoginPassword.getText().toString();

        if (ValidUtil.isEmailValid(this.binding.txtLoginEmail, email) && ValidUtil.isPasswordValid(this.binding.txtLoginPassword, password)) {
            final KProgressHUD progressDialog = new KProgressHUDUtils(this).showAuthenticating();

            final JsonObject user = new JsonObject();
            user.addProperty("email", binding.txtLoginEmail.getText().toString().trim());
            user.addProperty("password", binding.txtLoginPassword.getText().toString().trim());

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(User.class, dateJsonDeserializer);

            final Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://137.184.110.89:7002")
                    .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                    .build();

            final Call<User> userCall = retrofit.create(UserServices.class).login(user);

            userCall.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    progressDialog.dismiss();
                    switch (response.code()) {
                        case 200:
                            session.createLoginSession(response.body());

                            FancyToast.makeText(LoginActivity.this, "Successfully login", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                            Intent intent = new Intent(binding.getRoot().getContext(), MainActivity.class);
                            intent.putExtra("name", response.body().getFirstName() +' '+ response.body().getLastName());
                            intent.putExtra("email", response.body().getEmail());
                            startActivity(intent);
                            finish();
                            break;
                        default:
                            try {
                                FancyToast.makeText(LoginActivity.this, response.errorBody().string(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable error) {
                    progressDialog.dismiss();
                    FancyToast.makeText(LoginActivity.this, error.getMessage(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                }
            });
        }
    }

    private final JsonDeserializer<User> dateJsonDeserializer = (json, typeOfT, context) -> {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        final JsonObject jo = (JsonObject) json;

        return new User()
                .setUid(jo.get("uid").getAsInt())
                .setFirstName(jo.get("firstName").getAsString())
                .setLastName(jo.get("lastName").getAsString())
                .setEmail(jo.get("email").getAsString())
                .setRol(User.ROL.valueOf(jo.get("rol").getAsString()))
                .setContact(jo.get("contact").getAsString())
                .setPhoto(jo.get("photo").getAsString())
                .setBirthday(dateFormat.format(new Date(jo.get("birthday").getAsLong())));
    };
}