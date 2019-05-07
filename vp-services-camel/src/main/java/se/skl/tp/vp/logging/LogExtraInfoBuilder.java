package se.skl.tp.vp.logging;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;

public class LogExtraInfoBuilder {

  public static final String SENDER_ID = VPExchangeProperties.SENDER_ID;
  public static final String RECEIVER_ID = VPExchangeProperties.RECEIVER_ID;
  public static final String RIV_VERSION = VPExchangeProperties.RIV_VERSION;
  public static final String SERVICECONTRACT_NAMESPACE = VPExchangeProperties.SERVICECONTRACT_NAMESPACE;
  public static final String WSDL_NAMESPACE = "wsdl_namespace";
  public static final String SENDER_IP_ADRESS = VPExchangeProperties.SENDER_IP_ADRESS;
  public static final String VAGVAL_TRACE = VPExchangeProperties.VAGVAL_TRACE;
  public static final String ANROPSBEHORIGHET_TRACE = VPExchangeProperties.ANROPSBEHORIGHET_TRACE;
  public static final String OUT_ORIGINAL_SERVICE_CONSUMER_HSA_ID = "originalServiceconsumerHsaid";
  public static final String IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID = "originalServiceconsumerHsaid_in";
  public static final String TIME_ELAPSED = "time.elapsed";
  public static final String TIME_PRODUCER = "time.producer";
  public static final String HEADERS = "Headers";
  public static final String ENDPOINT_URL = "endpoint_url";
  public static final String SOURCE = "source";
  public static final String VP_X_FORWARDED_HOST = VPExchangeProperties.VP_X_FORWARDED_HOST;
  public static final String VP_X_FORWARDED_PROTO = VPExchangeProperties.VP_X_FORWARDED_PROTO;
  public static final String VP_X_FORWARDED_PORT = VPExchangeProperties.VP_X_FORWARDED_PORT;



  public static Map<String, String> createExtraInfo(Exchange exchange) {
    ExtraInfoMap<String, String> extraInfo = new ExtraInfoMap<>();

    extraInfo.put(SENDER_ID, exchange.getProperty(VPExchangeProperties.SENDER_ID, String.class));
    extraInfo.put(RECEIVER_ID, exchange.getProperty(VPExchangeProperties.RECEIVER_ID, String.class));
    extraInfo.put(OUT_ORIGINAL_SERVICE_CONSUMER_HSA_ID,
        exchange.getProperty(VPExchangeProperties.OUT_ORIGINAL_SERVICE_CONSUMER_HSA_ID, String.class));
    extraInfo.putNotEmpty(IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID,
        nullValue2Blank(exchange.getProperty(VPExchangeProperties.IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID, String.class)));
    extraInfo.put(SENDER_IP_ADRESS, exchange.getProperty(VPExchangeProperties.SENDER_IP_ADRESS, String.class));

    String serviceContractNS = exchange.getProperty(VPExchangeProperties.SERVICECONTRACT_NAMESPACE, String.class);
    String rivVersion = exchange.getProperty(VPExchangeProperties.RIV_VERSION, String.class);
    extraInfo.put(SERVICECONTRACT_NAMESPACE, serviceContractNS);
    extraInfo.put(RIV_VERSION, rivVersion);
    extraInfo.put(WSDL_NAMESPACE, createWsdlNamespace(serviceContractNS, rivVersion));

    extraInfo.put(HEADERS, mapAsString(exchange.getIn().getHeaders()));

    extraInfo.put(TIME_ELAPSED, getElapsedTime(exchange).toString());

    extraInfo.putNotNull(VAGVAL_TRACE, exchange.getProperty(VPExchangeProperties.VAGVAL_TRACE, String.class));
    extraInfo.putNotNull(ANROPSBEHORIGHET_TRACE, exchange.getProperty(VPExchangeProperties.ANROPSBEHORIGHET_TRACE, String.class));
    extraInfo.putNotNull(ENDPOINT_URL, exchange.getProperty(VPExchangeProperties.VAGVAL, String.class));

    String timeProducer = exchange.getIn().getHeader(HttpHeaders.X_SKLTP_PRODUCER_RESPONSETIME, String.class);
    extraInfo.putNotNull(TIME_PRODUCER, timeProducer);

    addHttpForwardHeaders(exchange, extraInfo);

    final Boolean isError = exchange.getProperty(VPExchangeProperties.SESSION_ERROR, Boolean.class);
    if (isError!=null && isError==true) {
      addErrorInfo(exchange, extraInfo);
    }
    return extraInfo;
  }

  private static void addHttpForwardHeaders(Exchange exchange, ExtraInfoMap<String, String> extraInfo) {
    // Following should be logged only once

    String httpXForwardedProto = exchange.getProperty(VPExchangeProperties.VP_X_FORWARDED_PROTO, String.class);
    if (httpXForwardedProto != null) {
      extraInfo.put(VP_X_FORWARDED_PROTO, httpXForwardedProto);
      exchange.removeProperty(VPExchangeProperties.VP_X_FORWARDED_PROTO);
    }
    String httpXForwardedHost = exchange.getProperty(VPExchangeProperties.VP_X_FORWARDED_HOST, String.class);
    if (httpXForwardedHost != null) {
      extraInfo.put(VP_X_FORWARDED_HOST, httpXForwardedHost);
      exchange.removeProperty(VPExchangeProperties.VP_X_FORWARDED_HOST);
    }
    String httpXForwardedPort = exchange.getProperty(VPExchangeProperties.VP_X_FORWARDED_PORT, String.class);
    if (httpXForwardedPort != null) {
      extraInfo.put(VP_X_FORWARDED_PORT, httpXForwardedPort);
      exchange.removeProperty(VPExchangeProperties.VP_X_FORWARDED_PORT);
    }
  }

  private static String createWsdlNamespace(String serviceContractNS, String profile) {
    //  Convert from interaction target namespace
    //    urn:${domänPrefix}:${tjänsteDomän}:${tjänsteInteraktion}${roll}:${m}
    //  to wsdl target namespace
    //    urn:riv:${tjänsteDomän}:${tjänsteInteraktion}:m:${profilKortnamn}
    // See https://riv-ta.atlassian.net/wiki/spaces/RTA/pages/99593635/RIV+Tekniska+Anvisningar+Tj+nsteschema
    //   and https://riv-ta.atlassian.net/wiki/spaces/RTA/pages/77856888/RIV+Tekniska+Anvisningar+Basic+Profile+2.1
    if(serviceContractNS==null || profile==null){
      return null;
    }
    return serviceContractNS.replace("Responder", "").concat(":").concat(profile);
  }

  private static Long getElapsedTime(Exchange exchange) {
    Date created = exchange.getProperty(Exchange.CREATED_TIMESTAMP, Date.class);
    return new Date().getTime() - created.getTime();
  }

  private static void addErrorInfo(Exchange exchange, ExtraInfoMap<String, String> extraInfo) {
    extraInfo.put(VPExchangeProperties.SESSION_ERROR, "true");
    extraInfo.put(VPExchangeProperties.SESSION_ERROR_DESCRIPTION,
        nullValue2Blank(exchange.getProperty(VPExchangeProperties.SESSION_ERROR_DESCRIPTION, String.class)));
    extraInfo.put(VPExchangeProperties.SESSION_ERROR_TECHNICAL_DESCRIPTION,
        nullValue2Blank(exchange.getProperty(VPExchangeProperties.SESSION_ERROR_TECHNICAL_DESCRIPTION, String.class)));
    extraInfo.put(VPExchangeProperties.SESSION_ERROR_CODE,
        nullValue2Blank(exchange.getProperty(VPExchangeProperties.SESSION_ERROR_CODE, String.class)));
    extraInfo.putNotEmpty(VPExchangeProperties.SESSION_HTML_STATUS,  exchange.getProperty(VPExchangeProperties.SESSION_HTML_STATUS, String.class) );
  }

  private static String nullValue2Blank(String s) {
    return (s == null) ? "" : s;
  }

  private static String mapAsString(Map<String, ?> map) {
    return map.keySet().stream()
        .map(key -> key + "=" + map.get(key))
        .collect(Collectors.joining(", ", "{", "}"));
  }

  private static class ExtraInfoMap<K,V> extends HashMap <K,V>{
    public V putNotNull(K key, V value){
      return value==null ? null : put(key, value);
    }

    public V putNotEmpty(K key, V value){
      return (value==null || ((String)value).isEmpty()) ? null : put(key, value);
    }
  }
}
