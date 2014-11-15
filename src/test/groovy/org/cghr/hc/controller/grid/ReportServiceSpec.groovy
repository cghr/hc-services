package org.cghr.hc.controller.grid

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
 * Created by ravitej on 16/11/14.
 */

@ContextConfiguration(locations = "classpath:appContextTest.groovy", loader = GenericGroovyXmlContextLoader)
class ReportServiceSpec extends Specification {

    @Autowired
    ReportService reportService
    MockMvc mockMvc
    @Autowired
    Sql sql


    def setup() {

        mockMvc = MockMvcBuilders.standaloneSetup(reportService).build()
        new DbSetup(sql).setup()

    }

    def "should respond with status ok"() {

        expect:
        mockMvc.perform(get('/GridService/report/11'))
                .andExpect(status().isOk())

        mockMvc.perform(get('/GridService/report/12'))
                .andExpect(status().isOk())

    }

}