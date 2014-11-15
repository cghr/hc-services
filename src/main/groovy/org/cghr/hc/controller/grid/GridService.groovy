package org.cghr.hc.controller.grid

import com.github.jknack.handlebars.Handlebars
import com.google.gson.Gson
import org.cghr.commons.db.DbAccess
import org.cghr.dataViewModel.DataModelUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created by ravitej on 8/4/14.
 */

@RestController
@RequestMapping("/GridService")
class GridService {

    @Autowired
    DataModelUtil dataModelUtil
    @Autowired
    DbAccess dbAccess

    String sql = ""

    @RequestMapping(value = "/{context}/area", produces = "application/json")
    String getAreas(@PathVariable("context") String context) {

        Map data = [nextState: context + ".areaDetail.house", entityId: 'areaId', refs: [:]]
        String link = createLink(data)
        sql = "select $link,name,landmark,pincode from area".toString()

        return constructJsonResponse(sql, [])
    }

    @RequestMapping(value = "/{context}/area/{areaId}/house", produces = "application/json")
    String getHouses(@PathVariable("context") String context, @PathVariable("areaId") Integer areaId) {

        String nextState = (context == 'enum') ? 'basicInf' : 'household'
        Map data = [nextState: context + ".houseDetail.$nextState", entityId: 'houseId', refs: [areaId: areaId]]

        String link = createLink(data)
        sql = "select $link,houseNs,gps_latitude,gps_longitude from house where areaId=?".toString()

        return constructJsonResponse(sql, [areaId])
    }

    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household", produces = "application/json")
    String getHouseholds(
            @PathVariable("context") String context,
            @PathVariable("areaId") Integer areaId, @PathVariable("houseId") Integer houseId) {

        Map data = [:]
        if (context != 'resamp')
            data = [nextState: context + '.householdDetail.visit', entityId: 'householdId', refs: [areaId: areaId, houseId: houseId], alias: 'a.']
        else
            data = [nextState: context + '.householdDetail.member', entityId: 'householdId', refs: [areaId: areaId, houseId: houseId]]
        String link = createLink(data)

        Map row = dbAccess.firstRow("select hhAvailability from enumVisit ORDER by id DESC LIMIT 1")

        if (context == 'enum')
            sql = "SELECT  $link,totalMembers totalMembers,CASEWHEN(b.hhAvailability='Door temporarily locked','Revisit',b.hhAvailability) flag  FROM household a left JOIN (SELECT m1.householdId,m1.hhAvailability FROM enumVisit m1 LEFT JOIN enumVisit m2 ON (m1.householdId = m2.householdId AND m1.id < m2.id) WHERE m2.id IS NULL)b ON a.householdId=b.householdId where houseId=?".toString()

        else if (context == 'hc')
            sql = "SELECT  $link,totalMembers totalMembers,b.hhVisit flag  FROM household a left JOIN (SELECT m1.householdId,m1.hhVisit FROM hcVisit m1 LEFT JOIN hcVisit m2 ON (m1.householdId = m2.householdId AND m1.id < m2.id) WHERE m2.id IS NULL)b ON a.householdId=b.householdId where houseId=?".toString()
        //sql = "select $link,b.mobile1,b.mobile2 from household a left join hhContact b on a.householdId=b.householdId where houseId=?".toString()
        else
            sql = "select $link,totalMembers `Total Members`,CONCAT($visit,'') flag from household     where houseId=?".toString()

        return constructJsonResponse(sql, [houseId])
    }



    //Members
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household/{householdId}/member", produces = "application/json")
    String getMembers(@PathVariable("context") String context,
                      @PathVariable("areaId") Integer areaId,
                      @PathVariable("houseId") Integer houseId, @PathVariable("householdId") String householdId) {

        Map data = [:]
        if (context != 'resamp')
            data = [nextState: context + '.memberDetail.bp1', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId], alias: 'a.']
        else
            data = [nextState: context + '.memberDetail.basicInf', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId]]

        String consentPhoto = createLink([columnName: 'consent', nextState: 'cam', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId, category: 'memberConsent', imgSuffix: 'consent']])
        String memberPhoto = createLink([columnName: 'photo', nextState: 'cam', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId, category: 'memberPhoto', imgSuffix: 'photo']])


        println 'before link creation'
        String link = createLink(data)
        println 'link created ' + link

        if (context == 'hc')
        //sql = "select $link,name,gender,age,CAST(CONCAT('<a ui-sref=\"cam({ memberId:',memberId,',areaId:$areaId,houseId:$houseId,householdId:$householdId,category:',',,'memberConsent,','imgSuffix:','consent})\">consent</a>') AS CHAR) consent from member where  householdId=? and age>29 and age<71".toString()
            sql = "select $link,name,gender,CONCAT(age_value,age_unit) age,CASEWHEN(b.memberId IS NULL,'Pending','<i class=\"icon icon-ok\"></i>') status from member a left join invitationCard b on a.memberId=b.memberId where  householdId=? and age_value>29 and age_value<71 and age_unit='Years'".toString()
        else if (context == 'resamp')
            sql = "select $link,name,gender,CONCAT(age_value,age_unit) age from member where  householdId=? and age_value>29 and age_value<71 and age_unit='Years'".toString()
        else
            sql = "select memberId,name,gender,CONCAT(age_value,age_unit) age from member where  householdId=?".toString()

        println 'before generating response'
        return constructJsonResponse(sql, [householdId])
    }
    //FFQ
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household/{householdId}/ffq", produces = "application/json")

    String getFFQ(@PathVariable("context") String context,
                  @PathVariable("areaId") Integer areaId,
                  @PathVariable("houseId") Integer houseId, @PathVariable("householdId") String householdId) {

        Map data = [nextState: context + '.ffqDetail.general', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId]]
        String link = createLink(data)
        sql = "select $link,name,gender,concat(age_value,age_unit) age from member where  householdId=? and age_value>29 and age_value<71 and age_unit='Years'".toString()

        return constructJsonResponse(sql, [householdId])
    }

    // Visit
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household/{householdId}/visit", produces = "application/json")

    String getEnumVisits(@PathVariable("context") String context, @PathVariable("householdId") String householdId) {

        if (context == 'enum')
            sql = "select id,hhAvailability,timelog from enumVisit where householdId=? ".toString()
        else
            sql = "select id,hhVisit,timelog from hcVisit where householdId=? ".toString()
        return constructJsonResponse(sql, [householdId])
    }

    //Household Deaths
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household/{householdId}/death", produces = "application/json")

    String getDeaths(@PathVariable("context") String context, @PathVariable("householdId") String householdId) {

        Map data = [nextState: '', entityId: '']
        String link = createLink(data)
        sql = "select name,age_value,gender from householdDeath where householdId=?".toString()

        return constructJsonResponse(sql, [householdId])
    }

    //Household Hospitalization
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household/{householdId}/hosp", produces = "application/json")

    String getHospitalization(
            @PathVariable("context") String context, @PathVariable("householdId") String householdId) {

        sql = "select name,reason from householdHosp where householdId=?".toString()

        return constructJsonResponse(sql, [householdId])
    }



    // Creating a Json from sql Query
    String constructJsonResponse(String sql, List params) {

        List cols = dbAccess.columns(sql, params)
        List filters = cols.collect { '#text_filter' }
        List sortings = cols.collect { 'str' }

        return dataModelUtil.constructJsonResponse(sql, params, filters.join(","), sortings.join(","));

    }

    String createLink(Map contextData) {

        Handlebars handlebars = new Handlebars()
        Gson gson = new Gson()

        Map entities = contextData.refs
        String columnName = contextData.columnName == null ? 'id' : contextData.columnName
        List entityList = []
        entities.each { k, v ->
            entityList << "$k" + ":" + "$v".toString()
        }
        String refs = entityList.join(",")

        String template = ""
        if (refs.isEmpty())
            template = "CAST(CONCAT('<a ui-sref=\"{{nextState}}({ {{entityId}}:',{{alias}}{{entityId}},'})\">',{{alias}}{{entityId}},'</a>') AS CHAR) $columnName"
        else
            template = "CAST(CONCAT('<a ui-sref=\"{{nextState}}({ {{entityId}}:',{{alias}}{{entityId}},',$refs })\">',{{alias}}{{entityId}},'</a>') AS CHAR) $columnName"

        def compiledTemplate = handlebars.compileInline(template)
        compiledTemplate.apply(contextData)


    }


}
