package se.skl.tp.vp;

import io.netty.handler.timeout.ReadTimeoutException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skl.tp.vp.certificate.CertificateExtractorProcessor;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.errorhandling.CheckPayloadProcessor;
import se.skl.tp.vp.errorhandling.ExceptionMessageProcessor;
import se.skl.tp.vp.httpheader.HttpHeaderExtractorProcessor;
import se.skl.tp.vp.requestreader.RequestReaderProcessor;
import se.skl.tp.vp.timeout.RequestTimoutProcessor;
import se.skl.tp.vp.vagval.*;
import se.skl.tp.vp.wsdl.WsdlProcessor;

import java.net.SocketException;

import static org.apache.camel.builder.PredicateBuilder.or;

@Component
public class VPRouter extends RouteBuilder {

    public static final String VP_HTTP_ROUTE = "vp-http-route";
    public static final String VP_HTTPS_ROUTE = "vp-https-route";
    public static final String VP_ROUTE = "vp-route";

    public static final String NETTY4_HTTP_FROM = "netty4-http:{{vp.http.route.url}}?matchOnUriPrefix=true";
    public static final String NETTY4_HTTP_TOD = "netty4-http:${exchange.getProperty('vagval')}";
    public static final String DIRECT_VP = "direct:vp";
    public static final String NETTY4_HTTPS_INCOMING_FROM = "netty4-http:{{vp.https.route.url}}?sslContextParameters=#incomingSSLContextParameters&ssl=true&sslClientCertHeaders=true&needClientAuth=true&matchOnUriPrefix=true";
    public static final String NETTY4_HTTPS_OUTGOING_TOD = "netty4-http:${exchange.getProperty('vagval')}?sslContextParameters=#outgoingSSLContextParameters&ssl=true";

    @Autowired
    VagvalProcessor vagvalProcessor;

    @Autowired
    BehorighetProcessor behorighetProcessor;

    @Autowired
    CertificateExtractorProcessor certificateExtractorProcessor;

    @Autowired
    HttpHeaderExtractorProcessor httpHeaderExtractorProcessor;

    @Autowired
    RequestReaderProcessor requestReaderProcessor;

    @Autowired
    ExceptionMessageProcessor exceptionMessageProcessor;

    @Autowired
    CheckPayloadProcessor checkPayloadProcessor;

    @Autowired
    RivTaProfilProcessor rivTaProfilProcessor;

    @Autowired
    WsdlProcessor wsdlProcessor;

    @Autowired
    RequestTimoutProcessor requestTimoutProcessor;

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .process(exceptionMessageProcessor)
                .handled(true);

        from(NETTY4_HTTPS_INCOMING_FROM).routeId(VP_HTTPS_ROUTE)
                .choice().when(header("wsdl").isNotNull())
                    .process(wsdlProcessor)
                .otherwise()
                    .process(certificateExtractorProcessor)
                    .to(DIRECT_VP)
                .end();

        from(NETTY4_HTTP_FROM).routeId(VP_HTTP_ROUTE)
                .choice().when(header("wsdl").isNotNull())
                    .process(wsdlProcessor)
                .otherwise()
                    .process(httpHeaderExtractorProcessor)
                    .to(DIRECT_VP)
                .end();

        from(DIRECT_VP).routeId(VP_ROUTE)
                .streamCaching()
                .process(requestReaderProcessor)
                .process(vagvalProcessor)
                .process(behorighetProcessor)
                .process(requestTimoutProcessor)
                .process(rivTaProfilProcessor)
                .doTry()
                    .choice()
                        .when(exchangeProperty(VPExchangeProperties.VAGVAL).contains("https://"))
                            .toD(NETTY4_HTTPS_OUTGOING_TOD)
                        .otherwise()
                            .toD(NETTY4_HTTP_TOD)
                    .endChoice()
                .endDoTry()
                .doCatch(SocketException.class, ReadTimeoutException.class)
                .end()
                .choice()
                    .when(exchangeProperty(VPExchangeProperties.SESSION_ERROR))
                        .log("Do Nothing")
                    .otherwise()
                        .choice()
                            .when(or(header("http.status").isNotEqualTo(200), body().isNull(), exchangeProperty(Exchange.EXCEPTION_CAUGHT).isNotNull()))
                                .convertBodyTo(String.class)
                                .process(checkPayloadProcessor)
                            .endChoice()
                        .end()
                    .endChoice()
                .end();


    }
}
