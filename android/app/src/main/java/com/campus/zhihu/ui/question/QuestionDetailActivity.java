package com.campus.zhihu.ui.question;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campus.zhihu.R;
import com.campus.zhihu.data.remote.ApiService;
import com.campus.zhihu.data.remote.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionDetailActivity extends AppCompatActivity {
    private long questionId;
    private ApiService api;
    private RecyclerView rvAnswers;
    private TextView tvNoAnswer, tvAnswerCount;
    private boolean isLiked = false, isFavorited = false;
    private ImageView ivLike, ivFavorite;
    private TextView tvLikeCount, tvFavCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        questionId = getIntent().getLongExtra("questionId", -1);
        api = RetrofitClient.createService(ApiService.class);
        if (questionId == -1) { finish(); return; }

        setContentView(R.layout.activity_question_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvAnswers = findViewById(R.id.rv_answers);
        rvAnswers.setLayoutManager(new LinearLayoutManager(this));
        tvNoAnswer = findViewById(R.id.tv_no_answer);
        tvAnswerCount = findViewById(R.id.tv_answer_count);

        ivLike = findViewById(R.id.iv_like);
        ivFavorite = findViewById(R.id.iv_favorite);
        tvLikeCount = findViewById(R.id.tv_like_count);
        tvFavCount = findViewById(R.id.tv_fav_count);

        EditText etAnswer = findViewById(R.id.et_answer);
        MaterialButton btnSubmit = findViewById(R.id.btn_submit);

        loadQuestion();
        loadAnswers();
        loadLikeStatus();
        loadFavoriteStatus();

        // 点赞问题
        findViewById(R.id.btn_like).setOnClickListener(v -> {
            Map<String, Object> body = new HashMap<>();
            body.put("targetType", 1); // 1=问题
            body.put("targetId", questionId);
            api.toggleLike(body).enqueue(new Callback<JsonObject>() {
                @Override public void onResponse(Call<JsonObject> c, Response<JsonObject> r) {
                    isLiked = !isLiked;
                    ivLike.setImageResource(isLiked ? R.drawable.ic_thumb_up_filled : R.drawable.ic_thumb_up);
                    loadQuestion(); // 刷新计数
                }
                @Override public void onFailure(Call<JsonObject> c, Throwable t) {}
            });
        });

        // 收藏
        findViewById(R.id.btn_favorite).setOnClickListener(v -> {
            api.toggleFavorite(questionId).enqueue(new Callback<JsonObject>() {
                @Override public void onResponse(Call<JsonObject> c, Response<JsonObject> r) {
                    isFavorited = !isFavorited;
                    ivFavorite.setImageResource(isFavorited ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
                    loadQuestion();
                }
                @Override public void onFailure(Call<JsonObject> c, Throwable t) {}
            });
        });

        // 提交回答
        btnSubmit.setOnClickListener(v -> {
            String content = etAnswer.getText().toString().trim();
            if (content.isEmpty()) { Toast.makeText(this, "请输入回答内容", Toast.LENGTH_SHORT).show(); return; }
            Map<String, String> body = new HashMap<>();
            body.put("content", content);
            api.createAnswer(questionId, body).enqueue(new Callback<JsonObject>() {
                @Override public void onResponse(Call<JsonObject> c, Response<JsonObject> r) {
                    if (r.isSuccessful()) { etAnswer.setText(""); loadAnswers(); Toast.makeText(QuestionDetailActivity.this, "回答成功！", Toast.LENGTH_SHORT).show(); }
                }
                @Override public void onFailure(Call<JsonObject> c, Throwable t) {}
            });
        });
    }

    private void loadLikeStatus() {
        api.getLikeStatus(1, questionId).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> c, Response<JsonObject> r) {
                if (r.isSuccessful() && r.body() != null && r.body().get("code").getAsInt() == 200) {
                    isLiked = r.body().get("data").getAsBoolean();
                    ivLike.setImageResource(isLiked ? R.drawable.ic_thumb_up_filled : R.drawable.ic_thumb_up);
                }
            }
            @Override public void onFailure(Call<JsonObject> c, Throwable t) {}
        });
    }

    private void loadFavoriteStatus() {
        api.getFavoriteStatus(questionId).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> c, Response<JsonObject> r) {
                if (r.isSuccessful() && r.body() != null && r.body().get("code").getAsInt() == 200) {
                    isFavorited = r.body().get("data").getAsBoolean();
                    ivFavorite.setImageResource(isFavorited ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
                }
            }
            @Override public void onFailure(Call<JsonObject> c, Throwable t) {}
        });
    }

    private void loadQuestion() {
        api.getQuestion(questionId).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().get("code").getAsInt() != 200) return;
                JsonObject data = response.body().getAsJsonObject("data");
                JsonObject q = data.getAsJsonObject("question");

                ((TextView) findViewById(R.id.tv_title)).setText(getStr(q, "title"));
                ((TextView) findViewById(R.id.tv_content)).setText(getStr(q, "content"));
                if (data.has("author") && !data.get("author").isJsonNull())
                    ((TextView) findViewById(R.id.tv_author)).setText(getStr(data.getAsJsonObject("author"), "nickname"));
                String time = getStr(q, "createdAt"); if (time.length() > 10) time = time.substring(0, 10);
                ((TextView) findViewById(R.id.tv_time)).setText(time);

                int views = getInt(q, "viewCount"), answers = getInt(q, "answerCount");
                int likes = getInt(q, "likeCount"), favs = getInt(q, "favoriteCount");
                ((TextView) findViewById(R.id.tv_stats)).setText(views + " 浏览 · " + answers + " 回答");
                tvLikeCount.setText(String.valueOf(likes));
                tvFavCount.setText(String.valueOf(favs));

                if (data.has("tags") && data.get("tags").isJsonArray()) {
                    ChipGroup cg = findViewById(R.id.chip_group_tags); cg.removeAllViews();
                    for (JsonElement e : data.getAsJsonArray("tags")) {
                        Chip chip = new Chip(QuestionDetailActivity.this);
                        chip.setText(getStr(e.getAsJsonObject(), "name"));
                        chip.setChipBackgroundColorResource(R.color.background);
                        chip.setTextSize(12); chip.setClickable(false); cg.addView(chip);
                    }
                }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }

    private void loadAnswers() {
        api.getAnswers(questionId, 1, 50).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                JsonObject data = response.body().getAsJsonObject("data");
                JsonArray records = data.getAsJsonArray("records");
                long total = data.has("total") ? data.get("total").getAsLong() : 0;
                tvAnswerCount.setText("(" + total + ")");
                if (records == null || records.size() == 0) {
                    tvNoAnswer.setVisibility(View.VISIBLE); rvAnswers.setVisibility(View.GONE); return;
                }
                tvNoAnswer.setVisibility(View.GONE); rvAnswers.setVisibility(View.VISIBLE);
                List<JsonObject> items = new ArrayList<>();
                for (JsonElement e : records) items.add(e.getAsJsonObject());
                rvAnswers.setAdapter(new AnswerAdapter(items, api));
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }

    private String getStr(JsonObject o, String k) { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : ""; }
    private int getInt(JsonObject o, String k) { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : 0; }

    // ===== 回答适配器（含楼中楼评论） =====
    private static class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.VH> {
        private final List<JsonObject> items;
        private final ApiService api;
        AnswerAdapter(List<JsonObject> items, ApiService api) { this.items = items; this.api = api; }

        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_answer, parent, false));
        }

        @Override public void onBindViewHolder(VH h, int pos) {
            JsonObject item = items.get(pos);
            JsonObject a = item.getAsJsonObject("answer");
            long answerId = a.has("id") ? a.get("id").getAsLong() : -1;

            h.tvContent.setText(getStr(a, "content"));
            if (item.has("author") && !item.get("author").isJsonNull())
                h.tvAuthor.setText(getStr(item.getAsJsonObject("author"), "nickname"));
            else h.tvAuthor.setText("匿名用户");

            String time = getStr(a, "createdAt"); if (time.length() > 10) time = time.substring(0, 10);
            h.tvTime.setText(time);

            int accepted = a.has("isAccepted") && !a.get("isAccepted").isJsonNull() ? a.get("isAccepted").getAsInt() : 0;
            h.tvAccepted.setVisibility(accepted == 1 ? View.VISIBLE : View.GONE);

            int likeCount = getInt(a, "likeCount");
            int commentCount = getInt(a, "commentCount");
            h.tvLikeCount.setText(likeCount + " 赞");
            h.tvCommentCount.setText(commentCount + " 评论");

            // 点赞回答
            final boolean[] liked = {false};
            api.getLikeStatus(2, answerId).enqueue(new Callback<JsonObject>() {
                @Override public void onResponse(Call<JsonObject> c, Response<JsonObject> r) {
                    if (r.isSuccessful() && r.body() != null && r.body().get("code").getAsInt() == 200) {
                        liked[0] = r.body().get("data").getAsBoolean();
                        h.ivLike.setImageResource(liked[0] ? R.drawable.ic_thumb_up_filled : R.drawable.ic_thumb_up);
                    }
                }
                @Override public void onFailure(Call<JsonObject> c, Throwable t) {}
            });

            h.btnLike.setOnClickListener(v -> {
                Map<String, Object> body = new HashMap<>();
                body.put("targetType", 2); body.put("targetId", answerId);
                api.toggleLike(body).enqueue(new Callback<JsonObject>() {
                    @Override public void onResponse(Call<JsonObject> c, Response<JsonObject> r) {
                        liked[0] = !liked[0];
                        h.ivLike.setImageResource(liked[0] ? R.drawable.ic_thumb_up_filled : R.drawable.ic_thumb_up);
                        int newCount = Integer.parseInt(h.tvLikeCount.getText().toString().replace(" 赞", "")) + (liked[0] ? 1 : -1);
                        h.tvLikeCount.setText(Math.max(0, newCount) + " 赞");
                    }
                    @Override public void onFailure(Call<JsonObject> c, Throwable t) {}
                });
            });

            // 评论区 toggle
            h.btnComment.setOnClickListener(v -> {
                if (h.llComments.getVisibility() == View.GONE) {
                    h.llComments.setVisibility(View.VISIBLE);
                    loadComments(h, answerId);
                } else {
                    h.llComments.setVisibility(View.GONE);
                }
            });

            // 发送评论
            h.btnSendComment.setOnClickListener(v -> {
                String content = h.etComment.getText().toString().trim();
                if (content.isEmpty()) return;
                Map<String, Object> body = new HashMap<>();
                body.put("content", content); body.put("targetType", 2); body.put("targetId", answerId);
                api.createComment(body).enqueue(new Callback<JsonObject>() {
                    @Override public void onResponse(Call<JsonObject> c, Response<JsonObject> r) {
                        if (r.isSuccessful()) {
                            h.etComment.setText("");
                            loadComments(h, answerId);
                            int newCount = Integer.parseInt(h.tvCommentCount.getText().toString().replace(" 评论", "")) + 1;
                            h.tvCommentCount.setText(newCount + " 评论");
                        }
                    }
                    @Override public void onFailure(Call<JsonObject> c, Throwable t) {}
                });
            });
        }

        private void loadComments(VH h, long answerId) {
            api.getComments(2, answerId, 1, 20).enqueue(new Callback<JsonObject>() {
                @Override public void onResponse(Call<JsonObject> c, Response<JsonObject> r) {
                    try {
                        if (!r.isSuccessful() || r.body() == null) return;
                        h.llCommentList.removeAllViews();
                        JsonObject body = r.body();
                        if (!body.has("data") || body.get("data").isJsonNull()) return;
                        JsonObject data = body.getAsJsonObject("data");
                        if (!data.has("records") || data.get("records").isJsonNull()) return;
                        JsonArray records = data.getAsJsonArray("records");
                        if (records.size() == 0) {
                            TextView tv = new TextView(h.itemView.getContext());
                            tv.setText("暂无评论"); tv.setTextSize(13); tv.setTextColor(0xFF999999);
                            tv.setPadding(0, 4, 0, 4);
                            h.llCommentList.addView(tv);
                            return;
                        }
                        for (JsonElement e : records) {
                            JsonObject item = e.getAsJsonObject();
                            // 主评论
                            addCommentView(h, item, false);
                            // 子回复（楼中楼）
                            if (item.has("replies") && item.get("replies").isJsonArray()) {
                                JsonArray replies = item.getAsJsonArray("replies");
                                for (JsonElement re : replies) {
                                    addCommentView(h, re.getAsJsonObject(), true);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        // 容错
                        h.llCommentList.removeAllViews();
                        TextView tv = new TextView(h.itemView.getContext());
                        tv.setText("加载评论失败"); tv.setTextSize(13); tv.setTextColor(0xFF999999);
                        h.llCommentList.addView(tv);
                    }
                }
                @Override public void onFailure(Call<JsonObject> c, Throwable t) {}
            });
        }

        /** 添加一条评论到评论列表 */
        private void addCommentView(VH h, JsonObject item, boolean isReply) {
            JsonObject c = item.getAsJsonObject("comment");
            String author = "未知";
            if (item.has("author") && !item.get("author").isJsonNull())
                author = getStr(item.getAsJsonObject("author"), "nickname");
            String text = getStr(c, "content");
            String time = getStr(c, "createdAt");
            if (time.length() > 10) time = time.substring(0, 10);

            // 如果是回复，显示 "回复 @xxx：" 前缀
            String replyPrefix = "";
            if (isReply && item.has("replyUser") && !item.get("replyUser").isJsonNull()) {
                replyPrefix = " 回复 @" + getStr(item.getAsJsonObject("replyUser"), "nickname");
            }

            LinearLayout row = new LinearLayout(h.itemView.getContext());
            row.setOrientation(LinearLayout.VERTICAL);
            int leftPad = isReply ? 48 : 0; // 子回复缩进
            row.setPadding(leftPad, 6, 0, 6);

            TextView tvComment = new TextView(h.itemView.getContext());
            tvComment.setText(author + replyPrefix + "：" + text);
            tvComment.setTextSize(13);
            tvComment.setTextColor(isReply ? 0xFF555555 : 0xFF333333);
            row.addView(tvComment);

            TextView tvTime = new TextView(h.itemView.getContext());
            tvTime.setText(time); tvTime.setTextSize(11); tvTime.setTextColor(0xFF999999);
            row.addView(tvTime);

            h.llCommentList.addView(row);

            // 分割线
            View div = new View(h.itemView.getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 1);
            lp.setMarginStart(leftPad);
            div.setLayoutParams(lp);
            div.setBackgroundColor(0xFFE8E8E8);
            h.llCommentList.addView(div);
        }

        @Override public int getItemCount() { return items.size(); }
        private static String getStr(JsonObject o, String k) { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : ""; }
        private static int getInt(JsonObject o, String k) { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : 0; }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvAuthor, tvTime, tvContent, tvAccepted, tvLikeCount, tvCommentCount;
            ImageView ivLike;
            View btnLike, btnComment;
            LinearLayout llComments, llCommentList;
            EditText etComment;
            View btnSendComment;
            VH(View v) {
                super(v);
                tvAuthor = v.findViewById(R.id.tv_author); tvTime = v.findViewById(R.id.tv_time);
                tvContent = v.findViewById(R.id.tv_content); tvAccepted = v.findViewById(R.id.tv_accepted);
                tvLikeCount = v.findViewById(R.id.tv_like_count); tvCommentCount = v.findViewById(R.id.tv_comment_count);
                ivLike = v.findViewById(R.id.iv_like);
                btnLike = v.findViewById(R.id.btn_like); btnComment = v.findViewById(R.id.btn_comment);
                llComments = v.findViewById(R.id.ll_comments); llCommentList = v.findViewById(R.id.ll_comment_list);
                etComment = v.findViewById(R.id.et_comment); btnSendComment = v.findViewById(R.id.btn_send_comment);
            }
        }
    }
}
