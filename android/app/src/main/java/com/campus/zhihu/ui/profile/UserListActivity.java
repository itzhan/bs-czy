package com.campus.zhihu.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campus.zhihu.R;
import com.campus.zhihu.ZhihuApp;
import com.campus.zhihu.data.remote.ApiService;
import com.campus.zhihu.data.remote.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用列表页：我的提问、我的回答、我的收藏、我的关注
 */
public class UserListActivity extends AppCompatActivity {
    public static final String EXTRA_TYPE = "type"; // questions, answers, favorites, following, followers
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        RecyclerView rv = findViewById(R.id.rv_list);
        TextView tvEmpty = findViewById(R.id.tv_empty);

        String type = getIntent().getStringExtra(EXTRA_TYPE);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        toolbar.setTitle(title != null ? title : "列表");
        toolbar.setNavigationOnClickListener(v -> finish());

        rv.setLayoutManager(new LinearLayoutManager(this));

        // 支持传入其他用户的 ID，未传入则使用当前登录用户
        long userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
        if (userId == -1) userId = ZhihuApp.getInstance().getUserId();
        ApiService api = RetrofitClient.createService(ApiService.class);

        Call<JsonObject> call;
        switch (type != null ? type : "") {
            case "questions": call = api.getUserQuestions(userId, 1, 100); break;
            case "answers": call = api.getUserAnswers(userId, 1, 100); break;
            case "favorites": call = api.getUserFavorites(userId, 1, 100); break;
            case "following": call = api.getFollowing(userId, 1, 100); break;
            case "followers": call = api.getFollowers(userId, 1, 100); break;
            default: finish(); return;
        }

        call.enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> c, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null && response.body().get("code").getAsInt() == 200) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    JsonArray records = data.getAsJsonArray("records");
                    if (records == null || records.size() == 0) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                        return;
                    }
                    if ("following".equals(type) || "followers".equals(type)) {
                        // 关注/粉丝列表显示用户
                        List<JsonObject> users = new ArrayList<>();
                        for (JsonElement e : records) users.add(e.getAsJsonObject());
                        rv.setAdapter(new UserAdapter(users));
                    } else {
                        // 提问/回答/收藏显示问题列表
                        List<JsonObject> items = new ArrayList<>();
                        for (JsonElement e : records) {
                            JsonObject item = e.getAsJsonObject();
                            items.add(item);
                        }
                        rv.setAdapter(new SimpleItemAdapter(items, type));
                    }
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                }
            }
            @Override public void onFailure(Call<JsonObject> c, Throwable t) {
                tvEmpty.setText("加载失败");
                tvEmpty.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            }
        });
    }
}
