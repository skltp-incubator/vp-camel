package se.skl.tp.vp.errorhandling;

import org.apache.camel.Exchange;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import se.skl.tp.vp.constants.VPExchangeProperties;

public class SoapFaultHelper {

    /*
     * Generic soap fault template, just use String.format(SOAP_FAULT, message);
     */
    private final static String SOAP_FAULT =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                    "  <soapenv:Header/>" +
                    "  <soapenv:Body>" +
                    "    <soap:Fault xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                    "      <faultcode>soap:Server</faultcode>\n" +
                    "      <faultstring>%s</faultstring>\n" +
                    "    </soap:Fault>" +
                    "  </soapenv:Body>" +
                    "</soapenv:Envelope>";



    public static String generateSoap11FaultWithCause(String cause) {
        return String.format(SOAP_FAULT, escape(cause));
    }

    private static final String escape(final String string) {
        return StringEscapeUtils.escapeXml(string);
    }

    public static String getStatusMessage(String code, String defaultReason) {

        if(code == null || code.length() == 0)
            return defaultReason;

        try {
            Integer intCode = Integer.valueOf(code);
            String reason = HttpStatus.valueOf(intCode).getReasonPhrase();
            return code + " " + reason;
        } catch(Exception e) {
            return code;
        }

    }

    public static String nvl(Object s) {
        return (s == null) ? "" : s.toString();
    }
}
