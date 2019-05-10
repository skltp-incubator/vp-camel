package se.skl.tp.vp.integrationtests.httpheader;

import se.skl.tp.vp.constants.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

import static org.apache.camel.language.constant.ConstantLanguage.constant;

public class HeadersUtil {

    public static String TEST_CONSUMER = "aTestConsumeer";
    public static String TEST_CORRELATION_ID = "aTestCorrelationId";
    public static String TEST_SENDER = "tp";
    public static String TEST_BODY = "aTestBody";

    public static Map getHttpsHeadersWithoutMembers() {
        Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.SOAP_ACTION, "action");
        return headers;
    }

    public static Map getHttpHeadersWithoutMembers() {
        Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.X_VP_SENDER_ID, TEST_SENDER);
        //This param is set by config, but is needed by HttpSenderIdExtractorProcessor.java before that.
        headers.put(HttpHeaders.X_VP_INSTANCE_ID, "dev_env");
        //This header is used as alias for the incoming address, when processing access to vp (whitelist)
        headers.put("X-Forwarded-For", constant("1.2.3.4"));
        headers.put(HttpHeaders.SOAP_ACTION, "action");
        return headers;
    }

    public static Map getHttpsHeadersWithMembers() {
        Map<String, Object> headers = getHttpsHeadersWithoutMembers();
        headers.put(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, TEST_CONSUMER);
        headers.put(HttpHeaders.X_SKLTP_CORRELATION_ID, TEST_CORRELATION_ID);
        return headers;
    }

    public static Map getHttpHeadersWithMembers() {
        Map<String, Object> headers = getHttpHeadersWithoutMembers();
        headers.put(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, TEST_CONSUMER);
        headers.put(HttpHeaders.X_SKLTP_CORRELATION_ID, TEST_CORRELATION_ID);
        return headers;
    }
}
