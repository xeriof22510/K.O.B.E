package com.example.user.kobe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 讀取記事標題
        String title = intent.getStringExtra("title");
        // 顯示訊息框
        for (int i = 0; i < 5; i++) {
            Toast.makeText(context, title, Toast.LENGTH_LONG).show();
        }
    }
}