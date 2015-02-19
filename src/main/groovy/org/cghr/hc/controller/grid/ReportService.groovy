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
                "12": "select houseId,areaId,cast(timelog as varchar) timelog,surveyor,houseNs,gps_latitude,gps_longitude from house",
                "13": "select householdId,houseId,areaId,cast(timelog as varchar) timelog,surveyor,religion,totalMembers from household",
                "14": "select householdId,houseId,areaId,cast(timelog as varchar) timelog,surveyor,name,age_value,age_unit,gender,head from member",
                "21": "select * from hcMember",
                "31":"select id,username,role from user"]

        constructJsonResponse(reports[reportId], [])
    }

    List constructJsonResponse(String sql, List params = []) {

        //String forArea = sql + " where areaId=?"
        dbAccess.rows(sql, params)

    }


}
