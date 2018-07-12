package se.skl.tp.vp.util.soaprequests;

public class TestSoapRequests {

  public static final String GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST =
      "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:add=\"http://www.w3.org/2005/08/addressing\" xmlns:urn=\"urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1\">\n"
          +
          "   <soapenv:Header>\n" +
          "      <add:To>UnitTest</add:To>\n" +
          "   </soapenv:Header>\n" +
          "   <soapenv:Body>\n" +
          "      <urn:GetCertificateRequest>\n" +
          "         <urn:certificateId>?</urn:certificateId>\n" +
          "         <urn:nationalIdentityNumber>?</urn:nationalIdentityNumber>\n" +
          "         <!--You may enter ANY elements at this point-->\n" +
          "      </urn:GetCertificateRequest>\n" +
          "   </soapenv:Body>\n" +
          "</soapenv:Envelope>";

}
