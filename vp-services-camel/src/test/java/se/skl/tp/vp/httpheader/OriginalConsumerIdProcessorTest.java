package se.skl.tp.vp.httpheader;


import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;

import static org.junit.Assert.assertEquals;

@RunWith( CamelSpringBootRunner.class )
@ContextConfiguration(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
public class OriginalConsumerIdProcessorTest {

  private String xRivtaOriginalConsumerId = "aTestConsumerId";
  private String approvedSenderId = "127.0.0.1";
  private String notApprovedSenderId = "1.2.3.4";
  private boolean configuredValue;

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Autowired
  OriginalConsumerIdProcessorImpl originalConsumerIdProcessor;

  @Before
  public void beforeTest(){
    configuredValue = originalConsumerIdProcessor.isEnforceSenderIdCheck();
  }

  @After
  public void after(){
    originalConsumerIdProcessor.setEnforceSenderIdCheck(configuredValue);
  }

  @Test
  public void senderTestedForUseOfXrivtaOriginalConsumerId() throws Exception {
    Exchange exchange = createExchange();

    exchange.setProperty(VPExchangeProperties.SENDER_ID, approvedSenderId);
    exchange.getIn().setHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, xRivtaOriginalConsumerId);
    originalConsumerIdProcessor.setEnforceSenderIdCheck(true);
    originalConsumerIdProcessor.process(exchange);
    assertEquals(xRivtaOriginalConsumerId, exchange.getProperty(VPExchangeProperties.IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID));
  }

  @Test
  public void senderNotTestedForUseOfXrivtaOriginalConsumerId() throws Exception {
    Exchange exchange = createExchange();
    exchange.setProperty(VPExchangeProperties.SENDER_ID, notApprovedSenderId);
    exchange.getIn().setHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, xRivtaOriginalConsumerId);
    originalConsumerIdProcessor.setEnforceSenderIdCheck(false);
    originalConsumerIdProcessor.process(exchange);
    assertEquals(xRivtaOriginalConsumerId, exchange.getProperty(VPExchangeProperties.IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID));
  }


  private Exchange createExchange() {
    CamelContext ctx = new DefaultCamelContext();
    return new DefaultExchange(ctx);
  }
}
