package com.codemine.talk2me;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.logging.LogRecord;

public class LoginActivity extends AppCompatActivity {
    final int HIDE_PROGRESSBAR = 1;
    final int SHOW_PROGRESSBAR = 2;
    final int ERROR_CONNECT = 3;//无法连接服务器
    final int ERROR_PASSWORD = 4;//密码错误
    final int ERROR_ACCOUNT = 5;//用户名不存在
    Message hideProgressBarMessage = new Message();
    Message showProgressBarMessage = new Message();

    public static StringBuilder dealResult = new StringBuilder();
    LinearLayout progressLayout;
    LinearLayout logInLayout;
    LinearLayout registerLayout;
    LinearLayout logLayout;
    Button loginButton;
    Button registerButton;
    Button loggingButton;
    EditText accountEdit;
    EditText passwordEdit;
    EditText registerAccountEdit;
    EditText registerPasswordEdit;
    EditText registerConfirmPasswordEdit;
    EditText registerEmailEdit;
    Button registerRegisterButton;
    Button registerBackButton;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_PROGRESSBAR:
                    progressLayout.setVisibility(View.GONE);
                    handler.removeMessages(HIDE_PROGRESSBAR);
                    break;
                case SHOW_PROGRESSBAR:
                    progressLayout.setVisibility(View.VISIBLE);
                    handler.removeMessages(SHOW_PROGRESSBAR);
                    break;
                case ERROR_CONNECT:
                    progressLayout.setVisibility(View.GONE);
                    alert("与服务器断开连接，请重试");
                    handler.removeMessages(ERROR_CONNECT);
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
        setContentView(R.layout.activity_login);


        checkLogin();
        hideProgressBarMessage.what = HIDE_PROGRESSBAR;
        showProgressBarMessage.what = SHOW_PROGRESSBAR;

        logInLayout = (LinearLayout) findViewById(R.id.login_layout);
        registerLayout = (LinearLayout) findViewById(R.id.register_layout);
        logLayout = (LinearLayout) findViewById(R.id.log_layout);
        loginButton = (Button) findViewById(R.id.login_button);
        registerButton = (Button) findViewById(R.id.register_button);
        loggingButton = (Button) findViewById(R.id.logging_button);
        accountEdit = (EditText) findViewById(R.id.account_edit);
        passwordEdit = (EditText) findViewById(R.id.password_edit);
        registerAccountEdit = (EditText) findViewById(R.id.register_account_edit);
        registerPasswordEdit = (EditText) findViewById(R.id.register_password_edit);
        registerConfirmPasswordEdit = (EditText) findViewById(R.id.register_confirm_password_edit);
        registerEmailEdit = (EditText) findViewById(R.id.register_email_edit);
        registerRegisterButton = (Button) findViewById(R.id.register_register_button);
        registerBackButton = (Button) findViewById(R.id.register_back_button);
        progressLayout = (LinearLayout) findViewById(R.id.progress_layout);//进度条
        progressLayout.setVisibility(View.GONE);

        final int passwordInputType = registerPasswordEdit.getInputType();

        //登陆界面
//        progressLayout.setVisibility(View.VISIBLE);
//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                progressLayout.setVisibility(View.VISIBLE);
//            }
//        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message();
                message.what = SHOW_PROGRESSBAR;
                handler.sendMessage(message);
//                loginButton.setBackgroundColor(Color.rgb(255, 140, 0));
//                loggingButton.setVisibility(View.VISIBLE);
//                logLayout.setVisibility(View.GONE);
//                progressLayout.setVisibility(View.VISIBLE);//显示进度条
                final String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                dealResult.delete(0, dealResult.length());
                HashMap<String, String> infoMap = new HashMap<>();
                infoMap.put("op", "login");
                infoMap.put("account", account);
                infoMap.put("password", password);
                JSONObject jsonObject = new JSONObject(infoMap);
                try {
                    new Thread(new SocketOperation(jsonObject, dealResult, handler)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                while (dealResult.toString().equals(""));

//                long startTime = System.currentTimeMillis();
//                while (true) {
//                    long nowTime = System.currentTimeMillis();
//                    if(dealResult.toString().equals("")) {
//                        if(nowTime - startTime < 5 * 1000)
//                            continue;
//                        alert("与服务器断开连接，请重试");
//                        break;
//                    }
//                    else if (dealResult.toString().equals("login success")) {
//                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                        intent.putExtra("account", accountEdit.getText().toString());
//                        startActivity(intent);
//                        finish();
//                    } else if (dealResult.toString().equals("error password")) {
//                        alert("密码错误");
//                    } else if (dealResult.toString().equals("error account")) {
//                        alert("账号不存在");
//                    } else {
//                        alert("系统错误，请重试");
//                    }


//                    if(!dealResult.toString().equals("")) {
//                        progressLayout.setVisibility(View.GONE);
//                        break;
//                    }
//                }
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                registerButton.setBackgroundColor(Color.rgb(0, 100, 0));
//                registerBackButton.setBackgroundColor(Color.rgb(255, 193, 37));
//                registerRegisterButton.setBackgroundColor(Color.rgb(78, 238, 148));
                logInLayout.setVisibility(View.GONE);
                registerLayout.setVisibility(View.VISIBLE);
            }
        });



        //注册界面

        registerAccountEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(registerAccountEdit.getText().toString().equals("账号不能为空")
                        || registerAccountEdit.getText().toString().equals("账号已存在")) {
                    registerAccountEdit.getText().clear();
                    registerAccountEdit.setTextColor(Color.BLACK);
                }
            }
        });

        registerPasswordEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(registerPasswordEdit.getText().toString().equals("密码不能为空")) {
                    registerPasswordEdit.getText().clear();
                    registerPasswordEdit.setTextColor(Color.BLACK);
                }
                registerPasswordEdit.setInputType(passwordInputType);
            }
        });

        registerConfirmPasswordEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(registerConfirmPasswordEdit.getText().toString().equals("确认密码不能为空")
                        || registerConfirmPasswordEdit.getText().toString().equals("两次密码输入需要相同")) {
                    registerConfirmPasswordEdit.getText().clear();
                    registerConfirmPasswordEdit.setTextColor(Color.BLACK);
                }
                registerConfirmPasswordEdit.setInputType(passwordInputType);
            }
        });

        registerEmailEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(registerEmailEdit.getText().toString().equals("邮箱地址不能为空")) {
                    registerEmailEdit.getText().clear();
                    registerEmailEdit.setTextColor(Color.BLACK);
                }
            }
        });

        registerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                loginButton.setBackgroundColor(Color.rgb(255, 193, 37));
//                registerButton.setBackgroundColor(Color.rgb(78, 238, 148));
                logInLayout.setVisibility(View.VISIBLE);
                registerLayout.setVisibility(View.GONE);
            }
        });

        registerRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String account = registerAccountEdit.getText().toString();
                String password = registerPasswordEdit.getText().toString();
                String confirmPassword = registerConfirmPasswordEdit.getText().toString();
                String emailAddress = registerEmailEdit.getText().toString();

                if(account.equals("")) {
                    registerAccountEdit.setText("账号不能为空");
                    registerAccountEdit.setTextColor(Color.RED);
                    return;
                }

                if(password.equals("")) {
                    registerPasswordEdit.setInputType(InputType.TYPE_CLASS_TEXT);
                    registerPasswordEdit.setText("密码不能为空");
                    registerPasswordEdit.setTextColor(Color.RED);
                    return;
                }

                if(emailAddress.equals("")) {
                    registerEmailEdit.setText("邮箱地址不能为空");
                    registerEmailEdit.setTextColor(Color.RED);
                    return;
                }

                if(confirmPassword.equals("")) {
                    registerConfirmPasswordEdit.setInputType(InputType.TYPE_CLASS_TEXT);
                    registerConfirmPasswordEdit.setText("确认密码不能为空");
                    registerConfirmPasswordEdit.setTextColor(Color.RED);
                    return;
                }

                if(!password.equals(confirmPassword)) {
                    registerConfirmPasswordEdit.setInputType(InputType.TYPE_CLASS_TEXT);
                    registerConfirmPasswordEdit.setText("两次密码输入需要相同");
                    registerConfirmPasswordEdit.setTextColor(Color.RED);
                    return;
                }

                handler.sendMessage(MyMessage.createMessage(SHOW_PROGRESSBAR));

                dealResult = dealResult.delete(0, dealResult.length());
                Map<String, String> map = new HashMap<>();
                map.put("op", "register");
                map.put("account", account);
                map.put("password", password);
                map.put("emailAddress", emailAddress);
                JSONObject jsonObject = new JSONObject(map);
                try {
                    new Thread(new SocketOperation(jsonObject, dealResult, handler)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                registerRegisterButton.setClickable(false);
//                registerRegisterButton.setBackgroundColor(Color.rgb(220, 220, 220));

//                long startTime = System.currentTimeMillis();
//                while(dealResult.toString().equals("")) {
//                    long nowTime = System.currentTimeMillis();
//                    if(nowTime - startTime > 5 * 1000) {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//                        builder.setMessage("与服务器断开连接，请重试");
//                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        });
//                        builder.create().show();
//                        break;
//                    }
//                }

//                long startTime = System.currentTimeMillis();
//                while(true) {
//                    long nowTime = System.currentTimeMillis();
//                    if(dealResult.toString().equals("")) {
//                        if(nowTime - startTime > 5 * 1000)
//                            continue;
//                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//                        builder.setMessage("与服务器断开连接，请重试");
//                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        });
//                        builder.create().show();
//                        registerRegisterButton.setClickable(true);
//                        registerRegisterButton.setBackgroundColor(Color.rgb(78, 238, 148));
//                        break;
//                    }
//                    else if (dealResult.toString().equals("account already exist")) {
//                        registerAccountEdit.setText("账号已存在");
//                        registerAccountEdit.setTextColor(Color.RED);
//                    }
//                    else if (dealResult.toString().equals("register success")) {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//                        builder.setMessage("注册成功，请登录");
//                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                                registerAccountEdit.getText().clear();
//                                registerPasswordEdit.getText().clear();
//                                registerConfirmPasswordEdit.getText().clear();
//                                registerEmailEdit.getText().clear();
//                                registerLayout.setVisibility(View.GONE);
//                                logInLayout.setVisibility(View.VISIBLE);
//                            }
//                        });
//                        builder.create().show();
//                    } else {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//                        builder.setMessage("注册失败，请重试");
//                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        });
//                        builder.create().show();
//
//                    }

//                    if(!dealResult.toString().equals("")) {
//                        registerRegisterButton.setClickable(true);
//                        registerRegisterButton.setBackgroundColor(Color.rgb(78, 238, 148));
//                        break;
//                    }
//                }
            }
        });
    }

    public void checkLogin() {

    }

    private void alert(String info) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(info);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
