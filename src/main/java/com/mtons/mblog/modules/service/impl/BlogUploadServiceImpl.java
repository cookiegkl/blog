package com.mtons.mblog.modules.service.impl;

import com.mtons.mblog.modules.entity.BlogUpload;
import com.mtons.mblog.modules.repository.BlogUploadRepository;
import com.mtons.mblog.modules.service.BlogUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlogUploadServiceImpl implements BlogUploadService {

    @Autowired
    private BlogUploadRepository blogUploadRepository;

    @Override
    public void addSmms(BlogUpload upload) {
        blogUploadRepository.save(upload);
    }
}
