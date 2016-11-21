package com.codemine.talk2me;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    ArrayList<ChattingInfo> chattingInfos = new ArrayList<>();
    ListView chatList;
    EditText inputMsgText;
    Button sendMsgButton;
    TextView backText;
    TextView chattingWith;
    String myAccount;
    String oppositeAccount;
    JSONObject callbackJson = new JSONObject();
    MySQLiteOpenHelper mySQLiteOpenHelper;
    SQLiteDatabase sqLiteDatabase;


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
        initChattingInfo();

        backText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ChattingAdapter chattingAdapter = new ChattingAdapter(ChatActivity.this, R.layout.chatting_item, chattingInfos);
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
                    chattingInfos.add(new ChattingInfo(R.drawable.head, inputMsgText.getText().toString(), MsgType.OWN, ""));
                    ChattingAdapter chattingAdapter = new ChattingAdapter(ChatActivity.this, R.layout.chatting_item, chattingInfos);
                    chatList.setAdapter(chattingAdapter);
                    chatList.setSelection(chattingInfos.size() - 1);
                }
                inputMsgText.getText().clear();
            }
        });

        //新开线程循环接收消息
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    JSONObject jsonObject = new JSONObject();
//                    jsonObject.put("op", "getNewChattingMsg");
//                    jsonObject.put("myAccount", myAccount);
//                    jsonObject.put("oppositeAccount", oppositeAccount);
//                    callbackJson = new SocketOperation(jsonObject).getMsg();
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    public Cursor getInfo() {
        String sql = "SELECT * FROM CHAT_RECORD WHERE (fromAccount = ? AND toAccount = ?) OR (fromAccount = ? AND toAccount = ?);";
        return sqLiteDatabase.rawQuery(sql, new String[]{myAccount, oppositeAccount, oppositeAccount, myAccount});
    }

    public void initChattingInfo() {
        //todo 从数据库加载历史消息
        chattingInfos.clear();
//        String sql = "SELECT * FROM CHAT_RECORD WHERE fromAccount IN (?,?);";
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

    //按下返回键结束当前活动
    @Override
    public void onBackPressed() {
        finish();
    }
}