package com.mtons.mblog.modules.entity;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;

import javax.persistence.*;

/**
 * 浏览
 *
 * @author Alex
 * @since 2019-06-20
 */
@Entity
@Table(name = "blog_view")
@Data
public class View implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /**
     * 浏览器
     */
    @Column(name = "browser_name")
    private String browserName;

    /**
     * 设备类型
     */
    @Column(name = "device_type")
    private String deviceType;

    /**
     * ip
     */
    @Column(name = "ip")
    private String ip;

    /**
     * 归属地
     */
    @Column(name = "location")
    private String location;

    /**
     * 操作系统
     */
    @Column(name = "operating_system")
    private String operatingSystem;

    /**
     * 来源页面
     */
    @Column(name = "referrer")
    private String referrer;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 访问地址
     */
    @Column(name = "url")
    private String url;

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
