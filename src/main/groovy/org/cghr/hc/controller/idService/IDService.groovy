package org.cghr.hc.controller.idService

import org.cghr.commons.db.DbAccess
import org.cghr.commons.db.DbStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created by ravitej on 8/4/14.
 */

@RestController
@RequestMapping("/IDService/enum")
class IDService {

    @Autowired
    DbAccess dbAccess
    @Autowired
    DbStore dbStore

    Map contextConfig = [
            house    : [id: 'houseId', table: 'house', parentId: 'areaId', nextId: "001"],
            household: [id: "householdId", table: 'household', parentId: 'houseId', nextId: "01"],
            member   : [id: 'memberId', table: 'member', parentId: 'householdId', nextId: "01"],
            hosp     : [id: 'id', table: 'householdHosp', parentId: 'householdId', nextId: "01"],
            death    : [id: 'id', table: 'householdDeath', parentId: 'householdId', nextId: "01"],
            visit    : [id: 'id', table: 'householdDeath', parentId: 'householdId', nextId: "1"]
    ]


    @RequestMapping(value = "/area/{areaId}/house", produces = "application/json")
    String getNextHouseId(@CookieValue("userid") String userid, @PathVariable("areaId") String areaId) {

        getNextId(areaId, userid, "house")

    }

    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household", produces = "application/json")
    String getNextHouseholdId(@CookieValue("userid") String userid, @PathVariable("houseId") String houseId) {


        getNextId(houseId, userid, "household")


    }

    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household/{householdId}/visit", produces = "application/json")
    String getNextVisit(
            @CookieValue("userid") String userid,
            @PathVariable("householdId") String householdId,
            @PathVariable("houseId") String houseId, @PathVariable("areaId") String areaId) {

        int visits = dbAccess.firstRow("select count(*) count from enumVisit where householdId=?", [householdId]).count
        if (visits == 0)
            dbStore.execute("INSERT INTO household(householdId,houseId,areaId) values(?,?,?)", [householdId, houseId, areaId])

        getNextId(householdId, userid, "visit")

    }

    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household/{householdId}/member", produces = "application/json")

    String getNextMember(@CookieValue("userid") String userid,
                         @PathVariable("householdId") String householdId) {

        getNextId(householdId, userid, "member")

    }

    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household/{householdId}/death", produces = "application/json")
    String getNextDeath(@CookieValue("userid") String userid,
                        @PathVariable("householdId") String householdId) {

        getNextId(householdId, userid, "death")

    }

    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household/{householdId}/hosp", produces = "application/json")
    String getNextHosp(@CookieValue("userid") String userid,
                       @PathVariable("householdId") String householdId) {

        getNextId(householdId, userid, "hosp")
    }


    String getNextId(String refId, String userid, String context) {

        Map config = contextConfig.get(context)
        String sql = "SELECT MAX($config.id) FROM $config.table WHERE $config.parentId=?"
        generateNextId(sql, refId, userid, context)

    }

    String generateNextId(String sql, String refId, String userid, String context) {

        Map row = dbAccess.firstRow(sql, [refId])
        String nextId = resolveNextId(row, context, userid, refId)

        return [id: nextId].toJson()

    }

    String resolveNextId(Map row, String context, String userid, String refId) {

        if (row.isEmpty()) {
            String idPrefix = (context == 'house') ? (userid + refId) : refId
            return idPrefix + (contextConfig."$context".nextId)
        } else {
            Long id = (row.id).toLong()
            return (++id).toString()
        }
    }


}
