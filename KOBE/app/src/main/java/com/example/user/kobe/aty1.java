package com.example.user.kobe;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class aty1 extends AppCompatActivity {

    private ListView item_list;

    // ListView使用的自定Adapter物件
    private ItemAdapter itemAdapter;
    // 儲存所有記事本的List物件
    private List<Item> items;

    // 選單項目物件
    private MenuItem add_item, revert_item, delete_item;

    // 已選擇項目數量
    private int selectedCount = 0;

    // 宣告資料庫功能類別欄位變數
    private ItemDAO itemDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main1);

        processViews();
        processControllers();

        // 建立資料庫物件
        itemDAO = new ItemDAO(getApplicationContext());

        // 取得所有記事資料
        items = itemDAO.getAll();

        itemAdapter = new ItemAdapter(this, R.layout.singleitem, items);
        item_list.setAdapter(itemAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Item item = (Item) data.getExtras().getSerializable(
                    "com.example.user.kobe.Item");

            // 是否修改提醒設定
            boolean updateAlarm = false;

            if (requestCode == 0) {
                // 新增記事資料到資料庫
                item = itemDAO.insert(item);

                items.add(item);
                itemAdapter.notifyDataSetChanged();

                updateAlarm = true;
            }
            // 如果是修改記事
            else if (requestCode == 1) {
                // 讀取記事編號
                int position = data.getIntExtra("position", -1);

                if (position != -1) {
                    // 讀取原來的提醒設定
                    Item ori = itemDAO.get(item.getId());
                    // 判斷是否需要設定提醒
                    updateAlarm = (item.getAlarmDatetime() != ori.getAlarmDatetime());

                    itemDAO.update(item);
                    items.set(position, item);
                    itemAdapter.notifyDataSetChanged();
                }
            }

            // 設定提醒
            if (item.getAlarmDatetime() != 0 && updateAlarm) {
                Intent intent = new Intent(this, AlarmReceiver.class);

                intent.putExtra("title", item.getTitle());
                // 加入記事編號資料
                intent.putExtra("id", item.getId());

                PendingIntent pi = PendingIntent.getBroadcast(
                        this, (int)item.getId(),
                        intent, PendingIntent.FLAG_ONE_SHOT);

                AlarmManager am = (AlarmManager)
                        getSystemService(Context.ALARM_SERVICE);
                am.set(AlarmManager.RTC_WAKEUP, item.getAlarmDatetime(), pi);
            }
        }
    }

    public TextView show_app_name;
    private void processViews() {

        item_list = (ListView) findViewById(R.id.item_list);
        show_app_name = (TextView) findViewById(R.id.textView);
    }

    private void processControllers() {
        // 建立選單項目點擊監聽物件
        AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // 讀取選擇的記事物件
                Item item = itemAdapter.getItem(position);

                // 如果已經有勾選的項目
                if (selectedCount > 0) {
                    // 處理是否顯示已選擇項目
                    processMenu(item);
                    // 重新設定記事項目
                    itemAdapter.set(position, item);
                } else {
                    Intent intent = new Intent(
                            aty1.this, event.class);

                    // 設定記事編號與記事物件
                    intent.putExtra("position", position);
                    intent.putExtra("com.example.user.kobe.Item", item);
                    intent.putExtra("mode", 1);
                    startActivityForResult(intent, 1);
                }
            }
        };

        // 註冊選單項目點擊監聽物件
        item_list.setOnItemClickListener(itemListener);

        // 建立記事項目長按監聽物件
        AdapterView.OnItemLongClickListener itemLongListener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                // 讀取選擇的記事物件
                Item item = itemAdapter.getItem(position);
                // 處理是否顯示已選擇項目
                processMenu(item);
                // 重新設定記事項目
                itemAdapter.set(position, item);
                return true;
            }
        };

        // 註冊記事項目長按監聽物件
        item_list.setOnItemLongClickListener(itemLongListener);

        // 建立長按監聽物件
        View.OnLongClickListener listener = new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder dialog =
                        new AlertDialog.Builder(aty1.this);
                dialog.setTitle(R.string.app_name)
                        .setMessage(R.string.version)
                        .show();
                return false;
            }

        };

        // 註冊長按監聽物件
        show_app_name.setOnLongClickListener(listener);
    }

    // 處理是否顯示已選擇項目
    private void processMenu(Item item) {
        // 如果需要設定記事項目
        if (item != null) {
            // 設定已勾選的狀態
            item.setSelected(!item.isSelected());

            // 計算已勾選數量
            if (item.isSelected()) {
                selectedCount++;
            }
            else {
                selectedCount--;
            }
        }

        // 根據選擇的狀況，設定是否顯示選單項目
        add_item.setVisible(selectedCount == 0);
        revert_item.setVisible(selectedCount > 0);
        delete_item.setVisible(selectedCount > 0);
    }

    // 載入選單資源
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        // 取得選單項目物件
        add_item = menu.findItem(R.id.add_item);
        revert_item = menu.findItem(R.id.revert_item);
        delete_item = menu.findItem(R.id.delete_item);

        // 設定選單項目
        processMenu(null);

        return true;
    }

    // 使用者選擇所有的選單項目都會呼叫這個方法
    public void clickMenuItem(MenuItem item) {
        int itemId = item.getItemId();

        // 判斷該執行什麼工作
        switch (itemId) {
            // 使用者選擇新增選單項目
            case R.id.add_item:
                // 使用Action名稱建立啟動另一個Activity元件需要的Intent物件
                Intent intent = new Intent(aty1.this, event.class);
                // 呼叫「startActivityForResult」，，第二個參數「0」表示執行新增
                intent.putExtra("mode", 0);
                startActivityForResult(intent, 0);
                break;
            // 取消所有已勾選的項目
            case R.id.revert_item:
                for (int i = 0; i < itemDAO.getCount(); i++) {
                    Item ri = itemAdapter.getItem(i);

                    if (ri.isSelected()) {
                        ri.setSelected(false);
                        itemAdapter.set(i, ri);
                    }
                }

                selectedCount = 0;
                processMenu(null);

                break;
            // 刪除
            case R.id.delete_item:
                // 沒有選擇
                if (selectedCount == 0) {
                    break;
                }

                // 建立與顯示詢問是否刪除的對話框
                AlertDialog.Builder d = new AlertDialog.Builder(this);
                String message = getString(R.string.delete_item);
                d.setTitle(R.string.delete)
                        .setMessage(String.format(message, selectedCount));
                d.setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 取得最後一個元素的編號
                                int index = itemDAO.getCount() - 1;

                                while (index > -1) {
                                    Item item = itemAdapter.get(index);

                                    if (item.isSelected()) {
                                        itemAdapter.remove(item);
                                        // 刪除資料庫中的記事資料
                                        itemDAO.delete(item.getId());
                                    }

                                    index--;
                                }

                                itemAdapter.notifyDataSetChanged();
                                selectedCount = 0;
                                processMenu(null);
                            }
                        });
                d.setNegativeButton(android.R.string.no, null);
                d.show();

                break;
        }
    }

    // 點擊應用程式名稱元件後呼叫的方法
    public void about(View view) {
        // 建立啟動另一個Activity元件需要的Intent物件
        // 建構式的第一個參數：「this」
        // 建構式的第二個參數：「Activity元件類別名稱.class」
        Intent intent = new Intent(this, about.class);
        // 呼叫「startActivity」，參數為一個建立好的Intent物件
        // 這行敘述執行以後，如果沒有任何錯誤，就會啟動指定的元件
        startActivity(intent);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
    // 設定
    public void clickPreferences(MenuItem item) {
        // 啟動設定元件
        startActivity(new Intent(this, PrefActivity.class));
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        itemDAO.close();
    }
}
