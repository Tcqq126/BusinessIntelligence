package com.business.intelligence.task;

import com.business.intelligence.crawler.eleme.ElemeCrawlerAll;
import com.business.intelligence.crawler.mt.MTCrawler;
import com.business.intelligence.dao.UserDao;
import com.business.intelligence.model.Authenticate;
import com.business.intelligence.model.Platform;
import com.business.intelligence.model.User;
import com.business.intelligence.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yanshi on 2017/7/15.
 */
@Slf4j
@Component
public class CrawlerTasks {
    @Autowired
    private ElemeCrawlerAll elemeCrawlerAll;

    @Autowired
    private UserDao userdao;

    @Autowired
    private MTCrawler mtCrawler;

    @Scheduled(cron = "* * 3 * * *")
    public void doRun() {
        elemeCrawlerAll.runAllCrawler();
    }

    @Scheduled(cron = "* * 3 30 * *")
    public void runAllMtCrawler() throws InterruptedException {
        List<Authenticate> authenticates = getAllUser();
        Date startDate = new Date();
        Date endDate = DateUtils.addDays(new Date(), -30);

        String startTime = DateFormatUtils.format(startDate, "yyyy-MM-dd");
        String endTime = DateFormatUtils.format(endDate, "yyyy-MM-dd");
        String st = DateFormatUtils.format(startDate, "yyyyMMdd");
        String et = DateFormatUtils.format(endDate, "yyyyMMdd");


        for (Authenticate authenticate : authenticates) {
            mtCrawler.setAuthenticate(authenticate);
            mtCrawler.login();
            mtCrawler.bizDataReport(startTime, endTime, true);
            mtCrawler.bizDataReport(startTime, endTime, false);
            mtCrawler.businessStatistics(st, et, false);
            mtCrawler.flowanalysis("30", false);
            mtCrawler.hotSales(startTime, endTime, false);
            mtCrawler.comment(startTime, endTime, true);
            mtCrawler.historySettleBillList(startTime, endTime, true);
        }
    }

    public List<Authenticate> getAllUser() {
        log.info("开始获取美团所有商户信息");
        List<Authenticate> list = new ArrayList<>();
        List<User> users = userdao.getUsersForPlatform(Platform.MT);
        for (User user : users) {
            Authenticate authenticate = new Authenticate();
            authenticate.setUserName(user.getUserName());
            authenticate.setPassword(user.getPassWord());
            list.add(authenticate);
        }
        log.info("所有美团商户信息已经加载完成");
        return list;
    }
}
