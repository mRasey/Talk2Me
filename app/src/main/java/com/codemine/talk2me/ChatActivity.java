package com.codemine.talk2me;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.codemine.talk2me.MESSAGE.*;

public class ChatActivity extends AppCompatActivity {

    ArrayList<ChattingInfo> chattingInfos = new ArrayList<>();
    ListView chatList;
    EditText inputMsgText;
    Button sendMsgButton;
    TextView backText;
    TextView chattingWith;
    String myAccount;
    String oppositeAccount;
    ChattingAdapter chattingAdapter;
    JSONObject callbackJson = new JSONObject();
    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase sqLiteDatabase;
    int chatInfoLength = 0;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_CURRENT_MSG:
                    handler.removeMessages(UPDATE_CURRENT_MSG);
                    //todo 从本地数据库加载信息
                    initChattingInfo();
                    if(chatInfoLength != chattingInfos.size()) { //如果之前的信息长度与现在的不一致表示有新的信息，则更新列表
                        chattingAdapter = new ChattingAdapter(ChatActivity.this, R.layout.chatting_item, chattingInfos);
                        chatList.setAdapter(chattingAdapter);
                        chatList.setSelection(chattingInfos.size() - 1);
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
        setContentView(R.layout.activity_chat);

        mySQLiteOpenHelper = new MySQLiteOpenHelper(this, "data.db", null, 1);//创建数据库
        sqLiteDatabase = mySQLiteOpenHelper.getWritableDatabase();

        chatList = (ListView) findViewById(R.id.chattingListView);
        inputMsgText = (EditText) findViewById(R.id.inputMsgText);
        sendMsgButton = (Button) findViewById(R.id.sendMsgButton);
        backText = (TextView) findViewById(R.id.back_text);
        chattingWith = (TextView) findViewById(R.id.chattingWith);



        myAccount = getIntent().getStringExtra("myAccount");
        oppositeAccount = getIntent().getStringExtra("oppositeAccount");
        chattingWith.setText(oppositeAccount);

        backText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initChattingInfo();
        chattingAdapter = new ChattingAdapter(ChatActivity.this, R.layout.chatting_item, chattingInfos);
        chatList.setAdapter(chattingAdapter);
        chatList.setSelection(chattingInfos.size() - 1);


        inputMsgText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!inputMsgText.getText().toString().equals(""))
                    sendMsgButton.setBackgroundColor(Color.parseColor("#FFC125"));
                else
                    sendMsgButton.setBackgroundColor(Color.parseColor("#EBEBEB"));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        sendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!inputMsgText.getText().toString().equals("")) {
                    String msg = inputMsgText.getText().toString();
                    String currentTime = Tools.getCurrentTime();
                    chattingInfos.add(new ChattingInfo(R.drawable.head, msg, MsgType.OWN, ""));
                    ChattingAdapter chattingAdapter = new ChattingAdapter(ChatActivity.this, R.layout.chatting_item, chattingInfos);
                    chatList.setAdapter(chattingAdapter);
                    chatList.setSelection(chattingInfos.size() - 1);
                    //todo 向服务端和本地数据库插入新的信息
                    insertNewMsg(myAccount, oppositeAccount, msg, currentTime);
                    new Thread(new SendNewMsgToServer(msg, currentTime)).start();
                }
                inputMsgText.getText().clear();
            }
        });

        //通知listview更新通信列表
        new Thread(new UpdateMsg()).start();
    }

    public Cursor getInfo() {
        String sql = "SELECT * FROM CHAT_RECORD WHERE (fromAccount = ? AND toAccount = ?) OR (fromAccount = ? AND toAccount = ?);";
        return sqLiteDatabase.rawQuery(sql, new String[]{myAccount, oppositeAccount, oppositeAccount, myAccount});
    }

    public void initChattingInfo() {
        //todo 从数据库加载历史消息
        chatInfoLength = chattingInfos.size();
        chattingInfos.clear();
        Cursor cursor = getInfo();
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex("fromAccount")).equals(myAccount)) {
                chattingInfos.add(
                        new ChattingInfo(
                                R.drawable.head,
                                cursor.getString(cursor.getColumnIndex("info")),
                                MsgType.OWN,
                                cursor.getString(cursor.getColumnIndex("date"))));
            }
            else {
                chattingInfos.add(
                        new ChattingInfo(
                                R.drawable.head,
                                cursor.getString(cursor.getColumnIndex("info")),
                                MsgType.OTHER,
                                cursor.getString(cursor.getColumnIndex("date"))));
            }
        }
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

    //按下返回键结束当前活动
    @Override
    public void onBackPressed() {
        finish();
    }

    private class UpdateMsg implements Runnable {

        @Override
        public void run() {
            try{
                while(true) {
                    handler.sendMessage(MyMessage.createMessage(UPDATE_CURRENT_MSG));
                    Thread.sleep(1000);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                new Thread(new UpdateMsg()).start();
            }
        }
    }

    private class SendNewMsgToServer implements Runnable {
        private String msg;
        private String currentTime;
        private JSONObject jsonObject;

        public SendNewMsgToServer(String msg, String currentTime) {
            this.msg = msg;
            this.currentTime = currentTime;
            jsonObject = new JSONObject();
        }

        @Override
        public void run() {
            try {
                jsonObject.put("op", "sendNewMsg");
                jsonObject.put("fromAccount", myAccount);
                jsonObject.put("toAccount", oppositeAccount);
                jsonObject.put("info", msg);
                jsonObject.put("date", currentTime);
                new SocketOperation(jsonObject).sendMsg();
            }
            catch (Exception e) {
                e.printStackTrace();
                new Thread(new SendNewMsgToServer(msg, currentTime)).start();
            }
        }
    }
}