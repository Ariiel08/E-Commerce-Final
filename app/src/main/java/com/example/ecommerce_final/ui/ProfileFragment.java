package com.example.ecommerce_final.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.ecommerce_final.databinding.FragmentProfileBinding;
import com.example.ecommerce_final.models.User;
import com.example.ecommerce_final.services.FirebaseServices;
import com.example.ecommerce_final.services.NetResponse;
import com.example.ecommerce_final.services.PrefManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private PrefManager pref;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        pref = new PrefManager(root.getContext());

        user = pref.getUserSession();

        if (user.getPhoto() != null && !user.getPhoto().isEmpty()) {
            FirebaseServices.obtain().download(user.getPhoto(), new NetResponse<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    System.out.println(response.toString());
                    binding.imgProfile2.setImageBitmap(response);
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, t.getMessage());
                }
            });
        }

        binding.tvProfileName.setText(user.getFirstName()+' '+user.getLastName());

        binding.tvProfileEmail.setText("Email: "+user.getEmail());

        binding.tvProfileContact.setText("Contact: "+user.getContact());

        binding.tvProfileRol.setText("Rol: "+user.getRol().toString());

        binding.tvProfileBirthday.setText("Birthday: "+user.getBirthday().substring(0, 10));

        return root;
    }
}