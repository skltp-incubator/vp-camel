package se.skl.tp.vp.integrationtests.httpheader;

import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;

import java.util.HashMap;
import java.util.Map;

import static org.apache.camel.language.constant.ConstantLanguage.constant;

public class HeadersUtil {

    public static Map getHttpsHeadersWithoutMembers() {
        Map<String, Object> headers = new HashMap<>();
        return headers;
    }

    public static Map getHttpHeadersWithoutMembers() {
        Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.X_VP_SENDER_ID, "tp");
        headers.put(HttpHeaders.X_VP_INSTANCE_ID, "dev_env");
        headers.put("X-Forwarded-For", constant("1.2.3.4"));
        return headers;
    }

    public static Map getHttpsHeadersWithMembers() {
        Map<String, Object> headers = getHttpsHeadersWithoutMembers();
        headers.put(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, "aTestConsumer");
        headers.put(HttpHeaders.X_SKLTP_CORRELATION_ID, "aTestCorrelationId");
        return headers;
    }

    public static Map getHttpHeadersWithMembers() {
        Map<String, Object> headers = getHttpHeadersWithoutMembers();
        headers.put(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, "aTestConsumer");
        headers.put(HttpHeaders.X_SKLTP_CORRELATION_ID, "aTestCorrelationId");
        return headers;
    }
}
