package com.campus.zhihu.util;

import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.campus.zhihu.data.remote.ApiService;
import com.campus.zhihu.data.remote.RetrofitClient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 举报弹窗工具类
 * targetType: 1=问题, 2=回答
 */
public class ReportDialog {

    private static final String[] REASON_LABELS = {
            "垃圾广告", "色情低俗", "不实信息", "人身攻击", "违法违规", "其他"
    };
    // reason 值从 1 开始
    private static final int[] REASON_VALUES = {1, 2, 3, 4, 5, 6};

    /**
     * 显示举报弹窗
     * @param context   上下文
     * @param targetType 1=问题, 2=回答
     * @param targetId   目标 ID
     */
    public static void show(Context context, int targetType, long targetId) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("举报")
                .setItems(REASON_LABELS, (dialog, which) -> {
                    // 选择原因后弹出补充描述输入
                    showDescriptionDialog(context, targetType, targetId, REASON_VALUES[which], REASON_LABELS[which]);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private static void showDescriptionDialog(Context context, int targetType, long targetId, int reason, String reasonLabel) {
        EditText etDesc = new EditText(context);
        etDesc.setHint("补充说明（可选）");
        etDesc.setTextSize(14);
        etDesc.setMinLines(2);
        etDesc.setMaxLines(4);

        LinearLayout container = new LinearLayout(context);
        container.setPadding(dp2px(context, 24), dp2px(context, 8), dp2px(context, 24), 0);
        container.addView(etDesc, new LinearLayout.LayoutParams(-1, -2));

        new MaterialAlertDialogBuilder(context)
                .setTitle("举报原因：" + reasonLabel)
                .setView(container)
                .setPositiveButton("提交", (dialog, w) -> {
                    submitReport(context, targetType, targetId, reason, etDesc.getText().toString().trim());
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private static void submitReport(Context context, int targetType, long targetId, int reason, String description) {
        ApiService api = RetrofitClient.createService(ApiService.class);
        Map<String, Object> body = new HashMap<>();
        body.put("targetType", targetType);
        body.put("targetId", targetId);
        body.put("reason", reason);
        if (!description.isEmpty()) body.put("description", description);

        api.createReport(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null && response.body().get("code").getAsInt() == 200) {
                    Toast.makeText(context, "举报提交成功，我们会尽快处理", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "举报提交失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(context, "网络错误，请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static int dp2px(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
