package com.campus.zhihu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.campus.zhihu.R;
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
            for (JsonElement e : data) {
                items.add(e.getAsJsonObject());
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = items.get(position);
        JsonObject question = item.has("question") ? item.getAsJsonObject("question") : item;
        holder.tvTitle.setText(question.has("title") ? question.get("title").getAsString() : "");
        String content = question.has("content") ? question.get("content").getAsString() : "";
        holder.tvContent.setText(content.length() > 100 ? content.substring(0, 100) + "..." : content);

        if (item.has("author") && !item.get("author").isJsonNull()) {
            JsonObject author = item.getAsJsonObject("author");
            holder.tvAuthor.setText(author.has("nickname") ? author.get("nickname").getAsString() : "匿名用户");
        } else {
            holder.tvAuthor.setText("匿名用户");
        }

        int answers = question.has("answerCount") ? question.get("answerCount").getAsInt() : 0;
        int likes = question.has("likeCount") ? question.get("likeCount").getAsInt() : 0;
        int views = question.has("viewCount") ? question.get("viewCount").getAsInt() : 0;
        holder.tvStats.setText(answers + "回答 · " + likes + "赞 · " + views + "浏览");

        holder.itemView.setOnClickListener(v -> listener.onItemClick(question));
    }

    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvAuthor, tvStats;
        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_title);
            tvContent = v.findViewById(R.id.tv_content);
            tvAuthor = v.findViewById(R.id.tv_author);
            tvStats = v.findViewById(R.id.tv_stats);
        }
    }
}
