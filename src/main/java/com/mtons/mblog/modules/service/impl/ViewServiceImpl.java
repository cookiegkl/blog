package com.mtons.mblog.modules.service.impl;

import com.mtons.mblog.modules.entity.View;
import com.mtons.mblog.modules.repository.ViewRepository;
import com.mtons.mblog.modules.service.IViewService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Alex
 * @since 2019-06-20
 */
@Service
public class ViewServiceImpl implements IViewService {

    @Resource
    private ViewRepository viewRepository;

    @Override
    public Page<View> listViews(Pageable pageable) {
        Page<View> page = viewRepository.findAll(pageable);
        List<View> list = new ArrayList<>();
        if (page != null) {
            List<View> content = page.getContent();
            if (CollectionUtils.isNotEmpty(content)) {
                content.forEach(po -> list.add(po));
            }
        }
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    public void addViews(List<View> data) {
        viewRepository.saveAll(data);
    }
}
