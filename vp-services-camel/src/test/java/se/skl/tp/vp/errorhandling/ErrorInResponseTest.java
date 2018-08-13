package se.skl.tp.vp.errorhandling;

import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP007;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RIV20;

import java.util.ArrayList;
import java.util.List;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.exceptions.VpSemanticException;
import se.skl.tp.vp.inneTest.TestBeanConfiguration;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;
import se.skltp.takcache.RoutingInfo;
import se.skltp.takcache.TakCache;

@RunWith( SpringRunner.class )
@SpringBootTest(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ErrorInResponseTest extends CamelTestSupport {

    public static final String REMOTE_EXCEPTION_MESSAGE = "Fel fel fel";

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Autowired
    ExceptionMessageProcessor exceptionMessageProcessor;

    @MockBean
    TakCache takCache;

    @Test //Test för när ett SOAP-fault kommer från Producenten
    public void errorInResponseTest() throws Exception {
        List<RoutingInfo> list = new ArrayList<>();
        list.add(new RoutingInfo("http://localhost:12123/vp",RIV20));
        mockRoutingAndAuthorized(list);

        resultEndpoint.expectedBodiesReceived(SoapFaultHelper.generateSoap11FaultWithCause(
            REMOTE_EXCEPTION_MESSAGE));

        template.sendBody(TestSoapRequests.GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST);
        resultEndpoint.assertIsSatisfied();
    }

    @Test //Test för när en Producent inte går att nå
    public void noProducerOnURLResponseTest() throws Exception {
        List<RoutingInfo> list = new ArrayList<>();
        String address = "http://localhost:12100/vp";
        list.add(new RoutingInfo(address,RIV20));
        mockRoutingAndAuthorized(list);

        template.sendBody(TestSoapRequests.GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST);
        String resultBody = resultEndpoint.getExchanges().get(0).getIn().getBody(String.class);
        assertStringContains(resultBody , "VP009");
        assertStringContains(resultBody , "address");
        assertStringContains(resultBody , "Exception Caught by Camel when contacting producer.");
        resultEndpoint.assertIsSatisfied();
    }

    @Test //Test för när en Producent svara med ett tomt svar
    public void emptyResponseTest() throws Exception {
        List<RoutingInfo> list = new ArrayList<>();
        String address = "http://localhost:12124/vp";
        list.add(new RoutingInfo(address,RIV20));
        mockRoutingAndAuthorized(list);

        template.sendBody(TestSoapRequests.GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST);
        String resultBody = resultEndpoint.getExchanges().get(0).getIn().getBody(String.class);
        assertStringContains(resultBody , "VP009");
        assertStringContains(resultBody , "address");
        assertStringContains(resultBody , "Empty message when server responded with status code:");
        resultEndpoint.assertIsSatisfied();
    }

    @Test //Test för när en Producent svarar med annat än SOAP tex ett exception, kontrolleras inte av VP
    public void nonSOAPResponseTest() throws Exception {
        List<RoutingInfo> list = new ArrayList<>();
        String address = "http://localhost:12125/vp";
        list.add(new RoutingInfo(address,RIV20));
        mockRoutingAndAuthorized(list);

        template.sendBody(TestSoapRequests.GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST);
        String resultBody = resultEndpoint.getExchanges().get(0).getIn().getBody(String.class);
        assertStringContains(resultBody , "java.lang.NullPointerException");
        resultEndpoint.assertIsSatisfied();
    }

    private void mockRoutingAndAuthorized(List<RoutingInfo> list) {
        Mockito.when(takCache.getRoutingInfo("urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1", "UnitTest")).thenReturn(list);
        Mockito.when(takCache.isAuthorized("UnitTest", "urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1", "UnitTest")).thenReturn(true);
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
                            exchange.setProperty(Exchange.EXCEPTION_CAUGHT, new VpSemanticException(
                                REMOTE_EXCEPTION_MESSAGE, VP007));
                        })
                        .process(exceptionMessageProcessor);

                from("netty4-http:http://localhost:12124/vp")
                    .process((Exchange exchange)-> {
                        exchange.getOut().setBody("");
                    });

                from("netty4-http:http://localhost:12125/vp")
                    .process((Exchange exchange)-> {
                        try {
                            String.valueOf(null);
                        } catch(NullPointerException e) {
                            exchange.getOut().setBody(e.toString());
                        }
                    });
            }
        };
    }
}