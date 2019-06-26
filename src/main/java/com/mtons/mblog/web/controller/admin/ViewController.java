package com.mtons.mblog.web.controller.admin;

import com.mtons.mblog.modules.entity.View;
import com.mtons.mblog.modules.service.IViewService;
import com.mtons.mblog.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 浏览控制层
 *
 * @author Alex
 * @date 2019/06/20
 */
@Controller
@RequestMapping("admin/view")
public class ViewController extends BaseController {

    @Autowired
    private IViewService viewService;

    @RequestMapping("list")
    public String list(ModelMap model) {
        Pageable pageable = wrapPageable(Sort.by(Sort.Direction.DESC, "id"));
        Page<View> list = viewService.listViews(pageable);
        model.put("page", list);
        return "/admin/view/list";
    }
}
