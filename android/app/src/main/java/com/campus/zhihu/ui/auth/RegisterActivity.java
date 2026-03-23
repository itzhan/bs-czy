package com.campus.zhihu.ui.auth;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campus.zhihu.R;
import com.campus.zhihu.data.remote.ApiService;
import com.campus.zhihu.data.remote.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etUsername, etPassword, etConfirmPassword, etNickname, etEmail, etDepartment;
    private MaterialButton btnRegister, btnGoLogin;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        api = RetrofitClient.createService(ApiService.class);

        etUsername = findViewById(R.id.et_reg_username);
        etPassword = findViewById(R.id.et_reg_password);
        etConfirmPassword = findViewById(R.id.et_reg_confirm_password);
        etNickname = findViewById(R.id.et_reg_nickname);
        etEmail = findViewById(R.id.et_reg_email);
        etDepartment = findViewById(R.id.et_reg_department);
        btnRegister = findViewById(R.id.btn_register);
        btnGoLogin = findViewById(R.id.btn_go_login);

        btnRegister.setOnClickListener(v -> performRegister());
        btnGoLogin.setOnClickListener(v -> finish());
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void performRegister() {
        String username = getText(etUsername);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);
        String nickname = getText(etNickname);
        String email = getText(etEmail);
        String department = getText(etDepartment);

        // 表单验证
        if (username.isEmpty()) {
            etUsername.setError("请输入用户名");
            etUsername.requestFocus();
            return;
        }
        if (username.length() < 3 || username.length() > 50) {
            etUsername.setError("用户名长度需为3-50字符");
            etUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("请输入密码");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("密码至少6位");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("两次密码不一致");
            etConfirmPassword.requestFocus();
            return;
        }
        if (nickname.isEmpty()) {
            etNickname.setError("请输入昵称");
            etNickname.requestFocus();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("注册中...");

        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        body.put("nickname", nickname);
        body.put("role", 0); // 默认学生
        if (!email.isEmpty()) body.put("email", email);
        if (!department.isEmpty()) body.put("department", department);

        api.register(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText(R.string.register);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject res = response.body();
                    if (res.get("code").getAsInt() == 200) {
                        Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String msg = res.has("message") ? res.get("message").getAsString() : "注册失败";
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText(R.string.register);
                Toast.makeText(RegisterActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
