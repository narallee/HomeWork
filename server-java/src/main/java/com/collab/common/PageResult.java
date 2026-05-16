package com.collab.common;

import lombok.Data;

@Data
public class PageResult<T> {
    private java.util.List<T> list;
    private long total;
    private int page;
    private int pageSize;

    public PageResult(java.util.List<T> list, long total, int page, int pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }
}
