package se.skl.tp.vp;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skl.tp.vp.certificate.CertificateExtractorProcessor;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.httpheader.HttpHeaderExtractorProcessor;
import se.skl.tp.vp.requestreader.RequestReaderProcessor;
import se.skl.tp.vp.vagval.BehorighetProcessor;
import se.skl.tp.vp.vagval.VagvalProcessor;

@Component
public class VPRouter extends RouteBuilder {

    public static final String VP_HTTP_ROUTE = "vp-http-route";
    public static final String VP_ROUTE = "vp-route";
    public static final String NETTY4_HTTP_FROM = "netty4-http:{{vp.http.route.url}}";
    public static final String NETTY4_HTTP_TOD = "netty4-http:${exchange.getProperty('vagval')}";
    public static final String DIRECT_VP = "direct:vp";
    public static final String VP_HTTPS_ROUTE = "vp-https-route";
    public static final String NETTY4_HTTPS_INCOMING_FROM = "netty4-http:{{vp.https.route.url}}?sslContextParameters=#incomingSSLContextParameters&ssl=true&sslClientCertHeaders=true&needClientAuth=true";
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

    @Override
    public void configure() throws Exception {
        from(NETTY4_HTTPS_INCOMING_FROM).routeId(VP_HTTPS_ROUTE)
                .process(certificateExtractorProcessor)
                .to(DIRECT_VP);

        from(NETTY4_HTTP_FROM).routeId(VP_HTTP_ROUTE)
                .process(httpHeaderExtractorProcessor)
                .to(DIRECT_VP);

        from(DIRECT_VP).routeId(VP_ROUTE)
                .process(requestReaderProcessor)
                .process(vagvalProcessor)
                .process(behorighetProcessor)
                .choice()
                    .when(exchangeProperty(VPExchangeProperties.VAGVAL).contains("https://"))
                        .toD(NETTY4_HTTPS_OUTGOING_TOD)
                    .otherwise()
                        .toD(NETTY4_HTTP_TOD)
                .endChoice();
    }
}
