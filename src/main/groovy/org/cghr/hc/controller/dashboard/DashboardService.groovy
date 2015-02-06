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

        getData("SELECT  username,COUNT(*)  downloads FROM USER  a  JOIN   outbox b ON a.id=b.recipient  WHERE role='user'  AND dwnStatus IS NULL GROUP BY  b.recipient")
    }

    @RequestMapping("/enum")
    List getTotalProgressEnum() {

        getData("select username name,count(*) households from user a join hhContact b on a.id=b.surveyor where b.timelog like '$today' group by b.surveyor")
        //getData("select username name,d.households from user c join  ( select b.surveyor,count(*) households  from hhContact a join household b on a.householdId=b.householdId where  a.timelog like '$today' group by b.surveyor ) d on c.id=d.surveyor ")

    }

    @RequestMapping("/hhq")
    List getTodayProgressHHQ() {

        getData("select username name,count(*) households from user a join invitationCard b on a.id=b.surveyor where b.timelog like '$today' group by b.surveyor")
        //getData("select username name,d.surveys from user c join  ( select b.surveyor,count(*) surveys  from invitationCard a join hcMember b on a.memberId=b.memberId where  a.timelog like '$today' group by b.surveyor ) d on c.id=d.surveyor  ")
    }

    List getData(String sql, List params = []) {

        dbAccess.rows(sql, params)
    }
    String getToday(){

        new SimpleDateFormat("yyyy-MM-dd").format(new Date())+"%"
    }
}
