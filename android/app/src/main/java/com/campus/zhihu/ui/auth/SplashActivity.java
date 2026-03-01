package com.campus.zhihu.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.campus.zhihu.ZhihuApp;
import com.campus.zhihu.ui.home.MainActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (ZhihuApp.getInstance().isLoggedIn()) {
                intent = new Intent(this, MainActivity.class);
            } else {
                intent = new Intent(this, AuthActivity.class);
            }
            startActivity(intent);
            finish();
        }, 1500);
    }
}
