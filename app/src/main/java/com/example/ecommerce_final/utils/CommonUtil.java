package com.example.ecommerce_final.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;

import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommonUtil {

    private static final String TAG = "CommonUtil";


    public static void alertDialog(@NonNull Context context, @NonNull String title, @NonNull String message, @NonNull VoidListener consumer) {
        new AlertDialog.Builder(context).setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> consumer.accept())
                .setNegativeButton("No", (dialog, which) -> dialog.cancel()).show();

    }
}
