package se.skl.tp.vp.httpheader;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.errorhandling.ExceptionUtil;
import se.skl.tp.vp.errorhandling.VpCodeMessages;
import se.skl.tp.vp.exceptions.VpSemanticException;
import static org.junit.Assert.assertTrue;
import static se.skl.tp.vp.constants.PropertyConstants.VP_PLATFORM_ID;

@RunWith(SpringRunner.class)
@ContextConfiguration(
    classes = {
      SenderIpExtractorFromHeader.class,
      FeedbackProtectionProcessorImpl.class,
      ExceptionUtil.class,
      VpCodeMessages.class
    })
@TestPropertySource("classpath:application.properties")
public class FeedbackProtectionProcessorTest {

  private static final String APPROVED_SENDER_ID = "SENDER1";

  @Value("${" + VP_PLATFORM_ID + "}")
  private String platformId;

  private String localPlatformId;

  @Autowired FeedbackProtectionProcessorImpl feedbackProtectionProcessor;

  @Before
  public void beforeTest(){
    if (StringUtils.isEmpty(platformId)) {
      localPlatformId = "testPlatformId";
    } else {
      localPlatformId = platformId;
    }
  }

  @After
  public void after(){
    feedbackProtectionProcessor.setPlatformId(platformId);
  }

  @Test
  public void senderHasSetHeaderXvpPlatformId() throws Exception {
    feedbackProtectionProcessor.setPlatformId(localPlatformId);
    Exchange exchange = createExchange();
    exchange.setProperty(VPExchangeProperties.SENDER_ID, APPROVED_SENDER_ID);
    exchange.getIn().setHeader(HttpHeaders.X_VP_PLATFORM_ID, platformId);
    try {
      feedbackProtectionProcessor.process(exchange);
    } catch (VpSemanticException e) {
      assertTrue(e.getMessage().contains("VP014 Faulty addressing. This message was sent from this VP instance and has been returned."));

    }
  }

  @Test
  public void senderHasNotSetHeaderXvpPlatformId() throws Exception {
    feedbackProtectionProcessor.setPlatformId(localPlatformId);
    Exchange exchange = createExchange();
    exchange.setProperty(VPExchangeProperties.SENDER_ID, APPROVED_SENDER_ID);
    try {
      feedbackProtectionProcessor.process(exchange);
      assertTrue(exchange.getIn().getHeader(HttpHeaders.X_VP_PLATFORM_ID).equals(localPlatformId));
    } catch (VpSemanticException e) {
      // Don't expect exception....
    }
  }

  @Test
  public void senderHasSetHeaderXvpPlatformIdLocalValueNull() throws Exception {
    feedbackProtectionProcessor.setPlatformId(null);
    Exchange exchange = createExchange();
    exchange.setProperty(VPExchangeProperties.SENDER_ID, APPROVED_SENDER_ID);
    try {
      feedbackProtectionProcessor.process(exchange);
      assertTrue(exchange.getIn().getHeader(HttpHeaders.X_VP_PLATFORM_ID) == null);
    } catch (VpSemanticException e) {
      // Don't expect exception....
    }
  }

  private Exchange createExchange() {
    CamelContext ctx = new DefaultCamelContext();
    return new DefaultExchange(ctx);
  }
}
