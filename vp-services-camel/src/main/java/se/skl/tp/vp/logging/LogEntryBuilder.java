package se.skl.tp.vp.logging;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
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

  protected static LogEntry createLogEntry(
      String loggerName,
      LogLevelType logLevel,
      Exchange exchange,
      String logMessage,
      Throwable exception) {

    LogRuntimeInfoType lri = createRunTimeInfo(exchange);
    LogMetadataInfoType lmi = createMetadataInfo(exchange, loggerName);
    LogMessageType lm = createLogMessage(logLevel, logMessage, exception);

    // Create the log entry object
    LogEntry logEntry = new LogEntry();
    logEntry.setMetadataInfo(lmi);
    logEntry.setRuntimeInfo(lri);
    logEntry.setMessageInfo(lm);

    Map<String, String> extraInfo = ExtraInfoBuilder.createExtraInfo(exchange);
    logEntry.setExtraInfo(extraInfo);

    return logEntry;
  }

  private static LogMessageType createLogMessage(LogLevelType logLevel, String logMessage, Throwable exception) {
    LogMessageType lm = new LogMessageType();
    lm.setLevel(logLevel);
    lm.setMessage(logMessage);

    // Setup exception information if present
    if (exception != null) {
      LogMessageExceptionType lme = new LogMessageExceptionType();

      lme.setExceptionClass(exception.getClass().getName());
      lme.setExceptionMessage(exception.getMessage());
      StackTraceElement[] stArr = exception.getStackTrace();
      // we are just interested in the first lines.
      for (int i = 0; i < stArr.length && i < 10; i++) {
        lme.getStackTrace().add(stArr[i].toString());
      }
      lm.setException(lme);
    }

    return lm;
  }

  private static LogMetadataInfoType createMetadataInfo(Exchange exchange, String loggerName) {

    String serviceImplementation = "";
    String endpoint = "";
    String propertyBusinessContextId = null;

    if (exchange != null) {
      serviceImplementation = exchange.getFromRouteId();
      Endpoint endpointURI = exchange.getFromEndpoint();
      endpoint = (endpointURI == null) ? "" : endpointURI.getEndpointUri();
    }

    LogMetadataInfoType lmi = new LogMetadataInfoType();
    lmi.setLoggerName(loggerName);
    lmi.setServiceImplementation(serviceImplementation);
    lmi.setEndpoint(endpoint);

    return lmi;
  }

  private static LogRuntimeInfoType createRunTimeInfo(Exchange exchange) {
    String messageId = "";
    String businessCorrelationId = "";
    String componentId = "";

    if (exchange != null) {
      businessCorrelationId = (String) exchange.getProperty(VPExchangeProperties.SKLTP_CORRELATION_ID, "");
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
