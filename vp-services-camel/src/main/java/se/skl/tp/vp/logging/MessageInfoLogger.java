package se.skl.tp.vp.logging;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import se.skl.tp.vp.logging.logentry.LogEntry;
import se.skl.tp.vp.logging.logentry.LogLevelType;


public class MessageInfoLogger {

  private static final Logger LOGGER_REQ_IN = org.slf4j.LoggerFactory.getLogger("se.skl.tp.vp.logging.req.in");
  private static final Logger LOGGER_REQ_OUT = org.slf4j.LoggerFactory.getLogger("se.skl.tp.vp.logging.req.out");
  private static final Logger LOGGER_RESP_IN = org.slf4j.LoggerFactory.getLogger("se.skl.tp.vp.logging.resp.in");
  private static final Logger LOGGER_RESP_OUT = org.slf4j.LoggerFactory.getLogger("se.skl.tp.vp.logging.resp.out");
  private static final Logger LOGGER_ERROR = org.slf4j.LoggerFactory.getLogger("se.skl.tp.vp.logging.error");

  private static final String LOG_EVENT_INFO = "logEvent-info";
  private static final String LOG_EVENT_ERROR = "logEvent-error";
  private static final String LOG_EVENT_DEBUG = "logEvent-debug";

  private static final String MSG_TYPE_LOG_REQ_IN = "xreq-in";
  private static final String MSG_TYPE_LOG_REQ_OUT = "xreq-out";
  private static final String MSG_TYPE_LOG_RESP_IN = "xresp-in";
  private static final String MSG_TYPE_LOG_RESP_OUT = "xresp-out";
  private static final String MSG_TYPE_ERROR = "error";


  public void logReqIn(Exchange exchange) throws Exception {
    log(LOGGER_REQ_IN, exchange, MSG_TYPE_LOG_REQ_IN);
  }

  public void logReqOut(Exchange exchange) throws Exception {
    log(LOGGER_REQ_OUT, exchange, MSG_TYPE_LOG_REQ_OUT);
  }

  public void logRespIn(Exchange exchange) throws Exception {
    log(LOGGER_RESP_IN, exchange, MSG_TYPE_LOG_RESP_IN);
  }

  public void logRespOut(Exchange exchange) throws Exception {
    log(LOGGER_RESP_OUT, exchange, MSG_TYPE_LOG_RESP_OUT);
  }

  public void logError(Exchange exchange) throws Exception {

    try {
      LogEntry logEntry = LogEntryBuilder.createLogEntry(LogLevelType.ERROR, MSG_TYPE_ERROR, exchange);
      logEntry.getExtraInfo().put(LogExtraInfoBuilder.SOURCE, getClass().getName());
      logEntry.getMessageInfo().setException(LogEntryBuilder.createMessageException(exchange));
      LOGGER_ERROR.error(LogMessageFormatter.format(LOG_EVENT_ERROR, logEntry));

    } catch (Exception e) {
      LOGGER_ERROR.error("Failed log message:" + MSG_TYPE_ERROR, e);
    }
  }


  public void log(Logger log, Exchange exchange, String messageType) throws Exception {
    try {
      if (log.isDebugEnabled()) {
        LogEntry logEntry = LogEntryBuilder.createLogEntry(LogLevelType.DEBUG, messageType, exchange);
        logEntry.getExtraInfo().put(LogExtraInfoBuilder.SOURCE, getClass().getName());
        logEntry.setPayload(exchange.getIn().getBody(String.class));
        log.debug(LogMessageFormatter.format(LOG_EVENT_DEBUG, logEntry));
      } else if (log.isInfoEnabled()) {
        LogEntry logEntry = LogEntryBuilder.createLogEntry(LogLevelType.INFO, messageType, exchange);
        logEntry.getExtraInfo().put(LogExtraInfoBuilder.SOURCE, getClass().getName());
        log.info(LogMessageFormatter.format(LOG_EVENT_INFO, logEntry));
      }

    } catch (Exception e) {
      log.error("Failed log message:" + messageType, e);
    }
  }

}
