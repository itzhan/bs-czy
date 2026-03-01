package com.campus.zhihu.ui.profile;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.campus.zhihu.R;
import com.campus.zhihu.ui.question.QuestionDetailActivity;
import com.google.gson.JsonObject;
import java.util.List;

/**
 * 通用列表项适配器 - 显示问题/回答标题和内容摘要，点击跳转详情
 */
public class SimpleItemAdapter extends RecyclerView.Adapter<SimpleItemAdapter.VH> {
    private final List<JsonObject> items;
    private final String type;

    public SimpleItemAdapter(List<JsonObject> items, String type) {
        this.items = items;
        this.type = type;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_list, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        JsonObject item = items.get(position);
        if ("answers".equals(type)) {
            String content = getStr(item, "content");
            holder.tvTitle.setText("回答 #" + (position + 1));
            holder.tvContent.setText(content.length() > 80 ? content.substring(0, 80) + "..." : content);
            // 回答点击 → 跳转到对应问题
            long questionId = item.has("questionId") && !item.get("questionId").isJsonNull() ? item.get("questionId").getAsLong() : -1;
            holder.itemView.setOnClickListener(v -> {
                if (questionId > 0) {
                    Intent intent = new Intent(v.getContext(), QuestionDetailActivity.class);
                    intent.putExtra("questionId", questionId);
                    v.getContext().startActivity(intent);
                }
            });
        } else {
            // questions / favorites → 直接用 id 跳转
            holder.tvTitle.setText(getStr(item, "title"));
            String content = getStr(item, "content");
            holder.tvContent.setText(content.length() > 60 ? content.substring(0, 60) + "..." : content);
            long questionId = item.has("id") && !item.get("id").isJsonNull() ? item.get("id").getAsLong() : -1;
            holder.itemView.setOnClickListener(v -> {
                if (questionId > 0) {
                    Intent intent = new Intent(v.getContext(), QuestionDetailActivity.class);
                    intent.putExtra("questionId", questionId);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override public int getItemCount() { return items.size(); }

    private String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;
        VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_title);
            tvContent = v.findViewById(R.id.tv_content);
        }
    }
}
