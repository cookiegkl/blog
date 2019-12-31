package com.mtons.mblog.web.controller.site.posts;

import com.mtons.mblog.base.lang.Consts;
import com.mtons.mblog.base.utils.FileKit;
import com.mtons.mblog.base.utils.FileUtils;
import com.mtons.mblog.base.utils.SmmsUtil;
import com.mtons.mblog.config.SmmsResult;
import com.mtons.mblog.modules.data.UploadResult;
import com.mtons.mblog.modules.entity.BlogUpload;
import com.mtons.mblog.modules.service.BlogUploadService;
import com.mtons.mblog.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

/**
 * 文件上传控制层
 *
 * @author langhsu
 */
@Controller
@RequestMapping("/post")
public class UploadController extends BaseController {

    @Autowired
    private BlogUploadService uploadService;

    public static HashMap<String, String> errorInfo = new HashMap<>();

    static {
        errorInfo.put("SUCCESS", "SUCCESS");
        errorInfo.put("NOFILE", "未包含文件上传域");
        errorInfo.put("TYPE", "不允许的文件格式");
        errorInfo.put("SIZE", "文件大小超出限制，最大支持2Mb");
        errorInfo.put("ENTYPE", "请求类型ENTYPE错误");
        errorInfo.put("REQUEST", "上传请求异常");
        errorInfo.put("IO", "IO异常");
        errorInfo.put("DIR", "目录创建失败");
        errorInfo.put("UNKNOWN", "未知错误");
    }

    @PostMapping("/upload")
    @ResponseBody
    public UploadResult upload(@RequestParam(value = "file", required = false) MultipartFile multipartFile) {
        UploadResult result = new UploadResult();
        // 检查空
        if (null == multipartFile || multipartFile.isEmpty()) {
            return result.error(errorInfo.get("NOFILE"));
        }
        String fileName = multipartFile.getOriginalFilename();
        // 检查类型
        if (!FileKit.checkFileType(fileName)) {
            return result.error(errorInfo.get("TYPE"));
        }
        // 检查大小
        String limitSize = siteOptions.getValue(Consts.STORAGE_LIMIT_SIZE);
        if (StringUtils.isBlank(limitSize)) {
            limitSize = "2";
        }
        if (multipartFile.getSize() > (Long.parseLong(limitSize) * 1024 * 1024)) {
            return result.error(errorInfo.get("SIZE"));
        }
        // 保存图片
        try {
            File file = FileUtils.multipartFileToFile(multipartFile);
            SmmsResult smmsResult = SmmsUtil.executeImport(file);
            SmmsResult.Data data = smmsResult.getData();
            result.ok(errorInfo.get("SUCCESS"));
            result.setName(data.getFilename());
            result.setPath(data.getUrl());
            result.setSize(data.getSize());
            savePic(smmsResult, data);
        } catch (Exception e) {
            result.error(errorInfo.get("UNKNOWN"));
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 保存图片
     *
     * @param smmsResult 图床返回
     * @param data       图片信息
     */
    private void savePic(SmmsResult smmsResult, SmmsResult.Data data) {
        BlogUpload upload = new BlogUpload();
        upload.setFileId(data.getFile_id());
        upload.setFileName(data.getFilename());
        upload.setWidth(data.getWidth());
        upload.setHeight(data.getHeight());
        upload.setStoreName(data.getStorename());
        upload.setSize(data.getSize());
        upload.setPath(data.getPath());
        upload.setHash(data.getHash());
        upload.setUrl(data.getUrl());
        upload.setDeletePath(data.getDelete());
        upload.setPage(data.getPage());
        upload.setRequestId(smmsResult.getRequestId());
        upload.setCreateTime(new Date());
        upload.setLastUpdateTime(new Date());
        upload.setIsDelete(false);
        uploadService.addSmms(upload);
    }
}
