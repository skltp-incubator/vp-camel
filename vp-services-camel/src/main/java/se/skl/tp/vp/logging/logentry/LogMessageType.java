package se.skl.tp.vp.logging.logentry;

import lombok.Data;

@Data
public class LogMessageType {
  protected LogLevelType level;
  protected String message;
  protected LogMessageExceptionType exception;
}
