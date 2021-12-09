package com.example.ecommerce_final.ui.login;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.ecommerce_final.MainActivity;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.databinding.FragmentForgotPasswordBinding;
import com.example.ecommerce_final.models.User;
import com.example.ecommerce_final.services.UserServices;
import com.example.ecommerce_final.utils.KProgressHUDUtils;
import com.example.ecommerce_final.utils.ValidUtil;
import com.google.gson.JsonObject;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shashank.sony.fancytoastlib.FancyToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;


public class ForgotPasswordFragment extends Fragment {
    private FragmentForgotPasswordBinding binding;
    private View root;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        binding.btnChangePassword.setOnClickListener(v -> {
            changePasword();
        });

        return root;
    }

    private void changePasword(){
        if (ValidUtil.isEmpty(root.getContext(), this.binding.txtEmailForgot, this.binding.txtNewPass)) {
            return;
        }

        final String email = binding.txtEmailForgot.getText().toString().trim();
        final String password = binding.txtNewPass.getText().toString().trim();


        if (ValidUtil.isEmailValid(this.binding.txtEmailForgot, email) && ValidUtil.isPasswordValid(this.binding.txtNewPass, password)) {
            final KProgressHUD progressDialog = new KProgressHUDUtils(root.getContext()).showAuthenticating();

            final JsonObject user = new JsonObject();
            user.addProperty("email", binding.txtEmailForgot.getText().toString().trim());
            user.addProperty("password", binding.txtNewPass.getText().toString().trim());


            final Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://137.184.110.89:7002")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            final Call<Void> changePasswordCall = retrofit.create(UserServices.class).changePassword(user);

            changePasswordCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    progressDialog.dismiss();
                    switch (response.code()) {
                        case 204:
                            FancyToast.makeText(root.getContext(), "Password succesfully changed", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                            Intent intent = new Intent(binding.getRoot().getContext(), LoginActivity.class);
                            startActivity(intent);
                            getParentFragmentManager().beginTransaction().remove(ForgotPasswordFragment.this).commitAllowingStateLoss();
                            break;
                        default:
                            try {
                                FancyToast.makeText(root.getContext(), response.errorBody().string(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable error) {
                    progressDialog.dismiss();
                    FancyToast.makeText(root.getContext(), error.getMessage(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                }
            });

        }
    }
}