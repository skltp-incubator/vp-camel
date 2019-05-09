package se.skl.tp.vp.httpheader;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.skl.tp.vp.certificate.HeaderCertificateHelperImpl;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.errorhandling.ExceptionUtil;
import se.skl.tp.vp.errorhandling.VpCodeMessages;
import se.skl.tp.vp.exceptions.VpSemanticException;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes={SenderIpExtractorFromHeader.class, HeaderCertificateHelperImpl.class, IPWhitelistHandlerImpl.class,
    HttpSenderIdExtractorProcessorImpl.class, VpCodeMessages.class, ExceptionUtil.class})
public class HttpSenderIdExtractorProcessorImplTest {

  public static final String VP_INSTANCE_ID = "dev_env";
  public static final String RTP_INSTANCE_ID = "rtp_env";
  public static final String WHITELISTED_IP_ADDRESS = "1.2.3.4";
  public static final String NOT_WHITELISTED_IP_ADDRESS = "10.20.30.40";
  public static final String HEADER_SENDER_ID = "Sender1";

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Autowired
  HttpSenderIdExtractorProcessorImpl httpHeaderExtractorProcessor;

  @Test
  public void ifWhitelistedShouldSetSenderIdFromInHeader() throws Exception {
    Exchange exchange = createExchange();
    exchange.getIn().setHeader(HttpHeaders.X_VP_SENDER_ID, HEADER_SENDER_ID);
    exchange.getIn().setHeader(HttpHeaders.X_VP_INSTANCE_ID, VP_INSTANCE_ID);
    exchange.getIn().setHeader("X-Forwarded-For", WHITELISTED_IP_ADDRESS);

    httpHeaderExtractorProcessor.process(exchange);

    assertEquals(HEADER_SENDER_ID, exchange.getProperty(VPExchangeProperties.SENDER_ID));
  }

  @Test
  public void ifNotWhitelistedShouldThrowVP011() throws Exception {
    thrown.expect(VpSemanticException.class);
    thrown.expectMessage(containsString("VP011"));

    Exchange exchange = createExchange();
    exchange.getIn().setHeader(HttpHeaders.X_VP_SENDER_ID, HEADER_SENDER_ID);
    exchange.getIn().setHeader(HttpHeaders.X_VP_INSTANCE_ID, VP_INSTANCE_ID);
    exchange.getIn().setHeader("X-Forwarded-For", NOT_WHITELISTED_IP_ADDRESS);

    httpHeaderExtractorProcessor.process(exchange);

    assertEquals(HEADER_SENDER_ID, exchange.getProperty(VPExchangeProperties.SENDER_ID));
  }

  @Test
  public void ifAnotherInstanceSenderIdShouldBeExtractedFromCert() throws Exception {
    final X500Principal principal = new X500Principal("OU=urken");
    final X509Certificate cert = Mockito.mock(X509Certificate.class);
    Mockito.when(cert.getSubjectX500Principal()).thenReturn(principal);

    Exchange exchange = createExchange();
    exchange.getIn().setHeader(HttpHeaders.X_VP_SENDER_ID, HEADER_SENDER_ID);
    exchange.getIn().setHeader(HttpHeaders.X_VP_INSTANCE_ID, RTP_INSTANCE_ID);
    exchange.getIn().setHeader("X-Forwarded-For", NOT_WHITELISTED_IP_ADDRESS);
    exchange.getIn().setHeader(HttpHeaders.REVERSE_PROXY_HEADER_NAME, cert);

    httpHeaderExtractorProcessor.process(exchange);

    assertEquals("urken", exchange.getProperty(VPExchangeProperties.SENDER_ID));
  }

  private Exchange createExchange() {
    CamelContext ctx = new DefaultCamelContext();
    return new DefaultExchange(ctx);
  }

}