package com.campus.zhihu.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.campus.zhihu.R;
import com.campus.zhihu.data.remote.ApiService;
import com.campus.zhihu.data.remote.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {
    private RecyclerView rv;
    private View tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton btnMarkAll;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_new, container, false);
        rv = view.findViewById(R.id.rv_notifications);
        tvEmpty = view.findViewById(R.id.tv_empty);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        btnMarkAll = view.findViewById(R.id.btn_mark_all_read);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadNotifications);

        btnMarkAll.setOnClickListener(v -> markAllRead());
        loadNotifications();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        ApiService api = RetrofitClient.createService(ApiService.class);
        api.getNotifications(1, 50).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().get("code").getAsInt() == 200) {
                    JsonArray records = response.body().getAsJsonObject("data").getAsJsonArray("records");
                    if (records == null || records.size() == 0) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                        btnMarkAll.setVisibility(View.GONE);
                        return;
                    }
                    tvEmpty.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                    btnMarkAll.setVisibility(View.VISIBLE);
                    List<JsonObject> items = new ArrayList<>();
                    for (JsonElement e : records) items.add(e.getAsJsonObject());
                    rv.setAdapter(new NotificationAdapter(items));
                }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isAdded()) swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void markAllRead() {
        ApiService api = RetrofitClient.createService(ApiService.class);
        api.markNotificationRead(null).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> c, Response<JsonObject> r) { loadNotifications(); }
            @Override public void onFailure(Call<JsonObject> c, Throwable t) {}
        });
    }

    // --- 内部适配器 ---
    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
        private final List<JsonObject> items;
        NotificationAdapter(List<JsonObject> items) { this.items = items; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            JsonObject n = items.get(position);
            String title = getStr(n, "title");
            String content = getStr(n, "content");
            String time = getStr(n, "createdAt");
            int isRead = n.has("isRead") && !n.get("isRead").isJsonNull() ? n.get("isRead").getAsInt() : 0;
            int type = n.has("type") && !n.get("type").isJsonNull() ? n.get("type").getAsInt() : 0;

            holder.tvTitle.setText(title.isEmpty() ? "系统通知" : title);
            holder.tvContent.setText(content);
            // 格式化时间：截取日期部分
            if (time.length() > 10) time = time.substring(0, 10);
            holder.tvTime.setText(time);
            // 未读圆点
            holder.vDot.setVisibility(isRead == 0 ? View.VISIBLE : View.GONE);
            // 未读加粗
            holder.tvTitle.setTypeface(null, isRead == 0 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            // 根据类型设置图标
            int iconRes;
            switch (type) {
                case 1: iconRes = android.R.drawable.ic_dialog_info; break;       // 新回答
                case 2: iconRes = android.R.drawable.ic_dialog_email; break;      // 评论
                case 3: iconRes = android.R.drawable.btn_star_big_on; break;      // 点赞
                case 4: iconRes = android.R.drawable.ic_menu_myplaces; break;     // 关注
                case 5: iconRes = android.R.drawable.ic_menu_info_details; break; // 采纳
                default: iconRes = android.R.drawable.ic_dialog_info; break;
            }
            holder.ivIcon.setImageResource(iconRes);
        }

        @Override public int getItemCount() { return items.size(); }

        private static String getStr(JsonObject obj, String key) {
            return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvContent, tvTime;
            ImageView ivIcon;
            View vDot;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_title);
                tvContent = v.findViewById(R.id.tv_content);
                tvTime = v.findViewById(R.id.tv_time);
                ivIcon = v.findViewById(R.id.iv_type_icon);
                vDot = v.findViewById(R.id.v_unread_dot);
            }
        }
    }
}
