import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import org.cghr.commons.db.CleanUp
import org.cghr.commons.db.DbAccess
import org.cghr.commons.db.DbStore
import org.cghr.commons.file.FileSystemStore
import org.cghr.dataSync.commons.SyncRunner
import org.cghr.dataSync.providers.*
import org.cghr.dataSync.service.SyncUtil
import org.cghr.security.controller.Auth
import org.cghr.security.controller.PostAuth
import org.cghr.security.service.OnlineAuthService
import org.cghr.security.service.UserService
import org.cghr.startupTasks.DbImport
import org.cghr.startupTasks.DirCreator
import org.cghr.startupTasks.MetaClassEnhancement
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.accept.ContentNegotiationManagerFactoryBean
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.commons.CommonsMultipartResolver
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver

beans {
    xmlns([context: 'http://www.springframework.org/schema/context'])
    xmlns([mvc: 'http://www.springframework.org/schema/mvc'])

    //Common Services
    context.'component-scan'('base-package': 'org.cghr.commons.web.controller')
    context.'component-scan'('base-package': 'org.cghr.dataSync.controller')
    context.'component-scan'('base-package': 'org.cghr.security.controller')
    context.'component-scan'('base-package': 'org.cghr.survey.controller')

    context.'component-scan'('base-package': 'org.cghr.hc.controller')


    mvc.'annotation-driven'()

    mvc.'interceptors'() {
        mvc.'mapping'('path': '/api/GridService/**') {
            bean('class': 'org.cghr.security.controller.AuthInterceptor')
        }
    }
    contentNegotiationViewResolver(ContentNegotiatingViewResolver,{
        mediaTypes=[json:'application/json']
    })
    contentNegotiationManager(ContentNegotiationManagerFactoryBean,{
        defaultContentType="application/json"
    })
    multipartResolver(CommonsMultipartResolver) {
        maxInMemorySize = 10240
        maxUploadSize = 1024000000
        uploadTempDir = "/tmp"
    }

    String userHome = System.getProperty('userHome')
    String appPath = System.getProperty('basePath')
    String server = 'http://barshi.vm-host.net:8080/hcServer/'
    serverBaseUrl(String, server)

    //Data Config
    dataSource(DataSource) { bean ->
        bean.destroyMethod = 'close'
        driverClassName = 'org.h2.Driver'
        //url = 'jdbc:h2:tcp://localhost/~/hcDemo:9092;database_to_upper=false;mode=mysql'
        url = 'jdbc:h2:~/hcDemo;database_to_upper=false;mode=mysql;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE'
        username = 'sa'
        password = ''
        initialSize = 5
        maxActive = 10
        maxIdle = 5
        minIdle = 2
    }
    //Data Config
//    dataSource(DataSource) {
//        driverClassName = 'com.mysql.jdbc.Driver'
//        url = 'jdbc:mysql://localhost:3306/hc'
//        username = 'root'
//        password = 'new1prog$'
//        initialSize = 5
//        maxActive = 10
//        maxIdle = 5
//        minIdle = 2
//    }
    gSql(Sql, dataSource = dataSource)
    dbAccess(DbAccess, gSql = gSql)
    // Entities
    dataStoreFactory(HashMap, [user: 'id', team: 'id', teamuser: 'id', assignment: 'id', userlog: 'id', authtoken: 'id', datachangelog: 'id', filechangelog: 'id', area: 'areaId', house: 'houseId', household: 'householdId', hhContact: 'householdId', member: 'memberId', hcMember: 'memberId', enumVisit: 'id', hcVisit: 'id', state: 'stateId', district: 'districtId', householdCommonQs: 'householdId', householdFoodItems: 'householdId', hospInf: 'householdId', deathInf: 'householdId', householdHosp: 'id', householdDeath: 'id', memberPhoto: 'memberId', memberBp1: 'memberId', memberBp2: 'memberId', memberTobaccoAlcohol: 'memberId', memberAlcoholFreq: 'memberId', memberAlcohol2: 'memberId', memberPersonalMedicalHistory: 'memberId', memberReproductiveHistory: 'memberId', memberGeneralMood: 'memberId', memberFamilyMedicalHistory: 'memberId', memberFmhDisease: 'memberId', memberPhysicalActivities: 'memberId', ffqGeneral: 'memberId', ffqBeverages: 'memberId', ffqCereals: 'memberId', ffqPulses: 'memberId', ffqVeg: 'memberId', ffqRaw: 'memberId', ffqFruits: 'memberId', ffqJuice: 'memberId', ffqNonVeg: 'memberId', ffqSweets: 'memberId', ffqSpiceMix: 'memberId', ffqSnacks: 'memberId', ffqOthers: 'memberId', ffqSalt: 'memberId', ffqFoodAdditives: 'memberId', resampling: 'memberId', outbox: 'id', inbox: 'id', invitationCard: 'memberId', resampAssign: 'memberId', memberImage: 'memberId'])
    dbStore(DbStore, gSql = gSql, dataStoreFactory = dataStoreFactory)

    // File Store Config
    fileStoreFactory(HashMap,
            [memberImage: [
                    memberConsent     : userHome + "hcDemo/repo/images/consent",
                    memberPhotoId     : userHome + "hcDemo/repo/images/photoId",
                    memberPhoto       : userHome + "hcDemo/repo/images/photo",
                    memberPhotoConsent: userHome + "hcDemo/repo/images/photoConsent"
            ]])
    fileSystemStore(FileSystemStore, fileStoreFactory = fileStoreFactory, dbStore = dbStore)


    //Todo Security
    serverAuthUrl(String, "http://localhost:8089/app/api/security/auth")
    httpClientParams()
    httpRequestFactory(HttpComponentsClientHttpRequestFactory) {
        readTimeout = 3000
        connectTimeout = 3000
    }
    restTemplate(RestTemplate, httpRequestFactory)
    onlineAuthService(OnlineAuthService, serverAuthUrl = serverAuthUrl, restTemplate = restTemplate)
    userService(UserService, dbAccess = dbAccess, dbStore = dbStore, onlineAuthService = onlineAuthService)
    postAuth(PostAuth
    )
    auth(Auth)

    //Startup Tasks
    metaClassEnhancement(MetaClassEnhancement)
    dbImport(DbImport, sqlDir = appPath + 'sqlImport', gSql = gSql)
    dirCreator(DirCreator, [
            userHome + 'hcDemo/repo/images/consent',
            userHome + 'hcDemo/repo/images/photo',
            userHome + 'hcDemo/repo/images/photoConsent',
            userHome + 'hcDemo/repo/images/photoId'
    ])

    //Todo DataSync
    String appName = 'hc'
    syncUtil(SyncUtil, dbAccess = dbAccess, restTemplate = restTemplate, baseIp = '192.168.0.', startNode = 100, endNode = 120, port = 8080, pathToCheck = 'api/sync/status/manager', appName = appName)


    agentDownloadServiceProvider(AgentDownloadServiceProvider, dbAccess = dbAccess, dbStore = dbStore, restTemplate = restTemplate,
            serverBaseUrl = serverBaseUrl,
            downloadInfoPath = 'api/sync/downloadInfo',
            downloadDataBatchPath = 'api/data/dataAccessBatchService/',
            syncUtil = syncUtil)

    agentFileUploadServiceProvider(AgentFileUploadServiceProvider, dbAccess = dbAccess, dbStore = dbStore, serverBaseUrl = serverBaseUrl,
            fileStoreFactory = fileStoreFactory,
            awakeFileManagerPath = 'app/AwakeFileManager')

    agentMsgDistServiceProvider(AgentMsgDistServiceProvider, dbAccess = dbAccess, dbStore = dbStore)

    agentUploadServiceProvider(AgentUploadServiceProvider, dbAccess = dbAccess, dbStore = dbStore, restTemplate = restTemplate, changelogChunkSize = 20,
            serverBaseUrl = serverBaseUrl,
            uploadPath = 'api/data/dataStoreBatchService',
            syncUtil = syncUtil)

    agentServiceProvider(AgentServiceProvider, agentDownloadServiceProvider,
            agentFileUploadServiceProvider,
            agentMsgDistServiceProvider,
            agentUploadServiceProvider)
    agentProvider(AgentProvider, agentServiceProvider = agentServiceProvider)
    syncRunner(SyncRunner, agentProvider = agentProvider)


    // Maintenance Tasks
    cleanup(CleanUp, dbAccess = dbAccess, excludedEntities = 'user')

    //Todo JsonSchema Path
    devJsonSchemaPath(String, userHome + 'apps/<appName>/ui/src/assets/jsonSchema')
    prodJsonSchemaPath(String, appPath + 'assets/jsonSchema')

    ipAddressPattern(String, "192.168")
    gpsSocketPort(Integer, 4444)
}
