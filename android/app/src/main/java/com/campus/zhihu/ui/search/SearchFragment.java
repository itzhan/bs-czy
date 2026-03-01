package com.campus.zhihu.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campus.zhihu.R;
import com.campus.zhihu.adapter.QuestionAdapter;
import com.campus.zhihu.data.remote.ApiService;
import com.campus.zhihu.data.remote.RetrofitClient;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {
    private ApiService api;
    private QuestionAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        api = RetrofitClient.createService(ApiService.class);
        TextInputEditText etSearch = view.findViewById(R.id.et_search);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_tags);
        RecyclerView rv = view.findViewById(R.id.rv_results);

        adapter = new QuestionAdapter(q -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), com.campus.zhihu.ui.question.QuestionDetailActivity.class);
            intent.putExtra("questionId", q.get("id").getAsLong());
            startActivity(intent);
        });
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        // 加载热门标签
        api.getHotTags(10).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null && response.body().get("code").getAsInt() == 200) {
                    JsonArray tags = response.body().getAsJsonArray("data");
                    for (JsonElement e : tags) {
                        JsonObject tag = e.getAsJsonObject();
                        Chip chip = new Chip(requireContext());
                        chip.setText(tag.get("name").getAsString());
                        chip.setCheckable(true);
                        chip.setOnClickListener(v -> searchByTag(tag.get("id").getAsLong()));
                        chipGroup.addView(chip);
                    }
                }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
                if (!keyword.isEmpty()) searchByKeyword(keyword);
                return true;
            }
            return false;
        });
        return view;
    }

    private void searchByKeyword(String keyword) {
        api.searchQuestions(keyword, 1, 20).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null && response.body().get("code").getAsInt() == 200) {
                    adapter.setData(response.body().getAsJsonObject("data").getAsJsonArray("records"));
                }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }

    private void searchByTag(long tagId) {
        api.getTagQuestions(tagId, 1, 20).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null && response.body().get("code").getAsInt() == 200) {
                    adapter.setData(response.body().getAsJsonObject("data").getAsJsonArray("records"));
                }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }
}
