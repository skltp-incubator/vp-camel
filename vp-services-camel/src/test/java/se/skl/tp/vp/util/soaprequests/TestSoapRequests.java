package se.skl.tp.vp.util.soaprequests;

public class TestSoapRequests {

  public static final String RECEIVER_UNIT_TEST = "UnitTest";
  public static final String RECEVIER_NO_PRODUCER_AVAILABLE = "RecevierNoProducerAvailable";
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

  public static final String GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST = String.format(GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST_VARIABLE_RECEIVER,
      RECEIVER_UNIT_TEST);
  public static final String GET_CERTIFICATE_NOT_AUTHORIZED_IN_TAK = String.format(GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST_VARIABLE_RECEIVER,
      RECEIVER_NOT_AUHORIZED);
  public static final String GET_CERTIFICATE_NO_VAGVAL_IN_TAK = String.format(GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST_VARIABLE_RECEIVER,
      RECEIVER_WITH_NO_VAGVAL);
  public static final String GET_CERTIFICATE_NO_PRODUCER_NOT_AVAILABLE_ = String.format(GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST_VARIABLE_RECEIVER,
      RECEVIER_NO_PRODUCER_AVAILABLE);
  public static final String GET_CERTIFICATE_UNKNOWN_RIVVERSION_ = String.format(GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST_VARIABLE_RECEIVER,
      RECEIVER_UNKNOWN_RIVVERSION);
  public static final String GET_CERTIFICATE_NO_RECEIVER = String.format(GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST_VARIABLE_RECEIVER, "");
  public static final String GET_CERTIFICATE_MULTIPLE_VAGVAL = String.format(GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST_VARIABLE_RECEIVER,
      RECEIVER_MULTIPLE_VAGVAL);
  public static final String GET_CERTIFICATE_NO_PHYSICAL_ADDRESS = String.format(GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST_VARIABLE_RECEIVER,
      RECEIVER_NO_PHYSICAL_ADDRESS);


}
