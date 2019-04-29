package se.skl.tp.vp.logging.logentry;

import java.util.List;
import lombok.Data;

@Data
public class LogMessageExceptionType {
  protected String exceptionClass;
  protected String exceptionMessage;
  protected List<String> stackTrace;
}
