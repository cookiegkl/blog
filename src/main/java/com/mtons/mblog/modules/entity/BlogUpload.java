/*
+--------------------------------------------------------------------------
|   Mblog [#RELEASE_VERSION#]
|   ========================================
|   Copyright (c) 2014, 2015 mtons. All Rights Reserved
|   http://www.mtons.com
|
+---------------------------------------------------------------------------
*/
package com.mtons.mblog.modules.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * SM图床
 */
@Entity
@Table(name = "blog_upload")
@Data
public class BlogUpload implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "file_id")
    private Integer fileId;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "size")
    private Integer size;

    @Column(name = "path")
    private String path;

    @Column(name = "hash")
    private String hash;

    @Column(name = "url")
    private String url;

    @Column(name = "delete_path")
    private String deletePath;

    @Column(name = "page")
    private String page;

    @Column(name = "request_id")
    private String requestId;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "last_update_time")
    private Date lastUpdateTime;

    /**
     * 是否删除(1：删除，0：未删除)
     */
    @Column(name = "is_delete")
    private Boolean isDelete;
}
