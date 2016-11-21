package com.codemine.talk2me;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class SocketOperation implements Runnable{

    private StringBuilder dealResult = new StringBuilder();
    private JSONObject inputJson;
    private JSONObject callBackJson;
    private Handler handler;
//    private String ip = "172.31.255.141";
    private String ip = "192.168.31.171";
//    private Socket socket = new Socket("192.168.31.132", 2333);
    private Message message = new Message();
//    private final int HIDE_PROGRESSBAR = 1;
//    private final int SHOW_PROGRESSBAR = 2;
//    private final int ERROR_CONNECT = 3;//无法连接服务器
//    private final int ERROR_PASSWORD = 4;//密码错误
//    private final int ERROR_ACCOUNT = 5;//用户名不存在

    public SocketOperation(JSONObject jsonObject, StringBuilder dealResult) throws IOException {
        this.inputJson = jsonObject;
//        this.dealResult = dealResult;
    }

    public SocketOperation(JSONObject jsonObject, JSONObject callBackJson) throws IOException {
        this.inputJson = jsonObject;
        this.callBackJson = callBackJson;
    }

    public SocketOperation(JSONObject inputJson) throws IOException {
        this.inputJson = inputJson;
    }

    public SocketOperation(JSONObject jsonObject, StringBuilder dealResult, Handler handler) throws IOException {
        this.inputJson = jsonObject;
//        this.dealResult = dealResult;
        this.handler = handler;
    }

    public SocketOperation(JSONObject jsonObject, Handler handler) {
        this.inputJson = jsonObject;
        this.handler = handler;
    }

    public void setDealResult(StringBuilder dealResult) {
        this.dealResult = dealResult;
    }

    public void setInputJson(JSONObject inputJson) {
        this.inputJson = inputJson;
    }

    public void setCallBackJson(JSONObject callBackJson) {
        this.callBackJson = callBackJson;
    }

    public boolean sendMsg() throws IOException {
        Socket socket = new Socket(ip, 2333);
        BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bfw.write(inputJson.toString() + "\n");
        bfw.flush();
        dealResult.append(bfr.readLine());
        if(dealResult.toString().equals("login success")) {
            handler.sendMessage(MyMessage.createMessage(MESSAGE.LOGIN_SUCCESS));
            return true;
        }
        else if(dealResult.toString().equals("account not exist")) {
            handler.sendMessage(MyMessage.createMessage(MESSAGE.ERROR_ACCOUNT));
            return true;
        }
        else if(dealResult.toString().equals("error password")) {
            handler.sendMessage(MyMessage.createMessage(MESSAGE.ERROR_PASSWORD));
            return true;
        }
        else if(dealResult.toString().equals("register success")) {
            handler.sendMessage(MyMessage.createMessage(MESSAGE.REGISTER_SUCCESS));
            return true;
        }
        else if(dealResult.toString().equals("account already exist")) {
            handler.sendMessage(MyMessage.createMessage(MESSAGE.ACCOUNT_ALREADY_EXIST));
            return true;
        }
        else {
            return false;
        }
    }

    //从服务器获取消息
    public JSONObject getMsg() throws IOException, JSONException {
        Socket socket = new Socket(ip, 2333);
//        String ipAddress = socket.getInetAddress().toString();
        BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bfw.write(inputJson.toString() + "\n");
        bfw.flush();
//        callBackJson = new JSONObject(bfr.readLine());
        return new JSONObject(bfr.readLine());
    }

    @Override
    public void run() {
        try {
            boolean canConnect = InetAddress.getByName(ip).isReachable(5000);
            if(canConnect) {
                sendMsg();
            }
            else {
                handler.removeMessages(MESSAGE.SHOW_PROGRESSBAR);
                if(dealResult.toString().equals("")) {
                    message.what = MESSAGE.ERROR_CONNECT;
                    handler.sendMessage(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
