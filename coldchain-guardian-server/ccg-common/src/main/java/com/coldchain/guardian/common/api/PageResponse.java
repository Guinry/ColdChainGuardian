package com.coldchain.guardian.common.api;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PageResponse<T> {

    private List<T> data;
    private long total;
    private int page;
    private int size;
    private int totalPages;

    public PageResponse(List<T> data, long total, int page, int size) {
        this.data = data;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }

    // 静态方法，用于创建PageResponse实例
    public static <T> PageResponse<T> of(List<T> records, long total, int page, int size) {
        return new PageResponse<>(records, total, page, size);
    }
}