package com.example.ecommerce_final.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerce_final.adapters.NotificationAdapter;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.databinding.FragmentNotificationBinding;
import com.example.ecommerce_final.models.NotificationModel;

import java.util.ArrayList;
import java.util.List;


public class NotificationFragment extends Fragment {
    private FragmentNotificationBinding binding;
    private AppDatabase appDatabase;
    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appDatabase = AppDatabase.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        notificationAdapter = new NotificationAdapter(root.getContext());

        NotificationViewModel notificationViewModel = new NotificationViewModel(appDatabase);
        recyclerView = binding.recyclerNoti;
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setAdapter(notificationAdapter);

        notificationViewModel.getNotificationListLiveData().observe(this, new Observer<List<NotificationModel>>() {
            @Override
            public void onChanged(List<NotificationModel> notificationModels) {
                notificationAdapter.setNotifications(notificationModels);
            }
        });

        binding.btnClearNoti.setOnClickListener(v -> {
            AppExecutors.getInstance().diskIO().execute(() -> {
                appDatabase.notificationDAO().deleteAll();
            });

            notificationViewModel.getNotificationListLiveData().observe(this, new Observer<List<NotificationModel>>() {
                @Override
                public void onChanged(List<NotificationModel> notificationModels) {
                    notificationAdapter.setNotifications(new ArrayList<>());
                }
            });
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}