package org.cghr.hc.client.service.grid
import com.github.jknack.handlebars.Handlebars
import org.cghr.commons.db.DbAccess
import org.cghr.dataViewModel.DataModelUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
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
        sql = "select $link,mobile1,mobile2 from household where houseId=?".toString()

        return constructJsonResponse(sql, [houseId])
    }

    //Head of the Household
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household/{householdId}/head", method = RequestMethod.GET, produces = "application/json")
    
    String getHead(@PathVariable("context") String context,
                   @PathVariable("areaId") Integer areaId,
                   @PathVariable("houseId") Integer houseId, @PathVariable("householdId") Integer householdId) {

        Map data = [nextState: context + '.memberDetail', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId]]
        String link = createLink(data)
        sql = "select $link,name,gender,age from member where  householdId=? and relationship='self'".toString()

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




        String link = createLink(data)
        sql = "select $link,name,gender,age from member where  householdId=?".toString()

        return constructJsonResponse(sql, [householdId])
    }
    //FFQ
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household/{householdId}/ffq", method = RequestMethod.GET, produces = "application/json")
    
    String getFFQ(@PathVariable("context") String context,
                  @PathVariable("areaId") Integer areaId,
                  @PathVariable("houseId") Integer houseId, @PathVariable("householdId") Integer householdId) {

        Map data = [nextState: context + '.ffqDetail.general', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId]]
        String link = createLink(data)
        sql = "select $link,name,gender,age from member where  householdId=?".toString()

        return constructJsonResponse(sql, [householdId])
    }

    // Visit
    @RequestMapping(value = "/{context}/area/{areaId}/house/{houseId}/household/{householdId}/visit", method = RequestMethod.GET, produces = "application/json")
    
    String getEnumVisits(@PathVariable("context") String context, @PathVariable("householdId") Integer householdId) {

        if (context == 'enum')
            sql = "select id,hhAvailability,time from enumVisit where householdId=? ".toString()
        else
            sql = "select id,hhVisit,time from hcVisit where householdId=? ".toString()
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


        def filtersArray = dbAccess.getColumnLabels(sql, params).split(",").collect {
            "#text_filter"
        }

        def sortingArray = dbAccess.getColumnLabels(sql, params).split(",").collect {
            "str"
        }

        return dataModelUtil.constructJsonResponse(sql, params, filtersArray.join(","), sortingArray.join(","));

    }

    String createLink(Map contextData) {

        Handlebars handlebars = new Handlebars()
        Map entities = contextData.refs
        List entityList = []
        entities.each { key, value ->

            entityList << "$key" + ":" + "$value".toString()

        }
        def refs = entityList.join(",")

        def template = ""
        if (entityList.isEmpty())
            template = "CAST(CONCAT('<a ui-sref=\"{{nextState}}({ {{entityId}}:',{{entityId}},'})\">',{{entityId}},'</a>') AS CHAR) id".toString()
        else
            template = "CAST(CONCAT('<a ui-sref=\"{{nextState}}({ {{entityId}}:',{{entityId}},',$refs })\">',{{entityId}},'</a>') AS CHAR) id".toString()

        def compiledTemplate = handlebars.compileInline(template)
        compiledTemplate.apply(contextData)


    }


}
