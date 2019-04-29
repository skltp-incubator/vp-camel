package se.skl.tp.vp.logging.logentry;

import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.Data;

@Data
public class LogRuntimeInfoType {

  protected String hostName;
  protected String hostIp;
  protected String processId;
  protected String threadId;
  protected String componentId;
  protected String messageId;
  protected String businessCorrelationId;

}
