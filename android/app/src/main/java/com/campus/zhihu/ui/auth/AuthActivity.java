package com.campus.zhihu.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campus.zhihu.R;
import com.campus.zhihu.ZhihuApp;
import com.campus.zhihu.data.remote.ApiService;
import com.campus.zhihu.data.remote.RetrofitClient;
import com.campus.zhihu.ui.home.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends AppCompatActivity {
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin, btnGoRegister;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        api = RetrofitClient.createService(ApiService.class);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoRegister = findViewById(R.id.btn_go_register);

        btnLogin.setOnClickListener(v -> performLogin());
        btnGoRegister.setOnClickListener(v -> {
            // TODO: Navigate to register page
            Toast.makeText(this, "注册功能开发中", Toast.LENGTH_SHORT).show();
        });
    }

    private void performLogin() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        btnLogin.setEnabled(false);
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        api.login(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject res = response.body();
                    if (res.get("code").getAsInt() == 200) {
                        JsonObject data = res.getAsJsonObject("data");
                        String token = data.get("token").getAsString();
                        JsonObject user = data.getAsJsonObject("user");
                        ZhihuApp.getInstance().saveLoginInfo(token,
                                user.get("id").getAsLong(),
                                user.get("username").getAsString(),
                                user.get("nickname").getAsString(),
                                user.has("avatar") && !user.get("avatar").isJsonNull() ? user.get("avatar").getAsString() : "",
                                user.get("role").getAsInt());
                        startActivity(new Intent(AuthActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(AuthActivity.this, res.get("message").getAsString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AuthActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(AuthActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
