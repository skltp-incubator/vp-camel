package se.skl.tp.vp.logging;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.converter.stream.InputStreamCache;
import se.skl.tp.vp.logging.logentry.LogEntry;
import se.skl.tp.vp.logging.logentry.LogLevelType;

@Slf4j
public class MessageInfoLogger {
  public void log(Exchange exchange, String messageType) throws Exception {
    try {
      Object body = exchange.getIn().getBody();

      LogEntry logEntry =  LogEntryBuilder.createLogEntry("", LogLevelType.DEBUG, exchange, "", null);
      log.info(LogMessageFormatter.format("", logEntry));

      if( log.isDebugEnabled() ){

      }


    }catch(Exception e){
      log.error("Failed log message:"+messageType, e);
    }
  }

}
