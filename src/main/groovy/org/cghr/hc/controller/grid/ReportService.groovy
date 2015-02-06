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

    @RequestMapping("/{reportId}")
    List getReport(@PathVariable("reportId") String reportId) {

        Map reports = [
                "11": "select * from area",
                "12": "select * from house",
                "13": "select * from household",
                "14": "select * from member",
                "21": "select * from hcMember"]

        constructJsonResponse(reports[reportId], [])
    }

    List constructJsonResponse(String sql, List params = []) {

        //String forArea = sql + " where areaId=?"
        dbAccess.rows(sql, params)

    }


}
