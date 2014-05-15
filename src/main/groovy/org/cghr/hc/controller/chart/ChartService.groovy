package org.cghr.hc.controller.chart

import org.cghr.chart.ChartDataModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
/**
 * Created by ravitej on 12/5/14.
 */
@RestController
@RequestMapping("/chart")
class ChartService {

    @Autowired
    ChartDataModel chartDataModel

    @RequestMapping(value="/pendingDownloads",method = RequestMethod.GET,produces = 'application/json')
    String getPendingDownloads(){

        chartDataModel.getChartDataModel("SELECT username,COUNT(*) downloads FROM outbox  a LEFT JOIN user b ON a.recipient=b.id GROUP BY recipient;",[])
    }
    @RequestMapping(value="/totalProgress",method = RequestMethod.GET,produces = 'application/json')
    String getTotalProgress(){

        chartDataModel.getChartDataModel("SELECT  username,COUNT(*)  completed FROM hcMember a LEFT JOIN user b ON a.user=b.id GROUP BY a.user",[])


    }

}
