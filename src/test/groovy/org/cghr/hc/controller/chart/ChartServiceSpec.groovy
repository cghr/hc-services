package org.cghr.hc.controller.chart

import groovy.sql.Sql
import org.cghr.hc.controller.dbSetup.DbSetup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.GenericGroovyXmlContextLoader
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Created by ravitej on 15/11/14.
 */
@ContextConfiguration(locations = "classpath:appContextTest.groovy", loader = GenericGroovyXmlContextLoader)
class ChartServiceSpec extends Specification {

    @Autowired
    ChartService chartService
    @Autowired
    Sql gSql

    MockMvc mockMvc

    def setup() {

        mockMvc = MockMvcBuilders.standaloneSetup(chartService).build()
        new DbSetup(gSql).setup()
    }


    def "should respond with status ok"() {

        expect:
        mockMvc.perform(get('/chart/pendingDownloads'))
                .andExpect(status().isOk())

        mockMvc.perform(get('/chart/todayProgressHHQ'))
                .andExpect(status().isOk())

        mockMvc.perform(get('/chart/todayProgressEnum'))
                .andExpect(status().isOk())

    }

}