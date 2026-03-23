package com.campus.zhihu.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.campus.zhihu.R;
import com.campus.zhihu.ui.profile.UserDetailActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {
    private final List<JsonObject> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(JsonObject question);
    }

    public QuestionAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(JsonArray data) {
        items.clear();
        if (data != null) {
            for (JsonElement e : data) items.add(e.getAsJsonObject());
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        JsonObject item = items.get(position);
        JsonObject question = item.has("question") ? item.getAsJsonObject("question") : item;

        h.tvTitle.setText(getStr(question, "title"));
        String content = getStr(question, "content");
        h.tvContent.setText(content.length() > 100 ? content.substring(0, 100) + "..." : content);

        // 作者
        if (item.has("author") && !item.get("author").isJsonNull()) {
            JsonObject author = item.getAsJsonObject("author");
            h.tvAuthor.setText(getStr(author, "nickname"));
            // 作者头像和名称可点击跳转到用户详情页
            long authorId = author.has("id") && !author.get("id").isJsonNull() ? author.get("id").getAsLong() : -1;
            if (authorId != -1) {
                View.OnClickListener authorClick = v -> {
                    Intent intent = new Intent(v.getContext(), UserDetailActivity.class);
                    intent.putExtra(UserDetailActivity.EXTRA_USER_ID, authorId);
                    v.getContext().startActivity(intent);
                };
                h.tvAuthor.setOnClickListener(authorClick);
                if (h.ivAvatar != null) h.ivAvatar.setOnClickListener(authorClick);
            }
        } else {
            h.tvAuthor.setText("匿名用户");
        }

        // 时间
        String time = getStr(question, "createdAt");
        if (time.length() > 10) time = time.substring(0, 10);
        if (h.tvTime != null) h.tvTime.setText(time);

        // 统计
        int answers = getInt(question, "answerCount");
        int likes = getInt(question, "likeCount");
        int views = getInt(question, "viewCount");
        h.tvStats.setText(answers + " 回答 · " + likes + " 赞 · " + views + " 浏览");

        // 标签（取第一个）
        if (h.tvTag != null && item.has("tags") && item.get("tags").isJsonArray()) {
            JsonArray tags = item.getAsJsonArray("tags");
            if (tags.size() > 0) {
                h.tvTag.setText(getStr(tags.get(0).getAsJsonObject(), "name"));
                h.tvTag.setVisibility(View.VISIBLE);
            } else {
                h.tvTag.setVisibility(View.GONE);
            }
        }

        h.itemView.setOnClickListener(v -> listener.onItemClick(question));
    }

    @Override public int getItemCount() { return items.size(); }

    private String getStr(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
    }
    private int getInt(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvAuthor, tvStats, tvTime, tvTag;
        ImageView ivAvatar;
        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_title);
            tvContent = v.findViewById(R.id.tv_content);
            tvAuthor = v.findViewById(R.id.tv_author);
            tvStats = v.findViewById(R.id.tv_stats);
            tvTime = v.findViewById(R.id.tv_time);
            tvTag = v.findViewById(R.id.tv_tag);
            ivAvatar = v.findViewById(R.id.iv_avatar);
        }
    }
}
