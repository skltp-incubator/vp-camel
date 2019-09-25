# Detaljerad konfiguration

I det här dokumentet finns dels information om hur man hanterar en proxy eller lastbalanserare framför VP Camel.
Längre ner finns också exempelfiler som visar hur VP Camel kan konfigureras.
Huvudsidan för konfigurering finns här: [VP Camel konfigurering]

### Terminering av SSL/TLS framför VP Camel

Om SSL/TLS trafik termineras framför VP, i t ex en reverse-proxy, behöver dessa konfigurationer göras:

##### Konfigurationer i proxyn:
 1. Propagera certifikat och inkommande HTTP-header(s) från reverse-proxy till VP:
    - Sätta certifikat för inkommande anrop i HTTP-header: `x-vp-auth-cert`
    - Propagera HTTP-header : `x-rivta-original-serviceconsumer-hsaid`   
    __Observera! Endast om den är satt i inkommande request.__
    - Propagera IP-nr för inkommande anrop i HTTP-header med namn enligt property `http.forwarded.header.xfor` i `application.properties` (se nedan).   
    För information om detta, se FK-5 i [Arkitekturella krav] samt anvisning i [RIV-TA] kapitel 8.5

  2. Sätta HTTP Forwarded headers för att stödja WSDL-lookup (t ex som: `https://vp/service_x?wsdl`):
   Ref: [SKLTP - Lastbalanserare / Reverse-proxy]

##### Konfigurationer i VP Camel application-custom.properties
 1. Namn på HTTP Forwarded headers kan (men behöver inte) ändras i `application-custom.properties`:
     - `http.forwarded.header.host=X-VP-Forwarded-Host`
     - `http.forwarded.header.port=X-VP-Forwarded-Port`
     - `http.forwarded.header.proto=X-VP-Forwarded-Proto`
     - `http.forwarded.header.xfor=X-Forwarded-For`
   
 2. Lägga till IP-nr (inre) för reverse-proxy’n till VP Camel's whitelist property (i `application-custom.properties`): 
     - `ip.whitelist=proxy inre ip-adress`


### Response Timeout
För de virtuella tjänster i VP som har stöd för individuell inställning av timeout går detta att styra per tjänst.
Det default-värde som används kan överlagras genom att redigera en fil `timeout-config.json`. Se [VP Camel konfigurering]. 
Default sätts connection timeout i nedanstående parameter i `application.properties`: 
`vp.connection.timeout=2000`

### Konfigurera loggning
Se anvisningar på sidan [Loggning konfigurering]

### Exempel på application-custom.properties
```
#----------------------  
# Spring configuration
#----------------------  

# Porten som servern skall starta på
server.port=8039

# true om(när) en lastbalanserare används framför VP
server.use-forward-headers=false 

# Konfiguration för undertow accesslog
server.undertow.accesslog.dir=/var/log/camel
server.undertow.accesslog.enabled=true
server.undertow.accesslog.pattern=common
server.undertow.accesslog.prefix=access_log
server.undertow.accesslog.rotate=true
server.undertow.accesslog.suffix=.log

#debug=true

#----------------------  
# Hawtio configuration
#----------------------
management.endpoints.web.exposure.include=hawtio,jolokia
hawtio.authenticationEnabled=false

#----------------------  
# VP configuration
#----------------------

# Installation basepath
base.path=/opt/vp

# VP basic configuration
vp.instance.id=NTjP_T_SERVICES_SE165565594230-10BR

vp.host=0.0.0.0
vp.http.route.url = http://${vp.host}:8080/vp
vp.https.route.url = https://${vp.host}:20000/vp
vp.status.url=http://${vp.host}:8080/status

vp.hsa.reset.cache.url=http://${vp.host}:24000/resethsacache
vp.reset.cache.url=http://${vp.host}:23000/resetcache

# Configuration if VP should perform a retry 
#  for SocketExceptions when calling producer 
vp.producer.retry.attempts=1
vp.producer.retry.delay=2000

# Header name where to find consumer IP address (set by forward proxy)
vagvalrouter.sender.ip.adress.http.header=X-Forwarded-For

# Whitelist fo IP addresses allowed to set sender-id in headers for http call
ip.whitelist=127.0.0.1,x.x.x.x,y.y.y.y,...

# List if files to be read by HSA cache, first file is master, rest is complementary
hsa.files=/www/inera/home/ine-app/hsaUppdatering/files/hsacache.xml,${base.path}/config/hsacachecomplementary.xml

# Defines if we should use old style default routing (VG#VE) when
# evaluating vÃ¤gval and behÃ¶righeter. Set this to blank to
# turn default routing off.
vagvalrouter.default.routing.address.delimiter=#
defaultrouting.allowedContracts=urn:riv:ehr:accesscontrol:AssertCareEngagementResponder:1,urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateQuestionResponder:1,urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateAnswerResponder:1
defaultrouting.allowedSenderIds=

# TAK configuration
takcache.use.behorighet.cache=true
takcache.use.vagval.cache=true
takcache.persistent.file.name=${base.path}/data/localCache
takcache.endpoint.address=http://ine-dit-app01:8085/tak-services/SokVagvalsInfo/v2

# Configuration for contract specific timeouts
timeout.json.file=${base.path}/config/timeoutconfig.json
timeout.json.file.default.tjanstekontrakt.name=default_timeouts

# Configuration for WSDL handling
wsdl.json.file=${base.path}/wsdl/wsdlconfig.json
wsdlfiles.directory=${base.path}/wsdl/

# Max message size VP could recieve in bytes (15Mb=15*1024*1024=15728640)
# Applies for messages recieved from consumer and responses from producer
vp.max.receive.length=15728640

headers.reg.exp.removeRegExp=(?i).*
headers.reg.exp.keepRegExp=(?i)x-rivta.*|x-skltp.*|SOAPAction|x-vp-sender-id|x-vp-instance-id
```

### Exempel på application-security.properties
```
# Overrides applications default vp-config.properties
 
#Location where certificate files are found
TP_TLS_STORE_LOCATION=/etc/mule/conf
 
 
#Truststore settings, what CAs and certificates VP should trust when communicating with
#consumers and producers.
TP_TLS_STORE_TRUSTSTORE_TYPE=jks
TP_TLS_STORE_TRUSTSTORE_FILE=truststore.jks
TP_TLS_STORE_TRUSTSTORE_PASSWORD=password
 
 
#Settings for the producer connector, when VP acts as producer, receiving calls from consumers
TP_TLS_STORE_PRODUCER_TYPE=jks
TP_TLS_STORE_PRODUCER_FILE=keystore.jks
TP_TLS_STORE_PRODUCER_PASSWORD=password
TP_TLS_STORE_PRODUCER_KEY_PASSWORD=password
 
 
#Settings for the consumer connector, when VP acts as consumer, making calls to producers
TP_TLS_STORE_CONSUMER_TYPE=jks
TP_TLS_STORE_CONSUMER_FILE=keystore.jks
TP_TLS_STORE_CONSUMER_PASSWORD=password
TP_TLS_STORE_CONSUMER_KEY_PASSWORD=password
```

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)


   [SKLTP - Lastbalanserare / Reverse-proxy]: <https://skl-tp.atlassian.net/wiki/spaces/SKLTP/pages/22773796/SKLTP+-+Lastbalanserare+Reverse-proxy>
   [Loggning konfigurering]: <logging_configuration.md>
   [VP Camel konfigurering]: <configuration.md>
   [Arkitekturella krav]: <https://skl-tp.atlassian.net/wiki/spaces/SKLTP/pages/44892313/SKLTP+VP+SAD+-+Arkitekturella+krav#SKLTPVPSAD-Arkitekturellakrav-Arkitekturellakrav-FK-5,Ursprungligavs%C3%A4ndare>
   [RIV-TA]: <http://rivta.se/documents/ARK_0001/RIV_Tekniska_Anvisningar_Oversikt_revE.pdf>