# Konfiguration av VP Camel

Konfigurering kan göras i filerna listade nedan. Vid respektive avsnitt finns information om funktion och vad som kan ändras.
  
 * application.properties  
 * application-security.properties  
 * timeoutconfig.json  
 * wsdlconfig.json

För mer information om hur eventuell proxy eller lastbalanserare ska konfigureras, samt exempelfiler, se [Detaljerad konfiguration].
Loggning och hur det går till och kan konfigureras kan man läsa om här: [Loggning konfiguration]
### Application.properties ###
Denna fil i original ligger under resources i jaren. Den kan överlagras (hela eller delar) genom att man skapar filen application-custom.properties som läggs i en mapp config. Den mappen ska läggas i mappen där programmet körs. 
|Nyckel|Defaultvärde/Exempel|Beskrivning|
| ---- | ------------------ | --------- |
|server.port|8880|Porten som servern ska starta på|
|server.use.forward.headers|false|Om VP Camel befinner sig bakom en proxy, sätt denna till true. Se vidare här: VP Camel Detaljerad konfiguration. Om propertyn saknas är defaultvärdet ```false```.|
|server.undertow.accesslog.dir|var/log/camel|Konfiguration för undertow accesslog: Var filerna ska lagras. Se till exempel [Tips på hur man konfigurerar undertow] eller sök på undertow på sidan [Spring-boot doc's]|
|server.undertow.accesslog.enabled|true|Konfiguration för undertow accesslog: På/av|
|server.undertow.accesslog.pattern|common|Konfiguration för undertow accesslog: Format på loggningen|
|server.undertow.accesslog.prefix|access_log|Konfiguration för undertow accesslog: Prefix på logg-filen|
|server.undertow.accesslog.rotate|true|Konfiguration för undertow accesslog: Använda rotering av filer|
|server.undertow.accesslog.suffix|.log|Konfiguration för undertow accesslog: Suffix på logg-filen|
|spring.profiles.include|security|Inkludera Spring's säkerhetsprofil|
|camel.springboot.name|vp-services|Namn på Spring-boot applikationen|
|management.endpoints.web.exposure.include|hawtion,jolokia| |
|hawtio.authenticationEnabled|false|Behövs autentisering för att logga med Hawtio?|
|base.path|/opt/vp|Sökväg till installationen|
|vp.instance.id|dev_env|Identifierare för den installerade VP:n|
|vp.host|localhost|Servern som VP är installerad på|
|vp.http.route.url|htttp://${vp.host}:12312/vp|Ingång för HTTP-anrop. Porten kan konfigureras|
|vp.https.route.url|https://${vp.host}:443/vp|Ingång för HTTPS-anrop. Porten kan konfigureras|
|vp.hsa.reset.cache.url|http://${vp.host}:24000/resethsacache|Ingång för anrop för att uppdatera HSA-cachen. Porten kan konfigureras|
|vp.reset.cache.url|http://${vp.host}:24000/resetcache|Ingång för anrop för att uppdatera TAK-cachen. Porten kan konfigureras|
|vp.status.url|http://${vp.host}:1080/status|Adressen till status-tjänsten, se även [SKLTP VP - Status tjänst]|
|management.security.enabled|false|False: Tillåt access till alla endpoints utan säkerhets-kontroll|
|endpoints.health.enabled|true|True: Slå på health-check för endpoints|
|endpoints.camelroutes.enabled|true|Medger tillgång till information om de Camel-routes som finns|
|endpoints.camelroutes.read-only|true|Tillgång till endpoints bara i read-only mode|
|certificate.senderid.subject.pattern|(?:2.5.4.5\|SERIALNUMBER)=([^,]+)|Var i certifikatet hittas senderId (reg-exp pattern)|
|http.forwarded.header.xfor|X-Forwarded-For|Reverse proxy/LB header-avsändare|
|http.forwarded.header.host|X-VP-Forwarded-Host|Reverse proxy/LB header-Intern host|
|http.forwarded.header.port|X-VP-Forwarded-Port|Reverse proxy/LB header-Intern port|
|http.forwarded.header.proto|X-VP-Forwarded-Proto|Reverse proxy/LB header-??|
|ip.whitelist|127.0.0.1|Komma-separerad lista. Vilka IP-adresser får sätta headern ```sender-id``` för http-anrop. Listan kan vara tom, vilket gör att alla tillåts|
|sender.id.allowed.list|127.0.0.1|Komma-separerad lista. Vilka adresser får sätta headern ```x-rivta-original-serviceconsumer-hsaid ```. Om listan ska användas beror på värdet av nyckeln  ```approve.the.use.of.header.originalconsumer``` nedan|
|approve.the.use.of.header.original.consumer|false|Ska listan ```sender.id.allowed.list``` användas eller ej. Om denna ej är satt, är defaultvärdet true, vilket kan orsaka problem om listan är tom|
|propagate.correlation.id.for.https|false|Ska korrelations-id:t propageras vidare även för https?|
|vp.header.user.agent|SKLTP VP/3.1|User_Agent för utgående requests|
|vp.header.content.type|text/xml;charset=UTF-8|Content-type för utgående requests|
|hsa.files|<file_path>/hsacache.xml,<file_path>/hsacacheComplementary.xml|Lista med filer som ska läsas av HSA cachen. Den första master, övriga kompletterande|
|vagvalrouter.default.routing.address.delimiter|#|Avgör om default routing ska användas (VG#VE) när man evaluerar vägval och behörigheter. Om värdet inte är satt så är default routing avstängt|
|takcache.use.behorighet.cache|true|Ska behörigheter användas i TAK-cachen?|
|takcache.use.vagval.cache|true|Ska vägval användas i TAK-cachen?|
|takcache.persistent.file.name|<path>/local-tak-cache.xml|Sökväg och namn till TAK-cachen|
|takcache.endpoint.address|<host:port>/takmockservice|Var ska cachen förnyas, dvs var finns den installerade TAK:en?|
|timeout.json.file|timeoutconfig.json|Sökväg till default eller skapad timeoutconfig-fil|
|timeout.json.file.default.tjansteKontrakt.name|default_timeouts|Vilket tjänstekontrakts timeout-värden ska anändas som default?|
|wsdl.json.file|wsdlconfig.json|Namn på json-fil med lista av wsdl:er som har url:er som inte följer standard. Se avsnitt om Wsdlconfig.json nedan|
|wsdlfiles.directory|<fullpath>/wsdl|Sökväg till mapp med wdsl-filer. De installerade kontrakten|
|headers.reg.exp.removeRegExp|(?i)x-vp.\*\|PEER_CERTIFICATES\|X-Forwarded.*\|CamelHttpPath|Filtrerar bort oönskade headrar från utgående request|
|headers.reg.exp.keepRegExp|(?i)x-vp-sender-id\|x-vp-instance-id|Ser till att vissa headrar inte försvinner från utgående request|
|vp.producer.retry.attempts|1|Hur många gånger ska producenten anropas vid misslyckat anrop? Negativt värde gör att den aldrig ger upp, 0 att den bara gör det första försöket|
|vp.producer.retry.delay|2000|Hur länge ska vp vänta till nästa försök, vid misslyckat anrop till producent (mS)|
|vp.maxreceive.length|157286640|Maxstorlek i bytes för Response, 15 mB|
|vp.connection.timeout|2000|Timeout för Producentens svar (mS)|

### Application-security.properties ###
Denna fil i original ligger under resources i jaren. Den kan överlagras (hela eller delar) genom att man skapar en fil `application-security.properties` som man lägger i en mapp `config`. Den mappen ska ligga i mappen där programmet körs. 
|Nyckel|Defaultvärde/Exempel|Beskrivning|
|------|--------------------|----------|
|tp.tls.store.location|/certs/|Mapp där certifikaten kan hittas|
|tp.tls.store.truststore.file|truststore.jks|Ska innehålla namn på de CA’s och Certifikat som VP kan lita på|
|tp.tls.store.truststore.password|<password>|Lösenord för trust-store|
|tp.tls.store.producer.file|tp.jks|Certifikat som används av VP i rollen som producent|
|tp.tls.store.producer.password|<password>|Lösenord för producent-certifikatet|
|tp.tls.store.producer.keyPassword|<password>|Lösenord för den privata nyckeln i producent-certifikatet|
|tp.tls.store.consumer.file|client.jks|Certifikat som används av VP i rollen som konsument|
|tp.tls.store.consumer.password|<password>|Lösenord för konsument-certifikatet|
|tp.tls.store.consumer.keyPassword|<password>|Lösenord för den privata nyckeln i konsument-certifikatet|
|tp.tls.allowedIncomingProtocols|TLSv1,TLSv1.1,TLSv1.2|Godkända protokoll för inkommande trafik|
|tp.tls.allowedOutgoingProtocols |TLSv1,TLSv1.1,TLSv1.2|Godkända protokoll för utgående trafik|

### Timeoutconfig.json ###
I `application.properties` pekas denna fil ut av nyckeln `timeout.json.file`. Vill man ha olika timeout’s på olika tjänstekontrakt kan man alltså redigera en ny fil och peka ut denna via `applicaton-custom.properties`. Vill man att ett av kontrakten ska användas som default, så sätter man namnet som värde i nyckeln `timeout.json.file.default.tjanstekontrakt.name` i `applicaton-custom.properties`. Annars kommer default-värdet i orginalfilen att användas, för de tjanstekontrakt som inte matchas mot de som finns i denna fil. Filen ska innehålla en komma-separerad lista i på formatet:
```
[
  {
    "tjanstekontrakt": "default_timeouts",
    "routetimeout": 30000,
    "producertimeout": 30000
  },  
  {
    "tjanstekontrakt": "urn.riv.processdevelopment.infections.DeleteActivityResponder.1",
    "routetimeout": 5000,
    "producertimeout": 5000
  },
  {
    "tjanstekontrakt": "urn.riv.processdevelopment.infections.DeleteCareEncounterResponder.1",
    "routetimeout": 4000,
    "producertimeout": 4000
  }
]
```
Producertimeout anger hur lång tid producenten har på sig att svara. Routetimeout används inte i nuläget.
Nycklarna och deras default-värden är:

|Nyckel|Defaultvärde/Exempel|
|------|--------------------|
|"tjanstekontrakt"|"default_timeouts"|
|"routetimeout"|30000|
|"producertimeout"|29000|

### Wsdlconfig.json ###
I `application.properties` pekas denna fil ut av nyckeln `wsdl.json.file`. Om man har wsdl-filer som av någon anledning inte följer naming-standard, så kan man lägga in dem i denna fil, på det format som framgår nedan. Filen ska alltså vara en komma-separerad lista på wsdl:er i json-format:
```
[
  {
   "tjanstekontrakt": "urn:riv:clinicalprocess:activity:actions:DeleteActivityResponder:1",    
   "wsdlfilepath": "classpath:testfiles/wsdl/clinicalprocess.activity.actions.GetActivities.1.rivtabp21/schemas/interactions/GetActivitiesInteraction/GetActivitiesInteraction_1.0_RIVTABP21.wsdl",    
   "wsdlurl": "vp/SomWeirdUrlNotFollowingNamingConventions"  
  }, 
  {
   "tjanstekontrakt":"x:x:x:x:1", 
   "wsdlfilepath":"classpath:xxx/yyy/GetSomethingInteraction_1.0_RIVTAB21.wsdl", 
   "wsdlurl":"vp/SomWeirdUrlNotFollowingNamingConventions"  
  }
]
```
Nycklarna och deras default-värden är:

|Nyckel|Defaultvärde/Exempel|
|------|--------------------|
|"tjanstekontrakt"|“urn:riv:clinicalprocess:actiity:actions:DeleteActivityResponder:1”|
|"wsdlfilepath"|“classpath:testfiles/wsdl/clinicalprocess.activity.actions.GetActivities.1.rivtab21/schemas/interactions/GetActivitiesInteraction/GetActivitiesInteraction_1.0_RIVTAB21.wsdl”|
|"wsdlurl"|“vp/SomeWeirdUrlNotFollowingNamingConventions”|

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)


   [Detaljerad konfiguration]: <https://github.com/skltp/vp-camel/docs/detail_config.md>
   [Loggning konfiguration]: <https://github.com/skltp/vp-camel/docs/logconfig.md>
   [Tips på hur man konfigurerar undertow]: <https://howtodoinjava.com/spring-boot2/embedded-server-logging-config/>
   [Spring-boot doc's]: <https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html>
   [SKLTP VP - Status tjänst]: <https://skl-tp.atlassian.net/wiki/spaces/FT/pages/674136339/SKLTP+VP+-+Status+tj+nst>