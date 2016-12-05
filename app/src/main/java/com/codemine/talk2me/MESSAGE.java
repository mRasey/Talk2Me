package com.codemine.talk2me;

/**
 * Created by billy on 2016/11/19.
 */

public class MESSAGE {
    final static int HIDE_PROGRESSBAR = 1;
    final static int SHOW_PROGRESSBAR = 2;
    final static int ERROR_CONNECT = 3;//无法连接服务器
    final static int ERROR_PASSWORD = 4;//密码错误
    final static int ERROR_ACCOUNT = 5;//用户名不存在
    final static int LOGIN_SUCCESS = 6;//登录成功
    final static int REGISTER_SUCCESS = 7;//注册成功
    final static int ACCOUNT_ALREADY_EXIST = 8;//用户名已存在
    final static int GET_FRIENDS_FROM_SERVER = 9;//从服务器获取好友
    final static int GET_NEW_MSG = 10;//从服务器获取新的消息
    final static int SHAKE_NEW_FRIEND = 11;//通过摇晃手机获得一个新的朋友
    final static int UPDATE_CURRENT_MSG = 12;//更新当前聊天界面的消息
}
