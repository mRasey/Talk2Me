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
}
