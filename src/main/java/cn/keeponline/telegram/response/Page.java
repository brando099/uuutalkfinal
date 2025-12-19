package cn.keeponline.telegram.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * 分页数据封装
 * @author jiawei
 * @since 2018-03-13 16:59:13
 */
@Data
@ToString
@NoArgsConstructor
public class Page<T> {
    /**
     * 当前页码
     */
    private Integer pageIndex = 1;

    /**
     * 页长
     */
    private Integer pageSize = 20;

    /**
     * 总条数
     */
    private Integer totalRecords;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 数据
     */
    private T data;

    /**
     * 筛选条件集合
     */
    private Map<String, Object> filterMap;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 创建一个分页对象
     *
     * @param data 数据对象
     * @return Page
     */
    public static <T> Page<T> success(T data, Integer totalRecords, Integer pageIndex, Integer pageSize) {
        Page<T> page = new Page<T>();
        page.data = data;
        page.totalRecords = totalRecords;
        page.pageIndex = pageIndex;
        page.pageSize = pageSize;
        return page;
    }

}