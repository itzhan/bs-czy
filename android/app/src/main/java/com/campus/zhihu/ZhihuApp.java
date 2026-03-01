package com.campus.zhihu;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 应用入口类
 * 管理全局配置、SharedPreferences(Token存储)
 */
public class ZhihuApp extends Application {

    private static ZhihuApp instance;
    private SharedPreferences prefs;

    public static final String BASE_URL = "http://10.0.2.2:8080/"; // 模拟器访问宿主机
    public static final String PREF_NAME = "campus_zhihu_prefs";
    public static final String KEY_TOKEN = "jwt_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_NICKNAME = "nickname";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_ROLE = "role";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static ZhihuApp getInstance() { return instance; }

    public String getToken() { return prefs.getString(KEY_TOKEN, ""); }

    public void saveLoginInfo(String token, long userId, String username, String nickname, String avatar, int role) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_NICKNAME, nickname)
                .putString(KEY_AVATAR, avatar)
                .putInt(KEY_ROLE, role)
                .apply();
    }

    public long getUserId() { return prefs.getLong(KEY_USER_ID, -1); }
    public String getUsername() { return prefs.getString(KEY_USERNAME, ""); }
    public String getNickname() { return prefs.getString(KEY_NICKNAME, ""); }
    public int getRole() { return prefs.getInt(KEY_ROLE, 0); }

    public boolean isLoggedIn() { return !getToken().isEmpty(); }

    public void logout() { prefs.edit().clear().apply(); }
}
