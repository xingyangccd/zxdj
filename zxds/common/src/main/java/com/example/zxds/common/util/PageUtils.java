package com.example.zxds.common.util;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装类
 * <p>
 * 作用：统一封装分页查询的返回结果，前端拿到后可以直接展示分页数据。
 * <p>
 * 包含字段：
 * - list：当前页的数据列表
 * - totalCount：总记录数
 * - pageSize：每页记录数
 * - pageIndex：当前页码
 * - totalPage：总页数（构造方法里自动计算：总记录数 / 每页条数，向上取整）
 */

@Data
public class PageUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 总记录数
     */
    private long totalCount;
    /**
     * 每页记录数
     */
    private int pageSize;
    /**
     * 总页数
     */
    private int totalPage;
    /**
     * 当前页数
     */
    private int pageIndex;
    /**
     * 列表数据
     */
    private List<?> list;

    public PageUtils(List<?> list, long totalCount, int pageIndex, int pageSize) {
        this.list = list;
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.pageIndex = pageIndex;
        this.totalPage = (int) Math.ceil((double) totalCount / pageSize);
    }

}