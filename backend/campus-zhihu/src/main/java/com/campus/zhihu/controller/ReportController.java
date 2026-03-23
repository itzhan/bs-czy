package com.campus.zhihu.controller;

import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.ReportDTO;
import com.campus.zhihu.entity.Report;
import com.campus.zhihu.mapper.ReportMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/reports") @RequiredArgsConstructor
public class ReportController {
    private final ReportMapper reportMapper;

    @PostMapping
    public Result<?> create(Authentication auth, @Valid @RequestBody ReportDTO dto) {
        Report r = new Report();
        r.setUserId((Long) auth.getPrincipal());
        r.setTargetType(dto.getTargetType()); r.setTargetId(dto.getTargetId());
        r.setReason(dto.getReason()); r.setDescription(dto.getDescription()); r.setStatus(0);
        reportMapper.insert(r);
        return Result.success("举报提交成功");
    }

    /** 查看自己的举报记录 */
    @GetMapping("/my")
    public Result<?> myReports(Authentication auth,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Report> p = reportMapper.selectPage(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size),
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Report>()
                        .eq(Report::getUserId, (Long) auth.getPrincipal())
                        .orderByDesc(Report::getCreatedAt));
        return Result.success(com.campus.zhihu.common.PageResult.of(p));
    }
}
