package com.campus.zhihu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.common.PageResult;
import com.campus.zhihu.common.Result;
import com.campus.zhihu.entity.*;
import com.campus.zhihu.mapper.*;
import com.campus.zhihu.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
public class AdminController {
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final CommentMapper commentMapper;
    private final ReportMapper reportMapper;
    private final TagService tagService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    private static final String[] REASON_LABELS = {"", "垃圾广告", "色情低俗", "不实信息", "人身攻击", "违法违规", "其他"};

    // --- 数据统计 ---
    @GetMapping("/stats")
    public Result<?> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userMapper.selectCount(null));
        stats.put("questionCount", questionMapper.selectCount(null));
        stats.put("answerCount", answerMapper.selectCount(null));
        stats.put("commentCount", commentMapper.selectCount(null));
        stats.put("pendingReports", reportMapper.selectCount(new LambdaQueryWrapper<Report>().eq(Report::getStatus, 0)));
        return Result.success(stats);
    }

    // ===================== 用户管理 CRUD =====================
    @GetMapping("/users")
    public Result<?> listUsers(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
                               @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer role) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt);
        if (keyword != null) w.and(q -> q.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        if (role != null) w.eq(User::getRole, role);
        Page<User> p = userMapper.selectPage(new Page<>(page, size), w);
        p.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(PageResult.of(p));
    }

    @PostMapping("/users")
    public Result<?> createUser(@RequestBody User user) {
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())) != null) {
            return Result.error("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) user.setStatus(1);
        if (user.getRole() == null) user.setRole(0);
        userMapper.insert(user);
        return Result.success();
    }

    @PutMapping("/users/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        user.setPassword(null); // 不通过此接口修改密码
        userMapper.updateById(user);
        return Result.success();
    }

    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userMapper.update(null, new LambdaUpdateWrapper<User>().eq(User::getId, id).set(User::getStatus, status));
        return Result.success();
    }

    @PutMapping("/users/{id}/role")
    public Result<?> updateUserRole(@PathVariable Long id, @RequestParam Integer role) {
        userMapper.update(null, new LambdaUpdateWrapper<User>().eq(User::getId, id).set(User::getRole, role));
        return Result.success();
    }

    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.success();
    }

    // ===================== 问题管理 CRUD =====================
    @GetMapping("/questions")
    public Result<?> listQuestions(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
                                   @RequestParam(required = false) Integer status, @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Question> w = new LambdaQueryWrapper<Question>().orderByDesc(Question::getCreatedAt);
        if (status != null) w.eq(Question::getStatus, status);
        if (keyword != null) w.like(Question::getTitle, keyword);
        return Result.success(PageResult.of(questionMapper.selectPage(new Page<>(page, size), w)));
    }

    @PostMapping("/questions")
    public Result<?> createQuestion(Authentication auth, @RequestBody Question question) {
        if (question.getUserId() == null) question.setUserId((Long) auth.getPrincipal());
        if (question.getStatus() == null) question.setStatus(1);
        questionMapper.insert(question);
        return Result.success();
    }

    @PutMapping("/questions/{id}")
    public Result<?> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        questionMapper.updateById(question);
        return Result.success();
    }

    @PutMapping("/questions/{id}/status")
    public Result<?> updateQuestionStatus(@PathVariable Long id, @RequestParam Integer status) {
        questionMapper.update(null, new LambdaUpdateWrapper<Question>().eq(Question::getId, id).set(Question::getStatus, status));
        return Result.success();
    }

    @DeleteMapping("/questions/{id}")
    public Result<?> deleteQuestion(Authentication auth, @PathVariable Long id) {
        questionService.delete((Long) auth.getPrincipal(), id, true); return Result.success();
    }

    // ===================== 回答管理 CRUD =====================
    @GetMapping("/answers")
    public Result<?> listAnswers(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
                                  @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Answer> w = new LambdaQueryWrapper<Answer>().orderByDesc(Answer::getCreatedAt);
        if (status != null) w.eq(Answer::getStatus, status);
        return Result.success(PageResult.of(answerMapper.selectPage(new Page<>(page, size), w)));
    }

    @PostMapping("/answers")
    public Result<?> createAnswer(Authentication auth, @RequestBody Answer answer) {
        if (answer.getUserId() == null) answer.setUserId((Long) auth.getPrincipal());
        if (answer.getStatus() == null) answer.setStatus(1);
        answerMapper.insert(answer);
        return Result.success();
    }

    @PutMapping("/answers/{id}")
    public Result<?> updateAnswer(@PathVariable Long id, @RequestBody Answer answer) {
        answer.setId(id);
        answerMapper.updateById(answer);
        return Result.success();
    }

    @PutMapping("/answers/{id}/status")
    public Result<?> updateAnswerStatus(@PathVariable Long id, @RequestParam Integer status) {
        answerMapper.update(null, new LambdaUpdateWrapper<Answer>().eq(Answer::getId, id).set(Answer::getStatus, status));
        return Result.success();
    }

    @DeleteMapping("/answers/{id}")
    public Result<?> deleteAnswer(Authentication auth, @PathVariable Long id) {
        answerService.delete((Long) auth.getPrincipal(), id, true); return Result.success();
    }

    // ===================== 评论管理 CRUD =====================
    @GetMapping("/comments")
    public Result<?> listComments(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(PageResult.of(commentMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Comment>().orderByDesc(Comment::getCreatedAt))));
    }

    @PutMapping("/comments/{id}")
    public Result<?> updateComment(@PathVariable Long id, @RequestBody Comment comment) {
        comment.setId(id);
        commentMapper.updateById(comment);
        return Result.success();
    }

    @DeleteMapping("/comments/{id}")
    public Result<?> deleteComment(@PathVariable Long id) {
        commentMapper.deleteById(id); return Result.success();
    }

    // ===================== 举报管理（增强） =====================
    @GetMapping("/reports")
    public Result<?> listReports(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
                                  @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Report> w = new LambdaQueryWrapper<Report>().orderByDesc(Report::getCreatedAt);
        if (status != null) w.eq(Report::getStatus, status);
        return Result.success(PageResult.of(reportMapper.selectPage(new Page<>(page, size), w)));
    }

    /** 查看被举报的内容详情 */
    @GetMapping("/reports/{id}/content")
    public Result<?> getReportContent(@PathVariable Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null) return Result.error("举报不存在");
        Map<String, Object> result = new HashMap<>();
        result.put("targetType", report.getTargetType());
        result.put("targetId", report.getTargetId());
        if (report.getTargetType() == 1) {
            Question q = questionMapper.selectById(report.getTargetId());
            if (q != null) {
                result.put("title", q.getTitle());
                result.put("content", q.getContent());
                result.put("authorId", q.getUserId());
                User author = userMapper.selectById(q.getUserId());
                result.put("authorName", author != null ? author.getNickname() : "未知");
            } else { result.put("content", "（内容已被删除）"); }
        } else if (report.getTargetType() == 2) {
            Answer a = answerMapper.selectById(report.getTargetId());
            if (a != null) {
                result.put("content", a.getContent());
                result.put("authorId", a.getUserId());
                User author = userMapper.selectById(a.getUserId());
                result.put("authorName", author != null ? author.getNickname() : "未知");
            } else { result.put("content", "（内容已被删除）"); }
        }
        // 举报人信息
        User reporter = userMapper.selectById(report.getUserId());
        result.put("reporterName", reporter != null ? reporter.getNickname() : "未知");
        result.put("reporterId", report.getUserId());
        String reasonLabel = (report.getReason() != null && report.getReason() >= 1 && report.getReason() < REASON_LABELS.length)
                ? REASON_LABELS[report.getReason()] : "其他";
        result.put("reasonLabel", reasonLabel);
        result.put("description", report.getDescription());
        return Result.success(result);
    }

    @PutMapping("/reports/{id}/handle")
    public Result<?> handleReport(Authentication auth, @PathVariable Long id,
                                   @RequestParam Integer status, @RequestParam(required = false) String note) {
        Report report = reportMapper.selectById(id);
        if (report == null) return Result.error("举报不存在");

        // 更新举报状态
        reportMapper.update(null, new LambdaUpdateWrapper<Report>().eq(Report::getId, id)
                .set(Report::getStatus, status).set(Report::getHandleNote, note)
                .set(Report::getHandleUserId, (Long) auth.getPrincipal())
                .set(Report::getHandleTime, LocalDateTime.now()));

        String reasonLabel = (report.getReason() != null && report.getReason() >= 1 && report.getReason() < REASON_LABELS.length)
                ? REASON_LABELS[report.getReason()] : "违规内容";
        String targetTypeName = report.getTargetType() == 1 ? "问题" : "回答";

        if (status == 1) {
            // ===== 举报成功：删除内容 + 通知被举报人 + 通知举报人 =====
            String contentPreview = "";
            if (report.getTargetType() == 1) {
                Question q = questionMapper.selectById(report.getTargetId());
                if (q != null) {
                    contentPreview = q.getTitle();
                    questionMapper.deleteById(q.getId());
                    notificationService.send(q.getUserId(), null, 4, "内容违规通知",
                            "您的问题「" + q.getTitle() + "」因涉嫌" + reasonLabel + "已被管理员删除" + (note != null && !note.isEmpty() ? "，备注：" + note : ""),
                            1, q.getId());
                }
            } else if (report.getTargetType() == 2) {
                Answer a = answerMapper.selectById(report.getTargetId());
                if (a != null) {
                    contentPreview = a.getContent().length() > 20 ? a.getContent().substring(0, 20) + "..." : a.getContent();
                    answerMapper.deleteById(a.getId());
                    notificationService.send(a.getUserId(), null, 4, "内容违规通知",
                            "您的回答「" + contentPreview + "」因涉嫌" + reasonLabel + "已被管理员删除" + (note != null && !note.isEmpty() ? "，备注：" + note : ""),
                            2, a.getId());
                }
            }
            // 通知举报人：举报成功
            notificationService.send(report.getUserId(), null, 4, "举报处理结果",
                    "您举报的" + targetTypeName + "「" + contentPreview + "」已被管理员确认违规并删除，感谢您的反馈！",
                    null, null);

        } else if (status == 2) {
            // ===== 举报失败：通知举报人 =====
            String contentPreview = "";
            if (report.getTargetType() == 1) {
                Question q = questionMapper.selectById(report.getTargetId());
                if (q != null) contentPreview = q.getTitle();
            } else if (report.getTargetType() == 2) {
                Answer a = answerMapper.selectById(report.getTargetId());
                if (a != null) contentPreview = a.getContent().length() > 20 ? a.getContent().substring(0, 20) + "..." : a.getContent();
            }
            notificationService.send(report.getUserId(), null, 4, "举报处理结果",
                    "您举报的" + targetTypeName + "「" + contentPreview + "」经审核未发现违规" + (note != null && !note.isEmpty() ? "，理由：" + note : "") + "。如有异议，可重新举报。",
                    null, null);
        }
        return Result.success();
    }

    // ===================== 标签管理 =====================
    @GetMapping("/tags")
    public Result<?> listTags() { return Result.success(tagService.list()); }

    @PostMapping("/tags")
    public Result<?> createTag(@RequestBody Tag tag) { return Result.success(tagService.create(tag)); }

    @PutMapping("/tags/{id}")
    public Result<?> updateTag(@PathVariable Long id, @RequestBody Tag tag) { tag.setId(id); return Result.success(tagService.update(tag)); }

    @DeleteMapping("/tags/{id}")
    public Result<?> deleteTag(@PathVariable Long id) { tagService.delete(id); return Result.success(); }
}
