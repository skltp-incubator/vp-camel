package se.skl.tp.vp.logging;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import org.slf4j.helpers.MessageFormatter;
import se.skl.tp.vp.logging.logentry.LogEntry;
import se.skl.tp.vp.logging.logentry.LogMessageExceptionType;
import se.skl.tp.vp.logging.logentry.LogMessageType;
import se.skl.tp.vp.logging.logentry.LogMetadataInfoType;
import se.skl.tp.vp.logging.logentry.LogRuntimeInfoType;

public class LogMessageFormatter {

  private static final String MSG_ID = "soi-toolkit.log"; // TODO Change this??
  private static final String LOG_STRING = MSG_ID +
      "\n** {}.start ***********************************************************" +
      "\nIntegrationScenarioId={}\nContractId={}\nLogMessage={}\nServiceImpl={}\nHost={} ({})\nComponentId={}\nEndpoint={}\nMessageId={}\nBusinessCorrelationId={}\nBusinessContextId={}\nExtraInfo={}\nPayload={}" +
      "{}" + // Placeholder for stack trace info if an error is logged
      "\n** {}.end *************************************************************";

  protected static InetAddress HOST = null;
  protected static String HOST_NAME = "UNKNOWN";
  protected static String HOST_IP = "UNKNOWN";
  protected static String PROCESS_ID = "UNKNOWN";

  static {
    try {
      // Let's give it a try, fail silently...
      HOST       = InetAddress.getLocalHost();
      HOST_NAME  = HOST.getCanonicalHostName();
      HOST_IP    = HOST.getHostAddress();
      PROCESS_ID = ManagementFactory.getRuntimeMXBean().getName();
    } catch (Throwable ex) {
    }
  }

  protected static String format(String logEventName, LogEntry logEntry) {
    LogMessageType messageInfo  = logEntry.getMessageInfo();
    LogMetadataInfoType metadataInfo = logEntry.getMetadataInfo();
    LogRuntimeInfoType runtimeInfo  = logEntry.getRuntimeInfo();

    String integrationScenarioId   = "";
    String contractId              = "";
    String logMessage              = messageInfo.getMessage();
    String serviceImplementation   = metadataInfo.getServiceImplementation();
    String componentId             = runtimeInfo.getComponentId();
    String endpoint                = metadataInfo.getEndpoint();
    String messageId               = runtimeInfo.getMessageId();
    String businessCorrelationId   = runtimeInfo.getBusinessCorrelationId();
    String payload                 = logEntry.getPayload();
    String businessContextIdString = "";
    String extraInfoString         = extraInfoToString(logEntry.getExtraInfo());

    StringBuffer stackTrace = new StringBuffer();
    LogMessageExceptionType lmeException = logEntry.getMessageInfo().getException();
    if (lmeException != null) {
      String ex = lmeException.getExceptionClass();
      String msg = lmeException.getExceptionMessage();
      List<String> st = lmeException.getStackTrace();

      stackTrace.append('\n').append("Stacktrace=").append(ex).append(": ").append(msg);
      for (String stLine : st) {
        stackTrace.append('\n').append("\t at ").append(stLine);
      }
    }
    return MessageFormatter
        .arrayFormat(LOG_STRING, new String[] {logEventName, integrationScenarioId, contractId, logMessage, serviceImplementation, HOST_NAME, HOST_IP, componentId, endpoint, messageId, businessCorrelationId, businessContextIdString, extraInfoString, payload, stackTrace.toString(), logEventName}).getMessage();
  }


  private static String extraInfoToString(Map extraInfo) {

    if (extraInfo == null) {
      return "";
    }

    StringBuffer extraInfoString = new StringBuffer();
    extraInfo.forEach((k,v)->extraInfoString.append("\n-").append(k).append("=").append(v));
    return extraInfoString.toString();
  }

}
