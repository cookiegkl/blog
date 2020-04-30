package com.mtons.mblog.base.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbMakerConfigException;
import org.lionsoul.ip2region.DbSearcher;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;


/**
 * 获取IP地址
 *
 * @author Alex
 * @date 2019/06/20
 */
public class IpAddress {

    private static final Logger logger = LogManager.getLogger(IpAddress.class);

    private static DbSearcher searcher = null;

    static {
        try {
            searcher = new DbSearcher(new DbConfig(), "docs/ip2region.db");
        } catch (FileNotFoundException e) {
            if (logger.isErrorEnabled()) {
                logger.error("IP地址库文件不存在错误", e);
            }
        } catch (DbMakerConfigException e) {
            if (logger.isErrorEnabled()) {
                logger.error("IP地址库初始化配置错误", e);
            }
        }
    }

    /**
     * 获取IP地址
     *
     * @param request
     * @return
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        //对于通过多个代理的情况， 第一个IP为客户端真实IP,多个IP按照','分割 "***.***.***.***".length()
        // "***.***.***.***".length()
        if (ip != null && ip.length() > 15) {
            // = 15
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip;
    }

    /**
     * 查询IP归属地
     *
     * @param ip
     * @return
     */
    public static String queryAddress(String ip) {
        if (searcher != null) {
            try {
                return format(searcher.memorySearch(ip).getRegion());
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("IP地址查询错误", e);
                }
            }
        }
        return "";
    }

    /**
     * IP归属地格式化
     *
     * @param ipAddress
     * @return
     */
    private static String format(String ipAddress) {
        if (ipAddress != null && !"".equals(ipAddress.trim())) {
            //0替换成空串
            ipAddress = StringUtils.replace(ipAddress, "|0", " ");
            //竖线替换成空格
            ipAddress = StringUtils.replace(ipAddress, "|", " ");
        }
        return ipAddress;
    }
}
