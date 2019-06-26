package com.mtons.mblog.web.interceptor;

import com.google.common.collect.Queues;
import com.mtons.mblog.base.utils.AccessInfo;
import com.mtons.mblog.base.utils.Configuration;
import com.mtons.mblog.base.utils.IpAddress;
import com.mtons.mblog.base.utils.UserAgentAnalysis;
import com.mtons.mblog.modules.entity.View;
import com.mtons.mblog.modules.service.IViewService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * 页面浏览工具类
 *
 * @author Alex
 * @date 2019/06/20
 */
@Component("pageViewManage")
public class PageViewManage implements InitializingBean {

    private static final Logger LOGGER = LogManager.getLogger(PageViewManage.class);

    @Resource
    IViewService pageViewService;

    /**
     * 创建一个可重用固定线程数的线程池
     */
    private ExecutorService pool = Executors.newFixedThreadPool(1);

    private static final BlockingQueue<View> blockingQueue = new ArrayBlockingQueue<>(1000000);

    /**
     * 线程活动
     */
    private volatile boolean threadActivity = true;

    private static final int BROWSER_NAME_LENGTH = 90;

    @PreDestroy
    public void destroy() {
        threadActivity = false;
        pool.shutdownNow();

    }

    @Override
    public void afterPropertiesSet() {
        pool.execute(() -> {
            while (threadActivity) {
                // 如果系统关闭，则不再运行
                try {
                    List<View> data = new ArrayList<>();
                    // 每次到1000条数据才进行入库，或者等待1分钟，没达到1000条也继续入库
                    // 第三个参数：数量; 第四个参数：时间; 第五个参数：时间单位
                    Queues.drain(blockingQueue, data, 1000, 1, TimeUnit.MINUTES);
                    pageViewService.addViews(data);
                } catch (InterruptedException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("访问量消费队列错误", e);
                    }
                }
            }
        });
    }

    /**
     * 添加访问量
     *
     * @param request 页面访问量
     * @return
     */
    public void addPV(HttpServletRequest request) {
        View pv = new View();
        pv.setCreateTime(new Date());
        pv.setLastUpdateTime(new Date());
        pv.setIsDelete(Boolean.FALSE);
        String ipAddress = IpAddress.getClientIpAddress(request);
        pv.setIp(ipAddress);
        pv.setLocation(IpAddress.queryAddress(ipAddress));
        pv.setReferrer(request.getHeader("Referer"));
        pv.setUrl(Configuration.getUrl(request) + Configuration.baseURI(request.getRequestURI(), request.getContextPath()) + (request.getQueryString() != null && !"".equals(request.getQueryString().trim()) ? "?" + request.getQueryString() : ""));
        AccessInfo accessInfo = UserAgentAnalysis.analysis(request.getHeader("User-Agent"));
        if (accessInfo != null) {
            if (accessInfo.getBrowserName() != null && accessInfo.getBrowserName().length() < BROWSER_NAME_LENGTH && accessInfo.getBrowserVersion() != null && accessInfo.getBrowserVersion().length() < BROWSER_NAME_LENGTH) {
                pv.setBrowserName(accessInfo.getBrowserName() + " " + accessInfo.getBrowserVersion());
            }
            if (accessInfo.getOperatingSystem() != null && accessInfo.getOperatingSystem().length() < BROWSER_NAME_LENGTH) {
                pv.setOperatingSystem(accessInfo.getOperatingSystem());
            }
            pv.setDeviceType(accessInfo.getDeviceType());
        }
        blockingQueue.offer(pv);
    }
}
