import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import org.cghr.commons.db.DbAccess
import org.cghr.commons.db.DbStore
import org.cghr.commons.file.FileSystemStore
import org.cghr.dataViewModel.DataModelUtil
import org.cghr.dataViewModel.DhtmlxGridModelTransformer
import org.cghr.security.controller.RequestParser
import org.cghr.security.service.OnlineAuthService
import org.cghr.security.service.UserService
import org.cghr.startupTasks.DbImport
import org.cghr.startupTasks.DirCreator
import org.springframework.web.client.RestTemplate

beans {

    xmlns([context: 'http://www.springframework.org/schema/context'])
    xmlns([mvc: 'http://www.springframework.org/schema/mvc'])

    //Common Services
    context.'component-scan'('base-package': 'org.cghr.commons.web.controller')
    context.'component-scan'('base-package': 'org.cghr.security.controller')
    context.'component-scan'('base-package': 'org.cghr.survey')

    //Project Specific Services
    context.'component-scan'('base-package': 'org.cghr.hc.client.service')

    mvc.'annotation-driven'()
    mvc.'interceptors'() {
        mvc.'mapping'('path': '/api/GridService/**') {
            bean('class': 'org.cghr.security.controller.AuthInterceptor')
        }
    }

    //Server Config
    serverBaseUrl(String, "http://barshi.vm-host.net:8080/hc/")

    //App Real Path
    String appPath = System.getProperty('basePath')
    basePath(String, appPath)

    //Database config
    dataSource(DataSource) {
        driverClassName = 'com.mysql.jdbc.Driver'
        url = 'jdbc:mysql://localhost:3306/hc'
        username = 'root'
        password = 'password'
        initialSize = 5
        maxActive = 10
        maxIdle = 5
        minIdle = 2
    }
    gSql(Sql, dataSource = dataSource)
    dbAccess(DbAccess, gSql = gSql)
    dataStoreFactory(HashMap, [user: 'id', userlog: 'id', authtoken: 'id', datachangelog: 'id', area: 'areaId', house: 'houseId', household: 'householdId', member: 'memberId', hcMember: 'memberId', enumVisit: 'id', hcVisit: 'id', householdCommonQs: 'householdId', householdFoodItems: 'householdId', hospInf: 'householdId', deathInf: 'householdId', householdHosp: 'id', householdDeath: 'id', memberBp1: 'memberId', memberBp2: 'memberId', memberTobaccoAlcohol: 'memberId', memberPersonalMedicalHistory: 'memberId', memberFamilyMedicalHistory: 'memberId', memberPhysicalActivities: 'memberId', ffqGeneral: 'memberId', ffqBeverages: 'memberId', ffqCereals: 'memberId', ffqPulses: 'memberId', ffqVeg: 'memberId', ffqRaw: 'memberId', ffqFruits: 'memberId', ffqJuice: 'memberId', ffqNonVeg: 'memberId', ffqSweets: 'memberId', ffqSpiceMix: 'memberId', ffqSnacks: 'memberId', ffqFoodAdditives: 'memberId', resampling: 'memberId'])
    dbStore(DbStore, gSql = gSql, dataStoreFactory = dataStoreFactory)

    //Auth Config
    requestParser(RequestParser)
    serverAuthUrl(String, appPath + "/api/security/auth")
    restTemplate(RestTemplate)
    onlineAuthService(OnlineAuthService, serverAuthUrl = serverAuthUrl, restTemplate = restTemplate)
    userService(UserService, dbAccess = dbAccess, dbStore = dbStore, onlineAuthService = onlineAuthService)

    //File Store Config
    fileStoreFactory(HashMap,
            [memberImage: [
                    memberConsent: appPath + "/repo/images/consent",
                    memberPhotoId: appPath + "/repo/images/photoId",
                    memberPhoto: appPath + "/repo/images/photo"
            ]])
    fileSystemStore(FileSystemStore, fileStoreFactory = fileStoreFactory, dbStore = dbStore)

    //App Startup Tasks
    dbImport(DbImport, dbScriptsPath = appPath + "/dbScripts", gSql = gSql)
    dirCreator(DirCreator, [
            appPath + "/repo/images/consent",
            appPath + "/repo/images/photoId",
            appPath + "/repo/images/photo"
    ])

    //Data Model Transformer
    transformer(DhtmlxGridModelTransformer, gSql = gSql)
    dataModelUtil(DataModelUtil, transformer = transformer, dbAccess = dbAccess)

}