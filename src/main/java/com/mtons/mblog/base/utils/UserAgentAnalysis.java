package com.mtons.mblog.base.utils;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.UserAgent;
import eu.bitwalker.useragentutils.Version;

/**
 * 浏览器UserAgent解析
 *
 * @author Alex
 * @date 2019/06/25
 */
public class UserAgentAnalysis {

    /**
     * 解析
     * String ua = request.getHeader("User-Agent");
     *
     * @param ua 请求头字段包含有关发起请求的用户代理的信息
     */
    public static AccessInfo analysis(String ua) {
        if (ua != null && !"".equals(ua.trim())) {
            //转成UserAgent对象
            UserAgent userAgent = UserAgent.parseUserAgentString(ua);
            if (userAgent != null) {
                AccessInfo accessInfo = new AccessInfo();
                //获取浏览器信息
                Browser browser = userAgent.getBrowser();
                if (browser != null) {
                    // 浏览器名称
                    String browserName = browser.getName();
                    //浏览器名称
                    accessInfo.setBrowserName(browserName);
                    if (browser.getGroup() != null) {
                        // 浏览器大类
                        String group = browser.getGroup().getName();
                        //浏览器大类
                        accessInfo.setGroup(group);
                    }
                }
                // 详细版本
                Version browserVersion = userAgent.getBrowserVersion();
                if (browserVersion != null) {
                    // 浏览器主版本
                    String version = browserVersion.getMajorVersion();
                    //详细版本
                    accessInfo.setBrowserVersion(browserVersion.getVersion());
                    //浏览器主版本
                    accessInfo.setVersion(version);
                }
                if (userAgent.getOperatingSystem() != null) {
                    //访问设备系统
                    accessInfo.setOperatingSystem(userAgent.getOperatingSystem().toString());
                    if (userAgent.getOperatingSystem().getDeviceType() != null && userAgent.getOperatingSystem().getDeviceType().getName() != null) {
                        if (userAgent.getOperatingSystem().getDeviceType().getName().equals("Computer")) {
                            //访问设备类型
                            accessInfo.setDeviceType("电脑");
                        } else if (userAgent.getOperatingSystem().getDeviceType().getName().equals("Mobile")) {
                            //访问设备类型
                            accessInfo.setDeviceType("手机");
                        } else if (userAgent.getOperatingSystem().getDeviceType().getName().equals("Tablet")) {
                            //访问设备类型
                            accessInfo.setDeviceType("平板");
                        } else if (userAgent.getOperatingSystem().getDeviceType().getName().equals("Game console")) {
                            //访问设备类型
                            accessInfo.setDeviceType("游戏机");
                        } else if (userAgent.getOperatingSystem().getDeviceType().getName().equals("Digital media receiver")) {
                            //访问设备类型
                            accessInfo.setDeviceType("数字媒体接收器");
                        } else if (userAgent.getOperatingSystem().getDeviceType().getName().equals("Wearable computer")) {
                            //访问设备类型
                            accessInfo.setDeviceType("可穿戴设备");
                        } else if (userAgent.getOperatingSystem().getDeviceType().getName().equals("Unknown")) {
                            //访问设备类型
                            accessInfo.setDeviceType("未知设备");
                        }
                    }
                }
                return accessInfo;
            }
        }
        return null;
    }
}
