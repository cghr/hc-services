package org.cghr.hc.controller.grid

import org.cghr.commons.db.DbAccess
import org.cghr.dataViewModel.DataModelUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * Created by ravitej on 8/4/14.
 */
@RestController
@RequestMapping("/GridService/report")
class ReportService {


    @Autowired
    DataModelUtil dataModelUtil
    @Autowired
    DbAccess dbAccess


    def sql = ""

    @RequestMapping(value = "/{reportId}", method = RequestMethod.GET, produces = "application/json")
    String getReport(@PathVariable("reportId") int reportId) {

        switch (reportId) {

            case 11:
                sql = "select * from area";
                break;
            case 12:
                sql = "select id,username,role from user";

            default:
                break;


        }

        constructJsonResponse(sql, [])

    }
    // Creating a Json from sql Query
    String constructJsonResponse(String sql, List params) {


        def filtersArray = dbAccess.getColumnLabels(sql, params).collect {
            '#text_filter'
        }
        def sortingArray = dbAccess.getColumnLabels(sql, params).collect {
            'str'
        }

        return dataModelUtil.constructJsonResponse(sql, params, filtersArray.join(","), sortingArray.join(","));

    }


}
