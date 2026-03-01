package com.campus.zhihu.common;

import lombok.Data;

@Data
public class PageResult<T> {
    private java.util.List<T> records;
    private long total;
    private long page;
    private long size;

    public static <T> PageResult<T> of(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> p) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(p.getRecords());
        r.setTotal(p.getTotal());
        r.setPage(p.getCurrent());
        r.setSize(p.getSize());
        return r;
    }

    public static <T> PageResult<T> of(java.util.List<T> records, long total, long page, long size) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(records);
        r.setTotal(total);
        r.setPage(page);
        r.setSize(size);
        return r;
    }
}
