package org.cghr.hc.controller.dashboard

import org.cghr.commons.db.DbAccess
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import java.text.SimpleDateFormat

/**
 * Created by ravitej on 12/5/14.
 */
@RestController
@RequestMapping("/dashboard")
class DashboardService {

    @Autowired
    DbAccess dbAccess


    @RequestMapping("/downloads")
    List getPendingDownloads() {

        getChartModel("SELECT  username,COUNT(*)  downloads FROM USER  a  JOIN   outbox b ON a.id=b.recipient  WHERE role='user'  AND dwnStatus IS NULL GROUP BY  b.recipient")
    }

    @RequestMapping("/hhq")
    List getTodayProgressHHQ() {


        getChartModel("SELECT username NAME,COUNT(*) households FROM  USER a JOIN  hhContact b ON a.id=LEFT(householdId,2)  WHERE timelog LIKE $today GROUP BY LEFT(householdId,2) ")
    }

    @RequestMapping("/enum")
    List getTotalProgressEnum() {

        getChartModel("SELECT username NAME,COUNT(*) households FROM  USER a JOIN  invitationCard b ON a.id=LEFT(memberId,2)  WHERE timelog LIKE $today GROUP BY LEFT(memberId,2) ")

    }

    List getChartModel(String sql, List params = []) {

        dbAccess.rows(sql, params)
    }
    String getToday(){

        new SimpleDateFormat("yyyy-MM-dd").format(new Date())+"%"
    }
}
