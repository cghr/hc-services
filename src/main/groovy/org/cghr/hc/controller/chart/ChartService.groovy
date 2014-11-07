package org.cghr.hc.controller.chart

import org.cghr.chart.ChartDataModel
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
    ChartDataModel chartDataModel

    @RequestMapping(value = "/pendingDownloads", produces = "application/json")
    String getPendingDownloads() {

        getChartModel("SELECT username,COUNT(*) downloads FROM outbox  a LEFT JOIN user b ON a.recipient=b.id WHERE dwnStatus is null GROUP BY recipient", [])
    }

    @RequestMapping(value = "/todayProgressHHQ", produces = "application/json")
    String getTodayProgressHHQ() {

        getChartModel("SELECT  username,COUNT(*)  completed FROM hcMember a LEFT JOIN user b ON a.surveyor=b.id WHERE a.timelog  > CURRENT_DATE() GROUP BY a.surveyor", [])
    }

    @RequestMapping(value = "/todayProgressEnum", produces = "application/json")
    String getTotalProgressEnum() {
        getChartModel("SELECT  username,COUNT(*)  completed FROM member a LEFT JOIN user b ON a.surveyor=b.id WHERE  a.timelog > CURRENT_DATE() GROUP BY a.surveyor", [])

    }

    String getChartModel(String sql, List params) {
        chartDataModel.getChartDataModel(sql, params)
    }
}
