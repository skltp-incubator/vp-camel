package se.skl.tp.vp.wsdl;

import static org.junit.Assert.*;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.NAMNRYMD_1;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RECEIVER_1;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.SENDER_1;
import static se.skl.tp.vp.wsdl.XmlHelper.selectXPathStringValue;

import java.util.Optional;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.skl.tp.vp.config.ProxyHttpForwardedHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.vagval.VagvalTestConfiguration;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = VagvalTestConfiguration.class)
public class WsdlProcessorImplTest {

  private static String PORT = "443";
  private static String HOST = "vp-loadbalancer-dns-name";
  private static String SCHEME = "http";
  private static String QUERY = "xsd=../../core_components/itintegration_registry_1.0.xsd";

  private static String ORIGINAL_URL =
      "https://test.esb.ntjp.se/vp/clinicalprocess/activity/actions/ProcessActivities/1/rivtabp21?wsdl";

  private static String ORIGINAL_URL_WITH_FRAGMENT =
      "https://test.esb.ntjp.se/vp/clinicalprocess/healthcond/certificate/GetCertificate/2/rivtabp21?wsdl#fragment";


  @Autowired
  WsdlProcessor wsdlProcessor;

  @Autowired
  private ProxyHttpForwardedHeaders proxyHttpForwardedHeaders;

  @Test
  public void processUnmatchedUri() throws Exception {

    Exchange ex = createExchangeWithHttpUri("http://0.0.0.0:8080/MyUnmatchedUri?wsdl");
    wsdlProcessor.process(ex);
    String body = (String) ex.getOut().getBody();
    assertTrue(body.contains("No wsdl found on this path, try any of the following paths:"));

  }

  /**
   * Test that the "wsdlProcessor" works with "wsdl legacy sources" that cant be read by naming
   * convention.
   *
   * Legacy format is a wsdl file with a file name not following specification and or a uri not
   * following conventions. In practice the sources and properties of these "files" have to be
   * specified in the wsdlconfig.json (as opposed to just put the wsdl in the dedicated directory)
   * @throws Exception
   */
  @Test
  public void processUriOfLegacyFormat() throws Exception {

    Exchange ex = createExchangeWithHttpUri("http://0.0.0.0:8080/vp/SomWeirdUrlNotFollowingNamingConventions?wsdl");
    wsdlProcessor.process(ex);
    Document document = DocumentHelper.parseText((String) ex.getOut().getBody());
    String name = selectXPathStringValue(document,"wsdl:definitions/@name","wsdl=http://schemas.xmlsoap.org/wsdl/");
    assertTrue("DeleteActivityInteraction".equals(name));
  }

  /**
   * Test that the "wsdlProcessor" works with "wsdl sources" that follows convention and simply are
   * put in an appropriate file structure under the wsdl dir specified by:
   * application.properties#wsdlfiles.directory
   *
   * @throws Exception
   */
  @Test
  public void processUri() throws Exception {
    Exchange ex = createExchangeWithHttpUri(ORIGINAL_URL);
    wsdlProcessor.process(ex);
    String body = (String) ex.getOut().getBody();
    Document document = DocumentHelper.parseText(body);
    String name = selectXPathStringValue(document,"wsdl:definitions/@name","wsdl=http://schemas.xmlsoap.org/wsdl/");
    assertTrue("ProcessActivitiesInteraction".equals(name));
  }


  private Exchange createExchangeWithHttpUri(String pHttpUri) {
    CamelContext ctx = new DefaultCamelContext();
    Exchange ex = new DefaultExchange(ctx);
    ex.getIn().setHeader(Exchange.HTTP_URI, pHttpUri);
    ex.getIn().setHeader(proxyHttpForwardedHeaders.getPort(), PORT);
    ex.getIn().setHeader(proxyHttpForwardedHeaders.getHost(), HOST);
    ex.getIn().setHeader(proxyHttpForwardedHeaders.getProto(), SCHEME);
    return ex;
  }

  private Message createMessageWithForwardedHeader() {
    CamelContext ctx = new DefaultCamelContext();
    Exchange ex = new DefaultExchange(ctx);
    ex.getIn().setHeader(proxyHttpForwardedHeaders.getPort(), PORT);
    ex.getIn().setHeader(proxyHttpForwardedHeaders.getHost(), HOST);
    ex.getIn().setHeader(proxyHttpForwardedHeaders.getProto(), SCHEME);
    return ex.getIn();
  }

  private Message createMessage() {
    CamelContext ctx = new DefaultCamelContext();
    Exchange ex = new DefaultExchange(ctx);
    return ex.getIn();
  }
}
