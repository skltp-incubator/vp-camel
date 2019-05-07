package se.skl.tp.vp.logging;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.logging.logentry.LogEntry;
import se.skl.tp.vp.logging.logentry.LogLevelType;
import se.skl.tp.vp.logging.logentry.LogMessageExceptionType;
import se.skl.tp.vp.logging.logentry.LogMessageType;
import se.skl.tp.vp.logging.logentry.LogMetadataInfoType;
import se.skl.tp.vp.logging.logentry.LogRuntimeInfoType;

@Slf4j
public class LogEntryBuilder {

  protected static InetAddress HOST = null;
  protected static String HOST_NAME = "UNKNOWN";
  protected static String HOST_IP = "UNKNOWN";
  protected static String PROCESS_ID = "UNKNOWN";

  static {
    try {
      // Let's give it a try, fail silently...
      HOST = InetAddress.getLocalHost();
      HOST_NAME = HOST.getCanonicalHostName();
      HOST_IP = HOST.getHostAddress();
      PROCESS_ID = ManagementFactory.getRuntimeMXBean().getName();
    } catch (Throwable ex) {
    }
  }

  public static LogEntry createLogEntry(
      LogLevelType logLevel,
      String logMessageType,
      Exchange exchange) {

    LogRuntimeInfoType lri = createRunTimeInfo(exchange);
    LogMetadataInfoType lmi = createMetadataInfo(exchange);
    LogMessageType lm = createLogMessage(logLevel, logMessageType);

    // Create the log entry object
    LogEntry logEntry = new LogEntry();
    logEntry.setMetadataInfo(lmi);
    logEntry.setRuntimeInfo(lri);
    logEntry.setMessageInfo(lm);

    Map<String, String> extraInfo = LogExtraInfoBuilder.createExtraInfo(exchange);
    logEntry.setExtraInfo(extraInfo);

    return logEntry;
  }

  public static LogMessageExceptionType createMessageException(Exchange exchange){
    Throwable throwable = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
    if (throwable == null) {
      return null;
    }

    LogMessageExceptionType lme = new LogMessageExceptionType();
    lme.setExceptionClass(throwable.getClass().getName());
    lme.setExceptionMessage(throwable.getMessage());
    lme.setStackTrace(new ArrayList<>());
    StackTraceElement[] stArr = throwable.getStackTrace();
    // we are just interested in the first lines.
    for (int i = 0; i < stArr.length && i < 10; i++) {
      lme.getStackTrace().add(stArr[i].toString());
    }
    return lme;
  }

  private static LogMessageType createLogMessage(LogLevelType logLevel, String logMessageType) {
    LogMessageType lm = new LogMessageType();
    lm.setLevel(logLevel);
    lm.setMessage(logMessageType);
    return lm;
  }

  private static LogMetadataInfoType createMetadataInfo(Exchange exchange) {

    String serviceImplementation = "";
    String endpoint = "";

    if (exchange != null) {
      serviceImplementation = exchange.getFromRouteId();
      String endpointURI = exchange.getProperty(VPExchangeProperties.HTTP_URL_IN, String.class);
      endpoint = (endpointURI == null) ? "" : endpointURI;

    }

    LogMetadataInfoType lmi = new LogMetadataInfoType();
    lmi.setServiceImplementation(serviceImplementation);
    lmi.setEndpoint(endpoint);

    return lmi;
  }

  private static LogRuntimeInfoType createRunTimeInfo(Exchange exchange) {
    String messageId = "";
    String businessCorrelationId = "";
    String componentId = "";

    if (exchange != null) {
      businessCorrelationId = exchange.getProperty(VPExchangeProperties.SKLTP_CORRELATION_ID, "", String.class);
      messageId = exchange.getMessage().getMessageId();

      // TODO In current vp this is set by muleContext.getConfiguration().getId() which results in "vp-services"
      componentId = exchange.getContext().getName();
    }

    LogRuntimeInfoType lri = new LogRuntimeInfoType();
    lri.setHostName(HOST_NAME);
    lri.setHostIp(HOST_IP);
    lri.setProcessId(PROCESS_ID);
    lri.setThreadId(Thread.currentThread().getName());
    lri.setComponentId(componentId);
    lri.setMessageId(messageId);
    lri.setBusinessCorrelationId(businessCorrelationId);
    return lri;
  }


}
