package org.cghr.hc.controller.chart

import org.cghr.commons.db.DbAccess
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created by ravitej on 12/5/14.
 */
@RestController
@RequestMapping("/chart")
class ChartService {

    @Autowired
    DbAccess dbAccess

    @RequestMapping("/pendingDownloads")
    Map[] getPendingDownloads() {

        
        getChartModel("SELECT username,COUNT(*) downloads FROM outbox  a LEFT JOIN user b ON a.recipient=b.id WHERE dwnStatus is null GROUP BY recipient")
    }

    @RequestMapping("/todayProgressHHQ")
    Map[] getTodayProgressHHQ() {

        getChartModel("SELECT  username,COUNT(*)  completed FROM hcMember a LEFT JOIN user b ON a.surveyor=b.id WHERE a.timelog  > CURRENT_DATE() GROUP BY a.surveyor")
    }

    @RequestMapping("/todayProgressEnum")
    Map[] getTotalProgressEnum() {
        getChartModel("SELECT  username,COUNT(*)  completed FROM member a LEFT JOIN user b ON a.surveyor=b.id WHERE  a.timelog > CURRENT_DATE() GROUP BY a.surveyor")

    }

    Map[] getChartModel(String sql, List params = []) {

        dbAccess.rows(sql, params)
    }
}
