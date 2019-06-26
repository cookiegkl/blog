package com.mtons.mblog.modules.service;

import com.mtons.mblog.modules.entity.View;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Alex
 * @since 2019-06-20
 */
public interface IViewService {

    /**
     * 分页查询浏览数据
     *
     * @return
     */
    Page<View> listViews(Pageable pageable);

    /**
     * 添加浏览记录
     *
     * @param data
     */
    void addViews(List<View> data);
}
