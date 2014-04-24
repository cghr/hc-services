package org.cghr.hc.client.service.idService

import groovy.sql.Sql
import org.json.simple.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by ravitej on 8/4/14.
 */

@Controller
@RequestMapping("/IDService/enum")
class IDService {

    def sql = ""
    final def nextHouse = "001"
    final def nextHousehold = "01"
    final def nextMember = "01"
    final def nextDeath = "01"
    final def nextHosp = "01"
    final def nextVisit = "1"
    def idPrefix = ""
    JSONObject idObject = new JSONObject();

    @Autowired
    Sql gSql


    String generateNextId(String sql, List params, HttpServletRequest request, String context) {

        Map row = gSql.firstRow(sql, params);
        Long id = (row.id==null) ? 0 : row.id;
        println 'id '+id;
        def nextId = ""

        if (id == 0) {

            if (context == 'house') idPrefix = getUserIdFromRequest(request) + params[0]
            else idPrefix = params[0] + ""

            nextId = constructIdFromRequest(request, context, idPrefix);
        } else
            nextId = (++id) + "";

        idObject.put("id", nextId);

        return idObject.toJSONString();

    }

    @RequestMapping(value = "/area/{areaId}/house", produces = "application/json")
    @ResponseBody
    String getNextHouseId(HttpServletRequest request, HttpServletResponse response,
                          @PathVariable("areaId") long areaId) {

        sql = "SELECT MAX(houseId) id FROM house WHERE areaId=?";
        generateNextId(sql, [areaId], request, "house")

    }

    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household", produces = "application/json")
    @ResponseBody
    String getNextHouseholdId(HttpServletRequest request, HttpServletResponse response,
                              @PathVariable("houseId") long houseId) {

        sql = "SELECT MAX(householdId) id FROM household WHERE  houseId=?";
        generateNextId(sql, [houseId], request, "household")


    }

    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household/{householdId}/visit", produces = "application/json")
    @ResponseBody
    String getNextVisit(HttpServletRequest request, HttpServletResponse response,
                        @PathVariable("householdId") long householdId) {

        //Save Id of the Household if doesn't exists
        //int householdExists=jdbcTemplate.queryForObject("select count(*) from visit where householdId=?",new Object[]{householdId},Integer.class);
        int householdExists = gSql.firstRow("select count(*) count from enumVisit where householdId=?", [householdId]).count

        if (householdExists == 0)
            gSql.execute("INSERT INTO household(householdId) values(?)", [householdId])

        sql = "SELECT MAX(id) id FROM enumVisit WHERE householdId=? ";
        generateNextId(sql, [householdId], request, "visit")

    }

    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household/{householdId}/member", produces = "application/json")
    @ResponseBody
    String getNextMember(HttpServletRequest request, HttpServletResponse response,
                         @PathVariable("householdId") long householdId) {

        sql = "SELECT MAX(memberId) id FROM member WHERE householdId=?";
        generateNextId(sql, [householdId], request, "member")

    }

    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household/{householdId}/death", produces = "application/json")
    @ResponseBody
    String getNextDeath(HttpServletRequest request, HttpServletResponse response,
                        @PathVariable("householdId") long householdId) {

        sql = "SELECT MAX(id) id FROM householdDeath WHERE householdId=? ";
        generateNextId(sql, [householdId], request, "death")

    }

    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household/{householdId}/hosp", produces = "application/json")
    @ResponseBody
    String getNextHosp(HttpServletRequest request, HttpServletResponse response,
                       @PathVariable("householdId") long householdId) {

        sql = "SELECT MAX(id) id FROM householdHosp WHERE householdId=?";
        generateNextId(sql, [householdId], request, "hosp")
    }

    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household/{householdId}/head", produces = "application/json")
    @ResponseBody
    String getNextHead(HttpServletRequest request, HttpServletResponse response,
                       @PathVariable("householdId") long householdId) {

        sql = "SELECT MAX(memberId) id FROM member WHERE householdId=? ";
        generateNextId(sql, [householdId], request, "member")

    }


    String getUserIdFromRequest(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("userid"))
                return cookie.getValue();

        }



        return null;
    }

    String constructIdFromRequest(HttpServletRequest request, String type, String idPrefix) {


        def nexItem = ""
        switch (type) {

            case "house":
                nexItem = nextHouse;
                break;

            case "household":
                nexItem = nextHousehold;
                break;

            case "member":
                nexItem = nextMember;
                break;

            case "hosp":
                nexItem = nextHosp;
                break;

            case "death":
                nexItem = nextDeath
                break;

            case "visit":
                nexItem = nextVisit
                break;

            default:
                return null;
                break;

        }
        return idPrefix + nexItem


    }
}
