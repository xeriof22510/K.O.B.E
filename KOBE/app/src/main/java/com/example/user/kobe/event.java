package com.example.user.kobe;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class event extends AppCompatActivity {

    private EditText title_text, content_text;

    // 啟動功能用的請求代碼
    public static final int START_ALARM = 3;
    public static final int START_COLOR = 4;

    // 記事物件
    private Item item;

    private Calendar cal1 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_event);

        processViews();

        // 取得Intent物件
        Intent intent = getIntent();

//        // 讀取Action名稱
//        String action = intent.getAction();



        // 如果是修改記事
        int mode = intent.getExtras().getInt("mode");
       if (mode == 1) {
            // 接收記事物件與設定標題、內容
            item = (Item) intent.getExtras().getSerializable(
                    "com.example.user.kobe.Item");
            title_text.setText(item.getTitle());
            content_text.setText(item.getContent());
        }
        // 新增記事
         else if(mode == 0){
            item = new Item();
         }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case START_ALARM:
                    break;
                // 設定顏色
                case START_COLOR:
                    int colorId = data.getIntExtra(
                            "colorId", Colors.LIGHTGREY.parseColor());
                    item.setColor(getColors(colorId));
                    break;
            }
        }
    }

    public static Colors getColors(int color) {
        Colors result = Colors.LIGHTGREY;

        if (color == Colors.BLUE.parseColor()) {
            result = Colors.BLUE;
        }
        else if (color == Colors.PURPLE.parseColor()) {
            result = Colors.PURPLE;
        }
        else if (color == Colors.GREEN.parseColor()) {
            result = Colors.GREEN;
        }
        else if (color == Colors.ORANGE.parseColor()) {
            result = Colors.ORANGE;
        }
        else if (color == Colors.RED.parseColor()) {
            result = Colors.RED;
        }

        return result;
    }

    private void processViews() {
        title_text = (EditText) findViewById(R.id.title_text);
        content_text = (EditText) findViewById(R.id.content_text);
    }

    // 點擊確定與取消按鈕都會呼叫這個方法
    public void onSubmit(View view) {
        // 確定按鈕
        if (view.getId() == R.id.ok_item) {
            String titleText = title_text.getText().toString();
            String contentText = content_text.getText().toString();

            item.setTitle(titleText);
            item.setContent(contentText);

//            if (getIntent().getAction().equals(
//                    "com.example.user.kobe.EDIT_ITEM")) {
//                item.setLastModify(new Date().getTime());
//            }
//            //新增記事
//            else {
                item.setDatetime( item.getAlarmDatetime());
                // 建立SharedPreferences物件
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(this);
                // 讀取設定的預設顏色
                int color = sharedPreferences.getInt("DEFAULT_COLOR", -1);
                item.setColor(getColors(color));
//            }

            Intent result = getIntent();
            result.putExtra("com.example.user.kobe.Item", item);
            setResult(Activity.RESULT_OK, result);
        }

        // 結束
        finish();
    }

    public void clickFunction(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.set_alarm:
                // 設定提醒日期時間
                processSetAlarm();
                break;
            case R.id.select_color:
                // 啟動設定顏色的Activity元件
                startActivityForResult(new Intent(event.this, ColorActivity.class), START_COLOR);
                break;
        }
    }

    // 設定提醒日期時間
    private void processSetAlarm() {
        Calendar calendar = Calendar.getInstance();

        if (item.getAlarmDatetime() != 0) {
            // 設定為已經儲存的提醒日期時間
            calendar.setTimeInMillis(item.getAlarmDatetime());
        }

        // 讀取年、月、日、時、分
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 儲存設定的提醒日期時間
        final Calendar alarm = Calendar.getInstance();


        // 設定提醒時間
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        alarm.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        alarm.set(Calendar.MINUTE, minute);

                        item.setAlarmDatetime(alarm.getTimeInMillis());
                    }
        };

        // 選擇時間對話框
        final TimePickerDialog tpd = new TimePickerDialog(this, timeSetListener, hour, minute, true);

        // 設定提醒日期
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        alarm.set(Calendar.YEAR, year);
                        alarm.set(Calendar.MONTH, monthOfYear);
                        alarm.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // 繼續選擇提醒時間
                        tpd.show();
                    }
        };

        // 建立與顯示選擇日期對話框
        final DatePickerDialog dpd = new DatePickerDialog(this, dateSetListener, year, month, day);
        dpd.show();

//        Log.d("alarm", String.valueOf(alarm.get(Calendar.DATE)));
   }
}