package com.campus.zhihu.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campus.zhihu.R;
import com.campus.zhihu.data.remote.ApiService;
import com.campus.zhihu.data.remote.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 我的举报记录页面
 */
public class MyReportsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list); // 复用通用列表布局

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("我的举报");
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rv_list);
        TextView tvEmpty = findViewById(R.id.tv_empty);
        rv.setLayoutManager(new LinearLayoutManager(this));

        ApiService api = RetrofitClient.createService(ApiService.class);
        api.getMyReports(1, 100).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null && response.body().get("code").getAsInt() == 200) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    JsonArray records = data.getAsJsonArray("records");
                    if (records == null || records.size() == 0) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("暂无举报记录");
                        rv.setVisibility(View.GONE);
                        return;
                    }
                    List<JsonObject> items = new ArrayList<>();
                    for (JsonElement e : records) items.add(e.getAsJsonObject());
                    rv.setAdapter(new ReportAdapter(items));
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (!isFinishing()) {
                    tvEmpty.setText("加载失败");
                    tvEmpty.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                }
            }
        });
    }
}
