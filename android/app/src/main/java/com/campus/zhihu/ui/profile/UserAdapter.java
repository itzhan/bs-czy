package com.campus.zhihu.ui.profile;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.campus.zhihu.R;
import com.google.gson.JsonObject;
import java.util.List;

/**
 * 用户列表适配器 - 用于关注/粉丝列表，可点击跳转用户详情
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {
    private final List<JsonObject> users;

    public UserAdapter(List<JsonObject> users) { this.users = users; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_list, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        JsonObject user = users.get(position);
        String nickname = user.has("nickname") && !user.get("nickname").isJsonNull() ? user.get("nickname").getAsString() : "未知用户";
        String dept = user.has("department") && !user.get("department").isJsonNull() ? user.get("department").getAsString() : "";
        holder.tvTitle.setText(nickname);
        holder.tvContent.setText(dept);

        // 点击跳转到用户详情页
        long userId = user.has("id") && !user.get("id").isJsonNull() ? user.get("id").getAsLong() : -1;
        if (userId != -1) {
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), UserDetailActivity.class);
                intent.putExtra(UserDetailActivity.EXTRA_USER_ID, userId);
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override public int getItemCount() { return users.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;
        VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_title);
            tvContent = v.findViewById(R.id.tv_content);
        }
    }
}
