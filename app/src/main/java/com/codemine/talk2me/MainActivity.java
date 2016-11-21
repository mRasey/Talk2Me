package com.codemine.talk2me;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.IntRange;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RunnableFuture;

import static com.codemine.talk2me.MESSAGE.*;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    final int NEW_MSG = 1;
    ListView contractsList;
    List<Contact> contacts = new ArrayList<>();
    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase sqLiteDatabase;
    JSONObject contractJsonInfo;
    JSONObject callBackJson = new JSONObject();
    String myAccount;

    Sensor sensor;
    SensorManager sensorManager;
    private static final int SHAKE_THRESHOLD = 5000;//摇晃灵敏度
    SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float xValue = Math.abs(event.values[0]);
            float yValue = Math.abs(event.values[1]);
            float zValue = Math.abs(event.values[2]);
            if (xValue > 15 || yValue > 15 || zValue > 15) { // 认为用户摇动了手机，触发摇一摇逻辑
                //todo 摇晃事件
                Toast.makeText(MainActivity.this, "摇一摇", Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("op", "shakeNewFriend");
                            jsonObject.put("account", myAccount);
                            callBackJson = new SocketOperation(jsonObject).getMsg();
                            handler.sendMessage(MyMessage.createMessage(SHAKE_NEW_FRIEND));
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_FRIENDS_FROM_SERVER:
                    handler.removeMessages(GET_FRIENDS_FROM_SERVER);
                    contacts.clear();
                    try {
                        int i = 0;
                        while(callBackJson.has(i + "")) {
                            contacts.add(new Contact(callBackJson.getString(i + ""), "", "", R.drawable.head));
                            i++;
                        }
                        ContactsAdapter contactsAdapter = new ContactsAdapter(MainActivity.this, R.layout.contact_item, contacts);
                        contractsList.setAdapter(contactsAdapter);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case GET_NEW_MSG:
                    handler.removeMessages(GET_NEW_MSG);
                    //todo 从服务器更新消息
                    try {
                        JSONArray jsonArray = callBackJson.getJSONArray("info");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String fromAccount = jsonObject.getString("fromAccount");
                            String toAccount = jsonObject.getString("toAccount");
                            String info = jsonObject.getString("info");
                            String date = jsonObject.getString("date");
                            insertNewMsg(fromAccount, toAccount, info, date);//将新消息插入数据库
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SHAKE_NEW_FRIEND:
                    handler.removeMessages(SHAKE_NEW_FRIEND);
                    //todo callback
                    try {
                        String newFriend = callBackJson.getString("newFriend");
                        Intent intent = new Intent();
                        intent.putExtra("myAccount", myAccount);
                        intent.putExtra("oppositeAccount", newFriend);
                        intent.setClass(MainActivity.this, ChatActivity.class);
                        startActivity(intent);
                        initContactsFromServer();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        myAccount = getIntent().getStringExtra("account");//获取本机用户名

        mySQLiteOpenHelper = new MySQLiteOpenHelper(this, "data.db", null, 1);//创建数据库
        sqLiteDatabase = mySQLiteOpenHelper.getWritableDatabase();
        contractsList = (ListView) findViewById(R.id.contactsList);

        initContactsFromServer(); // 从服务器初始化联系人列表
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        contractsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Contact contact = contacts.get(position);
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("myAccount", myAccount);
                intent.putExtra("oppositeAccount", contact.name);
                intent.putExtra("contactHeadPortraitId", contact.headPortraitId);
                startActivity(intent);
            }
        });

        //摇晃监听，实现摇一摇随机推荐好友
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);


        //新建线程循环接收服务器的消息
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        //向服务器取信息
                        HashMap<String, String> pushIpMap = new HashMap<>();
                        pushIpMap.put("op", "getNewMsg");//定义操作
                        pushIpMap.put("account", myAccount);//指明账号
                        callBackJson = new SocketOperation(new JSONObject(pushIpMap)).getMsg();//向服务器更新本机地址
                        handler.sendMessage(MyMessage.createMessage(GET_NEW_MSG));
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



    /**
     * 从服务器数据库读取联系人列表
     */
    public void initContactsFromServer() {
        contacts.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    while (true) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("op", "getFriends");
                        jsonObject.put("account", myAccount);
                        callBackJson = new SocketOperation(jsonObject).getMsg();
                        handler.sendMessage(MyMessage.createMessage(GET_FRIENDS_FROM_SERVER));
//                        Thread.sleep(1000);
//                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 获取当前时间
     * @return
     */
    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    /**
     * 向聊天信息数据库插入新的信息
     * @param fromAccount
     * @param toAccount
     * @param info
     * @param date
     */
    public void insertNewMsg(String fromAccount, String toAccount, String info, String date) {
        ContentValues values = new ContentValues();
        values.put("fromAccount", fromAccount);
        values.put("toAccount", toAccount);
        values.put("info", info);
        values.put("date", date);
        System.out.println("insert:   " + sqLiteDatabase.insert("CHAT_RECORD", null, values));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == android.hardware.Sensor.TYPE_ACCELEROMETER) {

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
