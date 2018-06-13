package se.skl.tp.vp;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.skl.tp.vp.certificate.CertificateExtractorProcessor;
import se.skl.tp.vp.constants.ApplicationProperties;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(classes = se.skl.tp.vp.BeansConfiguration.class)
@TestPropertySource("classpath:application.properties")
public class CertificateReaderTest extends CamelTestSupport {

    @Autowired
    Environment env;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Autowired
    CertificateExtractorProcessor certificateExtractorProcessor;

    @Test
    public void testSendRivta20MessageHttps() throws Exception {
        String expectedBody = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:add=\"http://www.w3.org/2005/08/addressing\" xmlns:urn=\"urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1\">\n" +
                "   <soapenv:Header>\n" +
                "      <add:To>1</add:To>\n" +
                "   </soapenv:Header>\n" +
                "   <soapenv:Body>\n" +
                "      <urn:GetCertificateRequest>\n" +
                "         <urn:certificateId>?</urn:certificateId>\n" +
                "         <urn:nationalIdentityNumber>?</urn:nationalIdentityNumber>\n" +
                "         <!--You may enter ANY elements at this point-->\n" +
                "      </urn:GetCertificateRequest>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        resultEndpoint.expectedBodiesReceived(expectedBody);
        resultEndpoint.expectedPropertyReceived("senderid", "tp");

        template.sendBody(expectedBody);
        resultEndpoint.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                        .to("netty4-http:https://localhost:4433/vp?sslContextParameters=#outgoingSSLContextParameters&ssl=true");

                from("netty4-http:https://localhost:4433/vp?sslContextParameters=#incomingSSLContextParameters&ssl=true&sslClientCertHeaders=true&needClientAuth=true")
                        .process(certificateExtractorProcessor)
                        .to("mock:result");
            }
        };
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        KeyStoreParameters incomingksp = new KeyStoreParameters();
        incomingksp.setResource(env.getProperty(ApplicationProperties.TP_TLS_STORE_LOCATION) + env.getProperty(ApplicationProperties.TP_TLS_STORE_PRODUCER_FILE));
        incomingksp.setPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_PRODUCER_PASSWORD));
        KeyManagersParameters incomingkmp = new KeyManagersParameters();
        incomingkmp.setKeyPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_PRODUCER_KEY_PASSWORD));
        incomingkmp.setKeyStore(incomingksp);
        KeyStoreParameters incomingtsp = new KeyStoreParameters();
        incomingtsp.setResource(env.getProperty(ApplicationProperties.TP_TLS_STORE_LOCATION) + env.getProperty(ApplicationProperties.TP_TLS_STORE_TRUSTSTORE_FILE));
        incomingtsp.setPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_TRUSTSTORE_PASSWORD));
        TrustManagersParameters incomingtmp = new TrustManagersParameters();
        incomingtmp.setKeyStore(incomingtsp);
        SSLContextParameters incomingsslContextParameters = new SSLContextParameters();
        incomingsslContextParameters.setKeyManagers(incomingkmp);
        incomingsslContextParameters.setTrustManagers(incomingtmp);

        KeyStoreParameters outgoingksp = new KeyStoreParameters();
        outgoingksp.setResource(env.getProperty(ApplicationProperties.TP_TLS_STORE_LOCATION) + env.getProperty(ApplicationProperties.TP_TLS_STORE_CONSUMER_FILE));
        outgoingksp.setPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_CONSUMER_PASSWORD));
        KeyManagersParameters outgoingkmp = new KeyManagersParameters();
        outgoingkmp.setKeyPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_CONSUMER_KEY_PASSWORD));
        outgoingkmp.setKeyStore(outgoingksp);
        KeyStoreParameters outgoingtsp = new KeyStoreParameters();
        outgoingtsp.setResource(env.getProperty(ApplicationProperties.TP_TLS_STORE_LOCATION) + env.getProperty(ApplicationProperties.TP_TLS_STORE_TRUSTSTORE_FILE));
        outgoingtsp.setPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_TRUSTSTORE_PASSWORD));
        TrustManagersParameters outgoingtmp = new TrustManagersParameters();
        outgoingtmp.setKeyStore(outgoingtsp);
        SSLContextParameters outgoingsslContextParameters = new SSLContextParameters();
        outgoingsslContextParameters.setKeyManagers(outgoingkmp);
        outgoingsslContextParameters.setTrustManagers(outgoingtmp);

        JndiRegistry registry = super.createRegistry();
        registry.bind("incomingSSLContextParameters", incomingsslContextParameters);
        registry.bind("outgoingSSLContextParameters", outgoingsslContextParameters);

        return registry;
    }

}
