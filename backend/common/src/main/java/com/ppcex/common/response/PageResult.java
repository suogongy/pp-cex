package com.ppcex.common.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 *
 * @param <T> 数据类型
 */
@Data
@Accessors(chain = true)
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 分页信息
     */
    private Pagination pagination;

    /**
     * 分页信息
     */
    @Data
    @Accessors(chain = true)
    public static class Pagination implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 当前页码
         */
        private Long page;

        /**
         * 每页大小
         */
        private Long size;

        /**
         * 总记录数
         */
        private Long total;

        /**
         * 总页数
         */
        private Long pages;

        /**
         * 是否有下一页
         */
        private Boolean hasNext;

        /**
         * 是否有上一页
         */
        private Boolean hasPrevious;

        public Pagination() {
        }

        public Pagination(Long page, Long size, Long total) {
            this.page = page;
            this.size = size;
            this.total = total;
            this.pages = (total + size - 1) / size;
            this.hasNext = page < pages;
            this.hasPrevious = page > 1;
        }
    }

    public PageResult() {
    }

    public PageResult(List<T> list, Pagination pagination) {
        this.list = list;
        this.pagination = pagination;
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> list, Long page, Long size, Long total) {
        Pagination pagination = new Pagination(page, size, total);
        return new PageResult<>(list, pagination);
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(List.of(), new Pagination(1L, 10L, 0L));
    }

    /**
     * 判断是否为空
     */
    public boolean isEmpty() {
        return list == null || list.isEmpty();
    }

    /**
     * 判断是否不为空
     */
    public boolean isNotEmpty() {
        return !isEmpty();
    }
}