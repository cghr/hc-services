package org.cghr.hc.controller.resampling

import groovy.sql.Sql
import org.cghr.commons.db.DbAccess
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * Created by ravitej on 13/5/14.
 */
@RestController
@RequestMapping("/resampling")
class Resampling {

    @Autowired
    DbAccess dbAccess
    @Autowired
    Sql gSql


    @RequestMapping(value = "", method = RequestMethod.GET)
    String assignResampling() {

        List list = dbAccess.getRowsAsListOfMaps("select distinct user from hcMember", [])
        List listOfUsers = list.collect {
            it.user
        }
        Collections.shuffle(listOfUsers)
        if (listOfUsers.size() % 2 != 0) //Make array even
            listOfUsers.remove(listOfUsers.size() - 1)
        List chunks = listOfUsers.collate(2)

        chunks.each {
            chunk ->
                gSql.execute("insert into resampAssign(memberId,user) select memberId,${chunk[1]} from hcMember where user=? LIMIT 2", [chunk[0]])
                gSql.execute("insert into resampAssign(memberId,user) select memberId,${chunk[0]} from hcMember where user=? LIMIT 2", [chunk[1]])

        }
        listOfUsers.each {
            String recipient=it;
            String sql="insert into outbox(datastore,ref,refId,recipient) values('resampAssign','user',$recipient)"
        }

    }


}
