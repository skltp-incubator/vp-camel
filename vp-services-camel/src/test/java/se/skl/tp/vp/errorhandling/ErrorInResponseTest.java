package se.skl.tp.vp.errorhandling;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.skl.tp.vp.Application;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.exceptions.VpSemanticException;
import se.skl.tp.vp.httpheader.SenderIpExtractor;

@RunWith( SpringJUnit4ClassRunner.class )
@SpringBootTest(classes = Application.class)
@ContextConfiguration(classes = se.skl.tp.vp.BeansConfiguration.class)
@TestPropertySource("classpath:application.properties")
public class ErrorInResponseTest extends CamelTestSupport {

    public static final String EXCEPTION_MESSAGE = "Fel fel fel";
    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Autowired
    SenderIpExtractor senderIpExtractor;

    @Autowired
    ExceptionMessageProcessor exceptionMessageProcessor;

    @Test
    public void errorInResponseTest() throws Exception {
        String expectedBody = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:add=\"http://www.w3.org/2005/08/addressing\" xmlns:urn=\"urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1\">\n" +
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

        resultEndpoint.expectedBodiesReceived(SoapFaultHelper.generateSoap11FaultWithCause(EXCEPTION_MESSAGE));

        template.sendBody(expectedBody);
        resultEndpoint.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                        .setHeader(HttpHeaders.X_VP_SENDER_ID, constant("UnitTest"))
                        .setHeader(HttpHeaders.X_VP_INSTANCE_ID, constant("dev_env"))
                        .setHeader("X-Forwarded-For", constant("1.2.3.4"))
                        .to("netty4-http:http://localhost:12312/vp")
                        .to("mock:result");;

                from("netty4-http:http://localhost:12123/vp")
                        .process((Exchange exchange)-> {
                            exchange.setProperty(Exchange.EXCEPTION_CAUGHT, new VpSemanticException(EXCEPTION_MESSAGE, VpSemanticErrorCodeEnum.VP007));
                        })
                        .process(exceptionMessageProcessor);
            }
        };
    }
}
