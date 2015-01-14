package org.cghr.hc.controller.grid
import org.cghr.commons.db.DbAccess
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
/**
 * Created by ravitej on 8/4/14.
 */
@RestController
@RequestMapping("/GridService/report")
class ReportService {


    @Autowired
    DbAccess dbAccess

    @RequestMapping(value = "/{reportId}", produces = "application/json")
    String getReport(@PathVariable("reportId") String reportId) {

        Map reports = ["11": "select * from area",
                       "12": "select id,username,role from user"]

        constructJsonResponse(reports."$reportId", [])
    }

    String constructJsonResponse(String sql, List params) {

        dbAccess.rows(sql,params).toJson()

    }


}
