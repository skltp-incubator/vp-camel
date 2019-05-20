package se.skl.tp.vp.httpheader;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticException;

import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP013;
import static se.skl.tp.vp.integrationtests.httpheader.HeadersUtil.TEST_CONSUMER;

/**
 * Testing that sender that sets the header X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID and isn't allowed to,
 * returns VpSemanticException VP013.
 */
@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SenderNotAllowedToUseHeaderTest extends CamelTestSupport {

    @Autowired
    private HeaderProcessorImpl headerProcessor;

    @Before
    public void setUp() {
       //nada
    }

    @After
    public void after() {
        //nada
    }

    //@Test
    public void testNotAllowedSenderUseHeaderWithString() throws Exception {
        try {
            Exchange ex = createExchangeWithProperties("NotAllowedSender");
            headerProcessor.process(ex);
            fail("Förväntade ett VP013 SemanticException");
        } catch (VpSemanticException vpSemanticException) {
            assertEquals(VP013, vpSemanticException.getErrorCode());
            assertTrue(vpSemanticException.getMessage().contains("VP013 Sender NOT on ConsumerList"));
        }
    }

    //@Test
    public void testNotAllowedSenderUseHeaderWithNull() throws Exception {
        try {
            Exchange ex = createExchangeWithProperties(null);
            headerProcessor.process(ex);
            fail("Förväntade ett VP013 SemanticException");
        } catch (VpSemanticException vpSemanticException) {
            assertEquals(VP013, vpSemanticException.getErrorCode());
            assertTrue(vpSemanticException.getMessage().contains("VP013 Sender NOT on ConsumerList"));
        }
    }

   // @Test
    public void testNotAllowedSenderUseHeaderWithEmptyString() throws Exception {
        try {
            Exchange ex = createExchangeWithProperties("");
            headerProcessor.process(ex);
            fail("Förväntade ett VP013 SemanticException");
        } catch (VpSemanticException vpSemanticException) {
            assertEquals(VP013, vpSemanticException.getErrorCode());
            assertTrue(vpSemanticException.getMessage().contains("VP013 Sender NOT on ConsumerList"));
        }
    }

    @Test
    public void testAllowedSenderUseHeader() throws Exception {
        try {
            Exchange ex = createExchangeWithProperties("tp");
            headerProcessor.process(ex);
            assertEquals(TEST_CONSUMER, ex.getIn().getHeaders().get(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID));
        } catch (VpSemanticException vpSemanticException) {
            fail("Did NOT expect exception");
        }
    }

    private Exchange createExchangeWithProperties(String sender) {
        CamelContext ctx = new DefaultCamelContext();
        Exchange ex = new DefaultExchange(ctx);
        ex.getIn().getHeaders().put(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, TEST_CONSUMER);
        ex.setProperty(VPExchangeProperties.VAGVAL, "http://test.com");
        ex.setProperty(VPExchangeProperties.SENDER_ID, sender);
        return ex;
    }
}
