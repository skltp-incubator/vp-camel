package se.skl.tp.vp.integrationtests.errorhandling;

import static org.apache.camel.test.junit4.TestSupport.assertStringContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP008;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.soap.SOAPBody;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import se.skl.tp.vp.integrationtests.utils.TestConsumer;
import se.skl.tp.vp.service.TakCacheService;
import se.skl.tp.vp.util.soaprequests.SoapUtils;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;
import se.skltp.takcache.TakCacheLog;
import se.skltp.takcache.TakCacheLog.RefreshStatus;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
public class FullServiceTakErrorIT {

  @Autowired
  TestConsumer testConsumer;

  @Autowired
  TakCacheService takCacheService;

  @SuppressWarnings("unchecked")
  @BeforeClass
  public static void beforeClass() throws IOException {
    // Do not start a takservice
  }

  @Test
  public void shouldGetVP008NoTakServiceAndNoLocalCache() throws Exception {
    TakCacheLog takCacheLog = takCacheService.refresh();
    assertEquals(false, takCacheLog.isRefreshSuccessful());
    assertEquals(RefreshStatus.REFRESH_FAILED, takCacheLog.getRefreshStatus());

    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(TestSoapRequests.GET_CERTIFICATE_NOT_AUTHORIZED_IN_TAK, headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());
    assertStringContains(soapBody.getFault().getFaultString(), VP008.getCode());

  }


}
