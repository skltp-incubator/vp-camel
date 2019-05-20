package se.skl.tp.vp.util.soaprequests;

public class TestSoapRequests {

  private TestSoapRequests(){
    // Static utility to create Soap request
  }

  public static final String RECEIVER_UNIT_TEST = "UnitTest";
  public static final String RECEIVER_HTTP = "HttpProducer";
  public static final String RECEIVER_HTTPS = "HttpsProducer";
  public static final String RECEIVER_NO_PRODUCER_AVAILABLE = "RecevierNoProducerAvailable";
  public static final String RECEIVER_WITH_NO_VAGVAL = "NoVagvalReceiver";
  public static final String RECEIVER_NOT_AUHORIZED = "NotAuhorizedReceiver";
  public static final String RECEIVER_UNKNOWN_RIVVERSION = "RecevierUnknownRivVersion";
  public static final String RECEIVER_MULTIPLE_VAGVAL = "RecevierMultipleVagval";
  public static final String RECEIVER_NO_PHYSICAL_ADDRESS = "RecevierNoPhysicalAddress";

  public static final String TJANSTEKONTRAKT_GET_CERTIFICATE_KEY = "urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1";


  public static final String GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST_VARIABLE_RECEIVER =
      "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:add=\"http://www.w3.org/2005/08/addressing\" xmlns:urn=\"urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1\">\n"
          +
          "   <soapenv:Header>\n" +
          "      <add:To>%s</add:To>\n" +
          "   </soapenv:Header>\n" +
          "   <soapenv:Body>\n" +
          "      <urn:GetCertificateRequest>\n" +
          "         <urn:certificateId>?</urn:certificateId>\n" +
          "         <urn:nationalIdentityNumber>?</urn:nationalIdentityNumber>\n" +
          "         <!--You may enter ANY elements at this point-->\n" +
          "      </urn:GetCertificateRequest>\n" +
          "   </soapenv:Body>\n" +
          "</soapenv:Envelope>";


  public static final String GET_ACTIVITIES_REQUEST_VARIABLE_RECEIVER =
      "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:riv:itintegration:registry:1\" xmlns:urn1=\"urn:riv:clinicalprocess:activity:actions:GetActivitiesResponder:1\" xmlns:urn2=\"urn:riv:clinicalprocess:activity:actions:1\">\n"
          +
          "   <soapenv:Header>\n" +
          "      <urn:LogicalAddress>%s</urn:LogicalAddress>\n" +
          "   </soapenv:Header>\n" +
          "   <soapenv:Body>\n" +
          "      <urn1:GetActivities>\n" +
          "         <urn1:patientId>\n" +
          "             <urn2:root>patientIdType</urn2:root>\n"+
          "             <urn2:extension>197404188888</urn2:extension>\n" +
          "         </urn1:patientId>\n" +
          "         <urn1:interactionAgreementId>2866a7c4-9c60-433f-9035-a4d779ffe7a1</urn1:interactionAgreementId>" +
          "         <urn1:sourceSystemId>"+
          "             <urn2:root>1.2.752.129.2.1.4.1</urn2:root>" +
          "             <urn2:extension>${sourceSystemHSAId}</urn2:extension>" +
          "         </urn1:sourceSystemId>" +
          "      </urn1:GetActivities>\n" +
          "   </soapenv:Body>\n" +
          "</soapenv:Envelope>";

  public static final String GET_NO_CERT_HTTP_SOAP_REQUEST =  createGetCertificateRequest(RECEIVER_HTTP);
  public static final String GET_CERT_HTTPS_REQUEST = createGetCertificateRequest(RECEIVER_HTTPS);

  public static String createGetCertificateRequest(String receiver){
    return String.format(GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST_VARIABLE_RECEIVER, receiver);
  }

  public static String createGetActivitiesRequest(String receiver){
    return String.format(GET_ACTIVITIES_REQUEST_VARIABLE_RECEIVER, receiver);
  }
}
