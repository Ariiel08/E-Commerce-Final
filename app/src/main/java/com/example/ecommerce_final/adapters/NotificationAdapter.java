package com.example.ecommerce_final.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerce_final.databinding.ItemNotificationBinding;
import com.example.ecommerce_final.models.NotificationModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{
    private ItemNotificationBinding binding;
    private List<NotificationModel> notificationModels;
    private final Context context;

    public NotificationAdapter(Context context){
        this.context = context;
    }

    public void setNotifications(List<NotificationModel> notificationModels){
        this.notificationModels = notificationModels;
        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = ItemNotificationBinding.inflate(inflater, parent, false);
        return new NotificationAdapter.ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull NotificationAdapter.ViewHolder holder, int position) {
        holder.tvTitle.setText(notificationModels.get(position).getTitle());
        holder.tvContent.setText(notificationModels.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        if(notificationModels == null){
            return 0;
        }

        return notificationModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvTitle = binding.tvNotiTitle;
            tvContent = binding.tvNotiContent;
        }
    }
}
