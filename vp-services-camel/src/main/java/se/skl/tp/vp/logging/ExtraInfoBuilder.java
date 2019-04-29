package se.skl.tp.vp.logging;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import se.skl.tp.vp.constants.VPExchangeProperties;

public class ExtraInfoBuilder {

  public static Map<String, String> createExtraInfo(Exchange exchange) {
    Map<String, String> extraInfo = new HashMap<>();

    extraInfo.put(VPExchangeProperties.SENDER_ID,
        (String) exchange.getProperty(VPExchangeProperties.SENDER_ID));
    extraInfo.put(VPExchangeProperties.RECEIVER_ID,
        (String) exchange.getProperty(VPExchangeProperties.RECEIVER_ID));
    extraInfo.put(VPExchangeProperties.ORIGINAL_SERVICE_CONSUMER_HSA_ID,
        (String) exchange.getProperty(VPExchangeProperties.ORIGINAL_SERVICE_CONSUMER_HSA_ID));
    extraInfo.put(VPExchangeProperties.RIV_VERSION,
        (String) exchange.getProperty(VPExchangeProperties.RIV_VERSION));
    extraInfo.put(VPExchangeProperties.SERVICECONTRACT_NAMESPACE,
        (String) exchange.getProperty(VPExchangeProperties.SERVICECONTRACT_NAMESPACE));
    extraInfo.put(VPExchangeProperties.SENDER_IP_ADRESS,
        (String) exchange.getProperty(VPExchangeProperties.SENDER_IP_ADRESS));

    // TODO We have no access to wsdl namespace
//    extraInfo.put(VPUtil.WSDL_NAMESPACE, (String) exchange.getProperty(VPExchangeProperties.WSDL_NAMESPACE));

    // TODO Fix headers below
//    String httpXForwardedProto = exchange.getInvocationProperty(VPExchangeProperties.VP_X_FORWARDED_PROTO);
//    if (httpXForwardedProto != null) {
//      extraInfo.put(HttpHeaders.VP_X_FORWARDED_PROTO, httpXForwardedProto);
//      // only log on first occasion
//      exchange.removeProperty(HttpHeaders.VP_X_FORWARDED_PROTO, PropertyScope.INVOCATION);
//    }
//    String httpXForwardedHost = exchange.getInvocationProperty(VPUtil.VP_X_FORWARDED_HOST);
//    if (httpXForwardedHost != null) {
//      extraInfo.put(HttpHeaders.VP_X_FORWARDED_HOST, httpXForwardedHost);
//      // only log on first occasion
//      exchange.removeProperty(HttpHeaders.VP_X_FORWARDED_HOST, PropertyScope.INVOCATION);
//    }
//    String httpXForwardedPort = exchange.getInvocationProperty(VPUtil.VP_X_FORWARDED_PORT);
//    if (httpXForwardedPort != null) {
//      extraInfo.put(HttpHeaders.VP_X_FORWARDED_PORT, httpXForwardedPort);
//      // only log on first occasion
//      exchange.removeProperty(HttpHeaders.VP_X_FORWARDED_PORT, PropertyScope.INVOCATION);
//    }

    // extract MDC data
    if (ThreadContextLogTrace.get(ThreadContextLogTrace.ROUTER_RESOLVE_VAGVAL_TRACE) != null) {
      extraInfo.put(ThreadContextLogTrace.ROUTER_RESOLVE_VAGVAL_TRACE,
          ThreadContextLogTrace.get(ThreadContextLogTrace.ROUTER_RESOLVE_VAGVAL_TRACE));
      extraInfo.put(ThreadContextLogTrace.ROUTER_RESOLVE_ANROPSBEHORIGHET_TRACE,
          ThreadContextLogTrace.get(ThreadContextLogTrace.ROUTER_RESOLVE_ANROPSBEHORIGHET_TRACE));
    }

    // TODO fix endpoint url
//    putPropertyIfNotNull(extraInfo, exchange, VPExchangeProperties.ENDPOINT_URL);

    //TODO fix error logging
//    final Boolean isError = (Boolean) exchange.getProperty(VPExchangeProperties.SESSION_ERROR);
//    if (isError) {
//      addErrorInfo(exchange, extraInfo, isError);
//    }
    return extraInfo;
  }

  private static void addErrorInfo(Exchange exchange, Map<String, String> extraInfo, Boolean isError) {
    extraInfo.put(VPExchangeProperties.SESSION_ERROR, isError.toString());
    extraInfo.put(VPExchangeProperties.SESSION_ERROR_DESCRIPTION,
        nullValue2Blank((String) exchange.getProperty(VPExchangeProperties.SESSION_ERROR_DESCRIPTION)));
    extraInfo.put(VPExchangeProperties.SESSION_ERROR_TECHNICAL_DESCRIPTION,
        nullValue2Blank((String) exchange.getProperty(VPExchangeProperties.SESSION_ERROR_TECHNICAL_DESCRIPTION)));
    extraInfo.put(VPExchangeProperties.SESSION_ERROR_CODE,
        nullValue2Blank((String) exchange.getProperty(VPExchangeProperties.SESSION_ERROR_CODE)));
    putPropertyIfNotEmpty(extraInfo, exchange, VPExchangeProperties.SESSION_HTML_STATUS);
  }

  public static String nullValue2Blank(String s) {
    return (s == null) ? "" : s;
  }

  public static void putPropertyIfNotEmpty(Map map, Exchange exchange, String propertyName) {
    String value = (String)exchange.getProperty(propertyName);
    if (value != null && !value.isEmpty()) {
      map.put(propertyName, value);
    }
  }

  public static void putPropertyIfNotNull(Map map, Exchange exchange, String propertyName) {
    String value = (String)exchange.getProperty(propertyName);
    if (value != null) {
      map.put(propertyName, value);
    }
  }
}
