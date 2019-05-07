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

//  public VpSemanticException createVpSemanticException(VpSemanticErrorCodeEnum codeEnum, String errMsg){
//    return new VpSemanticException(codeEnum +" "+ errMsg, codeEnum);
//  }

  public VpSemanticException createVpSemanticException(VpSemanticErrorCodeEnum codeEnum){
    return createVpSemanticException(codeEnum, null);
  }

  public VpSemanticException createVpSemanticException(VpSemanticErrorCodeEnum codeEnum, Object ...suffix){
    String exceptionMessage = createMessage(codeEnum, suffix);
    return new VpSemanticException(exceptionMessage, codeEnum);
  }

  public String createMessage(VpSemanticErrorCodeEnum codeEnum, Object ...suffix) {
    String errorMsg = vpCodeMessages.getMessage(codeEnum);
    return codeEnum+" "+ String.format(errorMsg, suffix);
  }



}
