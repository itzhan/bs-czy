package com.campus.zhihu.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.campus.zhihu.R;
import com.campus.zhihu.ZhihuApp;
import com.campus.zhihu.data.remote.ApiService;
import com.campus.zhihu.data.remote.RetrofitClient;
import com.campus.zhihu.ui.auth.AuthActivity;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        loadUserInfo(view);

        // 功能列表点击事件
        view.findViewById(R.id.btn_my_questions).setOnClickListener(v -> openList("questions", "我的提问"));
        view.findViewById(R.id.btn_my_answers).setOnClickListener(v -> openList("answers", "我的回答"));
        view.findViewById(R.id.btn_my_favorites).setOnClickListener(v -> openList("favorites", "我的收藏"));
        view.findViewById(R.id.btn_my_following).setOnClickListener(v -> openList("following", "我的关注"));

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            ZhihuApp.getInstance().logout();
            startActivity(new Intent(getActivity(), AuthActivity.class));
            if (getActivity() != null) getActivity().finish();
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) loadUserInfo(getView());
    }

    private void openList(String type, String title) {
        Intent intent = new Intent(getActivity(), UserListActivity.class);
        intent.putExtra(UserListActivity.EXTRA_TYPE, type);
        intent.putExtra(UserListActivity.EXTRA_TITLE, title);
        startActivity(intent);
    }

    private void loadUserInfo(View view) {
        ApiService api = RetrofitClient.createService(ApiService.class);
        api.getCurrentUser().enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().get("code").getAsInt() == 200) {
                    JsonObject user = response.body().getAsJsonObject("data");
                    setText(view, R.id.tv_nickname, getStr(user, "nickname"));
                    String bio = getStr(user, "bio");
                    setText(view, R.id.tv_bio, bio.isEmpty() ? "这个人很懒，什么都没写" : bio);
                    setText(view, R.id.tv_question_count, String.valueOf(getInt(user, "questionCount")));
                    setText(view, R.id.tv_answer_count, String.valueOf(getInt(user, "answerCount")));
                    setText(view, R.id.tv_follower_count, String.valueOf(getInt(user, "followerCount")));
                    setText(view, R.id.tv_like_count, String.valueOf(getInt(user, "likeCount")));
                }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setText(View root, int id, String text) {
        ((TextView) root.findViewById(id)).setText(text);
    }

    private String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private int getInt(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : 0;
    }
}
