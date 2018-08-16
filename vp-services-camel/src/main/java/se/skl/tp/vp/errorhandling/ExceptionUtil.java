package se.skl.tp.vp.errorhandling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.exceptions.VpSemanticException;

@Service
public class ExceptionUtil {

  VpCodeMessages vpCodeMessages;

  private static final Logger LOGGER = LogManager.getLogger(ExceptionUtil.class);

  @Autowired
  public ExceptionUtil(VpCodeMessages vpCodeMessages) {
    this.vpCodeMessages = vpCodeMessages;
  }

  public void raiseError(boolean test, VpSemanticErrorCodeEnum codeenum) {
    if(test) {
      raiseError(codeenum, null);
    }
  }

  public void raiseError(boolean test, VpSemanticErrorCodeEnum codeenum, Object ... suffix) {
    if(test) {
      raiseError(codeenum, suffix);
    }
  }

  public void raiseError(VpSemanticErrorCodeEnum codeEnum, Object ...suffix) {
    String errmsg = String.format( vpCodeMessages.getMessage(codeEnum), suffix);

    LOGGER.error(codeEnum +" "+errmsg);
    throw new VpSemanticException(codeEnum +" "+ errmsg, codeEnum);
  }

}
