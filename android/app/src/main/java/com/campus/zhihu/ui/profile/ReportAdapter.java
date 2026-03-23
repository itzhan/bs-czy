package com.campus.zhihu.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.campus.zhihu.R;
import com.google.gson.JsonObject;
import java.util.List;

/**
 * 举报记录列表适配器
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.VH> {
    private static final String[] REASON_LABELS = {"", "垃圾广告", "色情低俗", "不实信息", "人身攻击", "违法违规", "其他"};
    private static final String[] STATUS_LABELS = {"⏳ 待处理", "✅ 已处理", "❌ 已驳回"};
    private static final int[] STATUS_COLORS = {0xFFFA8C16, 0xFF52C41A, 0xFF999999};

    private final List<JsonObject> items;

    public ReportAdapter(List<JsonObject> items) { this.items = items; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        JsonObject item = items.get(position);
        int targetType = getInt(item, "targetType");
        int status = getInt(item, "status");
        int reason = getInt(item, "reason");

        // 类型
        h.tvTargetType.setText(targetType == 1 ? "举报问题 #" + getInt(item, "targetId") : "举报回答 #" + getInt(item, "targetId"));

        // 状态
        String statusLabel = (status >= 0 && status < STATUS_LABELS.length) ? STATUS_LABELS[status] : "未知";
        h.tvStatus.setText(statusLabel);
        int statusColor = (status >= 0 && status < STATUS_COLORS.length) ? STATUS_COLORS[status] : 0xFF999999;
        h.tvStatus.setTextColor(statusColor);

        // 原因
        String reasonLabel = (reason >= 1 && reason < REASON_LABELS.length) ? REASON_LABELS[reason] : "其他";
        h.tvReason.setText("举报原因：" + reasonLabel);

        // 补充说明
        String desc = getStr(item, "description");
        if (!desc.isEmpty()) {
            h.tvDescription.setText("补充说明：" + desc);
            h.tvDescription.setVisibility(View.VISIBLE);
        } else {
            h.tvDescription.setVisibility(View.GONE);
        }

        // 处理结果
        if (status == 1) {
            h.llHandleResult.setVisibility(View.VISIBLE);
            h.tvHandleTitle.setText("✅ 举报成功");
            h.tvHandleTitle.setTextColor(0xFF52C41A);
            String note = getStr(item, "handleNote");
            h.tvHandleNote.setText(!note.isEmpty() ? "处理备注：" + note : "被举报的内容已被删除");
        } else if (status == 2) {
            h.llHandleResult.setVisibility(View.VISIBLE);
            h.tvHandleTitle.setText("❌ 举报未通过");
            h.tvHandleTitle.setTextColor(0xFFFF4D4F);
            String note = getStr(item, "handleNote");
            h.tvHandleNote.setText(!note.isEmpty() ? "驳回理由：" + note : "经审核未发现违规");
        } else {
            h.llHandleResult.setVisibility(View.GONE);
        }

        // 时间
        String time = getStr(item, "createdAt");
        if (time.length() > 16) time = time.substring(0, 16).replace("T", " ");
        h.tvTime.setText("提交时间：" + time);
    }

    @Override public int getItemCount() { return items.size(); }

    private static String getStr(JsonObject o, String k) { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : ""; }
    private static int getInt(JsonObject o, String k) { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : 0; }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTargetType, tvStatus, tvReason, tvDescription, tvHandleTitle, tvHandleNote, tvTime;
        LinearLayout llHandleResult;
        VH(View v) {
            super(v);
            tvTargetType = v.findViewById(R.id.tv_target_type);
            tvStatus = v.findViewById(R.id.tv_status);
            tvReason = v.findViewById(R.id.tv_reason);
            tvDescription = v.findViewById(R.id.tv_description);
            tvHandleTitle = v.findViewById(R.id.tv_handle_title);
            tvHandleNote = v.findViewById(R.id.tv_handle_note);
            tvTime = v.findViewById(R.id.tv_time);
            llHandleResult = v.findViewById(R.id.ll_handle_result);
        }
    }
}
