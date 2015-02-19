package org.cghr.hc.controller.grid
import groovy.text.SimpleTemplateEngine
import org.cghr.commons.db.DbAccess
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
    DbAccess dbAccess

    String sql = ""

    @RequestMapping("/{context}/area")
    List getAreas(@PathVariable("context") String context) {

        Map data = [nextState: context + ".areaDetail.house", entityId: 'areaId', refs: [:]]
        String link = createLink(data)
        sql = "select $link,name,landmark,pincode,CONCAT('<a target=_new href=assets/maps/',areaId,'.jpg>Map</a>') map from area"

        constructJsonResponse(sql,[])
    }

    @RequestMapping("/{context}/area/{areaId}/house")
    List getHouses(@PathVariable("context") String context, @PathVariable("areaId") Integer areaId) {

        String nextState = (context == 'enum') ? 'basicInf' : 'household'
        Map data = [nextState: context + ".houseDetail.$nextState", entityId: 'houseId', refs: [areaId: areaId],alias: 'a.']

        String link = createLink(data)
        //sql = "select $link,houseNs,b.households,gps_latitude,gps_longitude from house a left join(select   houseId,count(*)  households  from household group by houseId) b on a.houseId=b.houseId where a.areaId=?".toString()

        sql="SELECT $link,houseNs,b.flag,gps_latitude,gps_longitude FROM house a LEFT JOIN " +
                "(SELECT houseId,CASEWHEN(hhAvailability LIKE '%temporarily locked%','Revisit','')  flag FROM  (SELECT  p.houseId,GROUP_CONCAT(p.hhAvailability) hhAvailability  FROM enumVisit p " +
                "LEFT JOIN enumVisit q ON p.householdId=q.householdId AND p.id<q.id WHERE q.id IS NULL  GROUP BY p.houseId ) tab1 ) b ON a.houseId=b.houseId  WHERE a.areaId=?"
        return constructJsonResponse(sql, [areaId])
    }

    @RequestMapping("/{context}/area/{areaId}/house/{houseId}/household")
    List getHouseholds(
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
            //sql = "SELECT  $link,totalMembers totalMembers,CASEWHEN(b.hhAvailability='Door temporarily locked','Revisit',b.hhAvailability) flag  FROM household a left JOIN (SELECT m1.householdId,m1.hhAvailability FROM enumVisit m1 LEFT JOIN enumVisit m2 ON (m1.householdId = m2.householdId AND m1.id < m2.id) WHERE m2.id IS NULL)b ON a.householdId=b.householdId where houseId=?".toString()
            sql="SELECT  $link,totalMembers totalMembers,head,CASEWHEN(b.hhAvailability='Door temporarily locked','Revisit',b.hhAvailability) flag  FROM household a left JOIN (SELECT m1.householdId,m1.hhAvailability FROM enumVisit m1 LEFT JOIN enumVisit m2 ON (m1.householdId = m2.householdId AND m1.id < m2.id) WHERE m2.id IS NULL)b ON a.householdId=b.householdId left join (select name head,householdId from member where head='Yes' ) c on a.householdId=c.householdId where houseId=?"

        else if (context == 'hc')
            sql = "SELECT  $link,totalMembers totalMembers,b.hhVisit flag  FROM household a left JOIN (SELECT m1.householdId,m1.hhVisit FROM hcVisit m1 LEFT JOIN hcVisit m2 ON (m1.householdId = m2.householdId AND m1.id < m2.id) WHERE m2.id IS NULL)b ON a.householdId=b.householdId where houseId=?".toString()
        //sql = "select $link,b.mobile1,b.mobile2 from household a left join hhContact b on a.householdId=b.householdId where houseId=?".toString()
        else
            sql = "select $link,totalMembers `Total Members`,CONCAT($visit,'') flag from household     where houseId=?".toString()

        return constructJsonResponse(sql, [houseId])
    }

    //Members
    @RequestMapping("/{context}/area/{areaId}/house/{houseId}/household/{householdId}/member")
    List getMembers(@PathVariable("context") String context,
                     @PathVariable("areaId") Integer areaId,
                     @PathVariable("houseId") Integer houseId, @PathVariable("householdId") String householdId) {

        Map data = [:]
        if (context == 'hc')
            data = [nextState: context + '.memberDetail.bp1', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId], alias: 'a.']
        else
            data = [nextState: context + '.memberDetail.basicInf', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId]]

        String consentPhoto = createLink([columnName: 'consent', nextState: 'cam', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId, category: 'memberConsent', imgSuffix: 'consent']])
        String memberPhoto = createLink([columnName: 'photo', nextState: 'cam', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId, category: 'memberPhoto', imgSuffix: 'photo']])


        String link = createLink(data)

        if (context == 'hc')
        //sql = "select $link,name,gender,age,CAST(CONCAT('<a ui-sref=\"cam({ memberId:',memberId,',areaId:$areaId,houseId:$houseId,householdId:$householdId,category:',',,'memberConsent,','imgSuffix:','consent})\">consent</a>') AS CHAR) consent from member where  householdId=? and age>29 and age<71".toString()
            sql = "select $link,name,gender,CONCAT(age_value,age_unit) age,CASEWHEN(b.memberId IS NULL,'Pending','Completed') status from member a left join invitationCard b on a.memberId=b.memberId where  householdId=? and age_value>29 and age_value<71 and age_unit='Years'".toString()
        else if (context == 'resamp')
            sql = "select $link,name,gender,CONCAT(age_value,age_unit) age from member a where  householdId=? and age_value>29 and age_value<71 and age_unit='Years'".toString()
        else
            sql = "select $link,name,gender,CONCAT(age_value,age_unit) age from member a where  householdId=?".toString()

        return constructJsonResponse(sql, [householdId])
    }
    //FFQ
    @RequestMapping("/{context}/area/{areaId}/house/{houseId}/household/{householdId}/ffq")

    List getFFQ(@PathVariable("context") String context,
                 @PathVariable("areaId") Integer areaId,
                 @PathVariable("houseId") Integer houseId, @PathVariable("householdId") String householdId) {

        Map data = [nextState: context + '.ffqDetail.general', entityId: 'memberId', refs: [areaId: areaId, houseId: houseId, householdId: householdId]]
        String link = createLink(data)
        sql = "select $link,name,gender,concat(age_value,age_unit) age from member where  householdId=? and age_value>29 and age_value<71 and age_unit='Years'".toString()

        return constructJsonResponse(sql, [householdId])
    }

    // Visit
    @RequestMapping("/{context}/area/{areaId}/house/{houseId}/household/{householdId}/visit")

    List getEnumVisits(@PathVariable("context") String context, @PathVariable("householdId") String householdId) {

        if (context == 'enum')
            sql = "select id,hhAvailability,timelog from enumVisit where householdId=? ".toString()
        else
            sql = "select id,hhVisit,timelog from hcVisit where householdId=? ".toString()
        return constructJsonResponse(sql, [householdId])
    }

    //Household Deaths
    @RequestMapping("/{context}/area/{areaId}/house/{houseId}/household/{householdId}/death")

    List getDeaths(@PathVariable("context") String context, @PathVariable("householdId") String householdId) {

        Map data = [nextState: '', entityId: '']
        String link = createLink(data)
        sql = "select name,age_value,gender from householdDeath where householdId=?".toString()

        return constructJsonResponse(sql, [householdId])
    }

    //Household Hospitalization
    @RequestMapping("/{context}/area/{areaId}/house/{houseId}/household/{householdId}/hosp")
    List getHospitalization(
            @PathVariable("context") String context, @PathVariable("householdId") String householdId) {

        sql = "select name,reason from householdHosp where householdId=?".toString()

        return constructJsonResponse(sql, [householdId])
    }

    // Creating a Json from sql Query
    List constructJsonResponse(String sql, List params) {
        dbAccess.rows(sql, params)
    }

    String createLink(Map contextData) {


        Map entities = contextData.refs
        String columnName = contextData.columnName == null ? 'id' : contextData.columnName
        List entityList = []
        entities.each { k, v ->
            entityList << "$k" + ":" + "$v".toString()
        }
        String refs = entityList.join(",")

        String text = ""

        Map bindingData = contextData.clone()
        bindingData << [columnName: columnName]
        bindingData << [refs: refs]
        if (!bindingData.alias)
            bindingData << [alias: '']

        if (refs.isEmpty())
            text = '''CAST(CONCAT('<a ui-sref=\"$nextState({ $entityId:',$alias$entityId,'})\">',$alias$entityId,'</a>') AS VARCHAR) $columnName'''
        //text = "CAST(CONCAT('<a ui-sref=\"{{nextState}}({ {{entityId}}:',{{alias}}{{entityId}},'})\">',{{alias}}{{entityId}},'</a>') AS CHAR) $columnName"
        else
            text = '''CAST(CONCAT('<a ui-sref=\"$nextState({ $entityId:',$alias$entityId,',$refs })\">',$alias$entityId,'</a>') AS VARCHAR) $columnName'''
        //text = "CAST(CONCAT('<a ui-sref=\"{{nextState}}({ {{entityId}}:',{{alias}}{{entityId}},',$refs })\">',{{alias}}{{entityId}},'</a>') AS CHAR) $columnName"

        resolveTemplate(text, bindingData)

    }

    String resolveTemplate(String text, Map binding) {

        def engine = new SimpleTemplateEngine()
        engine.createTemplate(text)
                .make(binding)
                .toString()
    }


}
