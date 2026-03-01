package com.campus.zhihu.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.campus.zhihu.R;
import com.campus.zhihu.adapter.QuestionAdapter;
import com.campus.zhihu.data.remote.ApiService;
import com.campus.zhihu.data.remote.RetrofitClient;
import com.campus.zhihu.ui.question.QuestionDetailActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private RecyclerView rvQuestions;
    private SwipeRefreshLayout swipeRefresh;
    private QuestionAdapter adapter;
    private ApiService api;
    private String currentTab = "recommend"; // recommend, latest, following

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        api = RetrofitClient.createService(ApiService.class);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        rvQuestions = view.findViewById(R.id.rv_questions);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        tabLayout.addTab(tabLayout.newTab().setText("推荐"));
        tabLayout.addTab(tabLayout.newTab().setText("最新"));
        tabLayout.addTab(tabLayout.newTab().setText("关注"));

        adapter = new QuestionAdapter(q -> {
            Intent intent = new Intent(getActivity(), QuestionDetailActivity.class);
            intent.putExtra("questionId", q.get("id").getAsLong());
            startActivity(intent);
        });
        rvQuestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvQuestions.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentTab = "recommend"; break;
                    case 1: currentTab = "latest"; break;
                    case 2: currentTab = "following"; break;
                }
                loadData();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();
        return view;
    }

    private void loadData() {
        swipeRefresh.setRefreshing(true);
        Call<JsonObject> call;
        switch (currentTab) {
            case "following": call = api.getFollowingQuestions(1, 20); break;
            case "latest": call = api.getQuestions(1, 20, null, null, "latest"); break;
            default: call = api.getRecommend(1, 20); break;
        }
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject res = response.body();
                    if (res.get("code").getAsInt() == 200 && res.has("data") && !res.get("data").isJsonNull()) {
                        JsonObject data = res.getAsJsonObject("data");
                        JsonArray records = data.getAsJsonArray("records");
                        adapter.setData(records);
                    }
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                if (getContext() != null) Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
