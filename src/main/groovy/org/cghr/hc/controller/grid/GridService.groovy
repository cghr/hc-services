package org.cghr.hc.controller.grid

import com.github.jknack.handlebars.Handlebars
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
@RequestMapping("/GridService")
class GridService {

    @Autowired
    DataModelUtil dataModelUtil
    @Autowired
    DbAccess dbAccess

    def sql = ""

    //Areas
    @RequestMapping(value = "/{context}/area", method = RequestMethod.GET, produces = "application/json")
    String getAreas(@PathVariable("context") String context) {

        Map data = [nextState: context + ".areaDetail.house", entityId: 'areaId', refs: [:]]
        String link = createLink(data)
        sql = "select $link,name,landmark,pincode from area".toString()

        return constructJsonResponse(sql, [])
    }

    //Houses
    @RequestMapping(value = "/{context}/area/{areaId}/house", method = RequestMethod.GET, produces = "application/json")
    String getHouses(@PathVariable("context") String context, @PathVariable("areaId") Integer areaId) {

        Map data

        if (context == 'enum')
            data = [nextState: context + '.houseDetail.basicInf', entityId: 'houseId', refs: [areaId: areaId]]
        else
            data = [nextState: context + '.houseDetail.household', entityId: 'houseId', refs: [areaId: areaId]]


        String link = createLink(data)
        sql = "select $link,houseNs,gps_latitude,gps_longitude from house where areaId=?".toString()

        return constructJsonResponse(sql, [areaId])
    }

    //Households
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household", method = RequestMethod.GET, produces = "application/json")

    String getHouseholds(
            @PathVariable("context") String context,
            @PathVariable("areaId") Integer areaId, @PathVariable("houseId") Integer houseId) {

        Map data = [:]
        if (context != 'resamp')
            data = [nextState: context + '.householdDetail.visit', entityId: 'householdId', refs: [areaId: areaId, houseId: houseId]]
        else
            data = [nextState: context + '.householdDetail.member', entityId: 'householdId', refs: [areaId: areaId, houseId: houseId]]
        String link = createLink(data)

        //sql = "select $link,b.mobile1,b.mobile2 from household a left join hhContact b on a.householdId=b.householdId where houseId=?".toString()
        sql = "select $link,totalMembers `Total Members` from household    where houseId=?".toString()

        return constructJsonResponse(sql, [houseId])
    }

    //Head of the Household
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household/{householdId}/head", method = RequestMethod.GET, produces = "application/json")

    String getHead(@PathVariable("context") String context,
                   @PathVariable("areaId") Integer areaId,
                   @PathVariable("houseId") Integer houseId, @PathVariable("householdId") Integer householdId) {

        Map data = [nextState: context + '.memberDetail', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId]]
        String link = createLink(data)
        sql = "select $link,name,gender,CONCAT(age_value,age_unit)age from member where  householdId=? and relationship='self'".toString()

        return constructJsonResponse(sql, [householdId])
    }

    //Members
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household/{householdId}/member", method = RequestMethod.GET, produces = "application/json")

    String getMembers(@PathVariable("context") String context,
                      @PathVariable("areaId") Integer areaId,
                      @PathVariable("houseId") Integer houseId, @PathVariable("householdId") Integer householdId) {

        Map data = [:]
        if (context != 'resamp')
            data = [nextState: context + '.memberDetail.basicInf', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId]]
        else
            data = [nextState: context + '.memberDetail.basicInf', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId]]

        String consentPhoto = createLink([columnName: 'consent', nextState: 'cam', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId, category: 'memberConsent', imgSuffix: 'consent']])
        String memberPhoto = createLink([columnName: 'photo', nextState: 'cam', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId, category: 'memberPhoto', imgSuffix: 'photo']])


        String link = createLink(data)

        if (context == 'hc')
        //sql = "select $link,name,gender,age,CAST(CONCAT('<a ui-sref=\"cam({ memberId:',memberId,',areaId:$areaId,houseId:$houseId,householdId:$householdId,category:',',,'memberConsent,','imgSuffix:','consent})\">consent</a>') AS CHAR) consent from member where  householdId=? and age>29 and age<71".toString()
            sql = "select $link,name,gender,CONCAT(age_value,age_unit) age,CAST(CONCAT('<a href=\"#/hc/area/$areaId/house/$houseId/household/$householdId/member/',memberId,'/cam/memberConsent/consent\">','Consent</a>') AS CHAR) consent,CAST(CONCAT('<a href=\"#/hc/area/$areaId/house/$houseId/household/$householdId/member/',memberId,'/cam/memberPhoto/photo\">','Photo</a>') AS CHAR) photo  from member where  householdId=? and age_value>29 and age_value<71 and age_unit='Years'".toString()
        else if (context == 'resamp')
            sql = "select $link,name,gender,CONCAT(age_value,age_unit) age from member where  householdId=? and age_value>29 and age_value<71 and age_unit='Years'".toString()
        else
            sql = "select $link,name,gender,CONCAT(age_value,age_unit) age from member where  householdId=?".toString()

        return constructJsonResponse(sql, [householdId])
    }
    //FFQ
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household/{householdId}/ffq", method = RequestMethod.GET, produces = "application/json")

    String getFFQ(@PathVariable("context") String context,
                  @PathVariable("areaId") Integer areaId,
                  @PathVariable("houseId") Integer houseId, @PathVariable("householdId") Integer householdId) {

        Map data = [nextState: context + '.ffqDetail.general', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId]]
        String link = createLink(data)
        sql = "select $link,name,gender,concat(age_value,age_unit) age from member where  householdId=? and age_value>29 and age_value<71 and age_unit='Years'".toString()

        return constructJsonResponse(sql, [householdId])
    }

    // Visit
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household/{householdId}/visit", method = RequestMethod.GET, produces = "application/json")

    String getEnumVisits(@PathVariable("context") String context, @PathVariable("householdId") Integer householdId) {

        if (context == 'enum')
            sql = "select id,hhAvailability,timelog from enumVisit where householdId=? ".toString()
        else
            sql = "select id,hhVisit,timelog from hcVisit where householdId=? ".toString()
        return constructJsonResponse(sql, [householdId])
    }

    //Household Deaths
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household/{householdId}/death", method = RequestMethod.GET, produces = "application/json")

    String getDeaths(@PathVariable("context") String context, @PathVariable("householdId") Integer householdId) {

        Map data = [nextState: '', entityId: '']
        String link = createLink(data)
        sql = "select name,age_value,gender from householdDeath where householdId=?".toString()

        return constructJsonResponse(sql, [householdId])
    }

    //Household Hospitalization
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household/{householdId}/hosp", method = RequestMethod.GET, produces = "application/json")

    String getHospitalization(
            @PathVariable("context") String context, @PathVariable("householdId") Integer householdId) {

        sql = "select name,reason from householdHosp where householdId=?".toString()

        return constructJsonResponse(sql, [householdId])
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")

    String get() {

        Map data = [nextState: '', entityId: '']
        String link = createLink(data)
        sql = "select $link from ".toString()

        return constructJsonResponse(sql, [])
    }

    // Creating a Json from sql Query
    String constructJsonResponse(String sql, List params) {


        def filtersArray = dbAccess.columns(sql, params).collect {
            "#text_filter"
        }

        def sortingArray = dbAccess.columns(sql, params).collect {
            "str"
        }

        return dataModelUtil.constructJsonResponse(sql, params, filtersArray.join(","), sortingArray.join(","));

    }

    String createLink(Map contextData) {

        Handlebars handlebars = new Handlebars()
        Map entities = contextData.refs
        String columnName = contextData.columnName == null ? 'id' : contextData.columnName
        List entityList = []
        entities.each { key, value ->

            entityList << "$key" + ":" + "$value".toString()

        }
        def refs = entityList.join(",")

        def template = ""
        if (entityList.isEmpty())
            template = "CAST(CONCAT('<a ui-sref=\"{{nextState}}({ {{entityId}}:',{{alias}}{{entityId}},'})\">',{{alias}}{{entityId}},'</a>') AS CHAR) $columnName".toString()
        else
            template = "CAST(CONCAT('<a ui-sref=\"{{nextState}}({ {{entityId}}:',{{alias}}{{entityId}},',$refs })\">',{{alias}}{{entityId}},'</a>') AS CHAR) $columnName".toString()

        def compiledTemplate = handlebars.compileInline(template)
        compiledTemplate.apply(contextData)


    }


}
