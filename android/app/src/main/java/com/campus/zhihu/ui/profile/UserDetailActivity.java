package com.campus.zhihu.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.campus.zhihu.R;
import com.campus.zhihu.ZhihuApp;
import com.campus.zhihu.data.remote.ApiService;
import com.campus.zhihu.data.remote.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 用户详情页 —— 查看他人主页、关注/取消关注
 */
public class UserDetailActivity extends AppCompatActivity {
    public static final String EXTRA_USER_ID = "userId";

    private long userId;
    private ApiService api;
    private MaterialButton btnFollow;
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
        if (userId == -1) { finish(); return; }

        setContentView(R.layout.activity_user_detail);
        api = RetrofitClient.createService(ApiService.class);

        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnFollow = findViewById(R.id.btn_follow);

        // 如果是自己，隐藏关注按钮
        long myId = ZhihuApp.getInstance().getUserId();
        if (userId == myId) {
            btnFollow.setVisibility(View.GONE);
        } else {
            // 关注/取消关注
            btnFollow.setOnClickListener(v -> toggleFollow());
            loadFollowStatus();
        }

        // 功能列表 — TA的提问/回答
        findViewById(R.id.btn_user_questions).setOnClickListener(v -> openList("questions", "TA的提问"));
        findViewById(R.id.btn_user_answers).setOnClickListener(v -> openList("answers", "TA的回答"));

        // 粉丝数可点击查看粉丝列表
        findViewById(R.id.btn_followers).setOnClickListener(v -> openList("followers", "粉丝列表"));

        loadUserInfo();
    }

    private void loadUserInfo() {
        api.getUser(userId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null && response.body().get("code").getAsInt() == 200) {
                    JsonObject user = response.body().getAsJsonObject("data");
                    setText(R.id.tv_nickname, getStr(user, "nickname"));
                    String bio = getStr(user, "bio");
                    setText(R.id.tv_bio, bio.isEmpty() ? "这个人很懒，什么都没写" : bio);
                    setText(R.id.tv_question_count, String.valueOf(getInt(user, "questionCount")));
                    setText(R.id.tv_answer_count, String.valueOf(getInt(user, "answerCount")));
                    setText(R.id.tv_follower_count, String.valueOf(getInt(user, "followerCount")));
                    setText(R.id.tv_like_count, String.valueOf(getInt(user, "likeCount")));
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (!isFinishing()) Toast.makeText(UserDetailActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFollowStatus() {
        api.isFollowing(userId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null && response.body().get("code").getAsInt() == 200) {
                    isFollowing = response.body().get("data").getAsBoolean();
                    updateFollowButton();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }

    private void toggleFollow() {
        btnFollow.setEnabled(false);
        api.toggleFollow(userId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                btnFollow.setEnabled(true);
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null && response.body().get("code").getAsInt() == 200) {
                    isFollowing = !isFollowing;
                    updateFollowButton();
                    loadUserInfo(); // 刷新粉丝数
                    Toast.makeText(UserDetailActivity.this, isFollowing ? "关注成功" : "已取消关注", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                btnFollow.setEnabled(true);
                Toast.makeText(UserDetailActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFollowButton() {
        if (isFollowing) {
            btnFollow.setText("已关注");
            btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE8E8E8));
            btnFollow.setTextColor(0xFF666666);
        } else {
            btnFollow.setText("关注");
            btnFollow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary, getTheme())));
            btnFollow.setTextColor(0xFFFFFFFF);
        }
    }

    private void openList(String type, String title) {
        Intent intent = new Intent(this, UserListActivity.class);
        intent.putExtra(UserListActivity.EXTRA_TYPE, type);
        intent.putExtra(UserListActivity.EXTRA_TITLE, title);
        intent.putExtra(UserListActivity.EXTRA_USER_ID, userId);
        startActivity(intent);
    }

    private void setText(int id, String text) {
        ((TextView) findViewById(id)).setText(text);
    }

    private String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private int getInt(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : 0;
    }
}
