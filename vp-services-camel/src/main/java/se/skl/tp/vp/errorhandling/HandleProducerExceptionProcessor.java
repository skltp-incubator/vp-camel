package se.skl.tp.vp.errorhandling;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;

@Service
@Log4j2
public class HandleProducerExceptionProcessor implements Processor {

  ExceptionUtil exceptionUtil;

  @Autowired
  public HandleProducerExceptionProcessor(ExceptionUtil exceptionUtil) {
    this.exceptionUtil = exceptionUtil;
  }

  @Override
  public void process(Exchange exchange) throws Exception {

    try {

      Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
      if (exception != null) {
        String message = exception.getMessage();
        if (exception instanceof ReadTimeoutException) {
          message = "Timeout when waiting on response from producer.";
        }

        log.debug("Exception Caught by Camel when contacting producer. Exception information: " + left(message, 200) + "...");
        String addr = (String) exchange.getProperty(VPExchangeProperties.VAGVAL, "<UNKNOWN>");
        String vpMsg = String.format("%s. Exception Caught by Camel when contacting producer. Exception information: (%s: %s)",
            addr, exception.getClass().getName(), message);
        String cause = exceptionUtil.createMessage(VpSemanticErrorCodeEnum.VP009, vpMsg);
        SoapFaultHelper.setSoapFaultInResponse(exchange, cause, VpSemanticErrorCodeEnum.VP009.toString());
      }
    } catch (Exception e) {
      log.error("An error occured in HandleProducerExceptionProcessor", e);
      throw exceptionUtil.createVpSemanticException(VpSemanticErrorCodeEnum.VP009, "unknown");
    }

  }

  private String left(String s, int len) {
    if (s == null) {
      return null;
    }

    int i = s.length() > len ? len : s.length();
    return s.substring(0, i);
  }


}
