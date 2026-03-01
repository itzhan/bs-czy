package com.campus.zhihu.ui.question;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campus.zhihu.data.remote.ApiService;
import com.campus.zhihu.data.remote.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import android.widget.LinearLayout;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AskQuestionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        MaterialToolbar toolbar = new MaterialToolbar(this);
        toolbar.setTitle("发布问题");
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> finish());
        layout.addView(toolbar);

        TextInputLayout titleLayout = new TextInputLayout(this, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        TextInputEditText etTitle = new TextInputEditText(titleLayout.getContext());
        etTitle.setHint("问题标题");
        titleLayout.addView(etTitle);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = 24;
        layout.addView(titleLayout, params);

        TextInputLayout contentLayout = new TextInputLayout(this, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        TextInputEditText etContent = new TextInputEditText(contentLayout.getContext());
        etContent.setHint("问题内容");
        etContent.setMinLines(5);
        contentLayout.addView(etContent);
        params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = 16;
        layout.addView(contentLayout, params);

        MaterialButton btnSubmit = new MaterialButton(this);
        btnSubmit.setText("发布");
        params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = 24;
        layout.addView(btnSubmit, params);

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String content = etContent.getText() != null ? etContent.getText().toString().trim() : "";
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "标题和内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("title", title);
            body.put("content", content);
            body.put("status", 1);
            RetrofitClient.createService(ApiService.class).createQuestion(body).enqueue(new Callback<JsonObject>() {
                @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().get("code").getAsInt() == 200) {
                        Toast.makeText(AskQuestionActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                @Override public void onFailure(Call<JsonObject> call, Throwable t) { Toast.makeText(AskQuestionActivity.this, "发布失败", Toast.LENGTH_SHORT).show(); }
            });
        });

        setContentView(layout);
    }
}
