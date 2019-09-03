package se.skl.tp.vp.errorhandling;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.netty4.http.NettyHttpOperationFailedException;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;

@Service
@Log4j2
public class HandleProducerExceptionProcessor implements Processor {

  private ExceptionUtil exceptionUtil;
  private static final String SOAP_XMLNS = "http://schemas.xmlsoap.org/soap/envelope/";
  private static final Integer HTTP_STATUS_500 = 500;

  @Autowired
  public HandleProducerExceptionProcessor(ExceptionUtil exceptionUtil) {
    this.exceptionUtil = exceptionUtil;
  }

  @Override
  public void process(Exchange exchange) throws Exception {
    try {
      Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
      if (exception != null && !shouldPassContentFromProducer(exchange, exception) ){

        String message = getExceptionMessage(exception);
        log.debug("Exception Caught by Camel when contacting producer. Exception information: " + left(message, 200) + "...");

        String addr = (String) exchange.getProperty(VPExchangeProperties.VAGVAL, "<UNKNOWN>");
        String vpMsg = String.format("%s. Exception Caught by Camel when contacting producer. Exception information: (%s: %s)",
            addr, exception.getClass().getName(), getExceptionMessage(exception));
        String cause = exceptionUtil.createMessage(VpSemanticErrorCodeEnum.VP009, vpMsg);
        SoapFaultHelper.setSoapFaultInResponse(exchange, cause, VpSemanticErrorCodeEnum.VP009.toString());
      }
    } catch (Exception e) {
      log.error("An error occured in HandleProducerExceptionProcessor", e);
      throw exceptionUtil.createVpSemanticException(VpSemanticErrorCodeEnum.VP009, "unknown");
    }

  }

  private String getExceptionMessage(Exception exception) {
    if (exception instanceof ReadTimeoutException) {
     return  "Timeout when waiting on response from producer.";
    }
    return exception.getMessage();
  }

  private boolean shouldPassContentFromProducer(Exchange exchange, Exception exception) {
    if (exception instanceof NettyHttpOperationFailedException) {
      NettyHttpOperationFailedException operationFailedException = (NettyHttpOperationFailedException) exception;
      return operationFailedException.getStatusCode() == HTTP_STATUS_500 && operationFailedException.getContentAsString().contains(SOAP_XMLNS);
    } if(exception instanceof HttpOperationFailedException) {
      HttpOperationFailedException operationFailedException = (HttpOperationFailedException) exception;
      if(operationFailedException.getStatusCode() == HTTP_STATUS_500 && operationFailedException.getResponseBody().contains(SOAP_XMLNS)){
        exchange.getIn().setBody(operationFailedException.getResponseBody());
        return true;
      }
    }
    return false;
  }

  private String left(String s, int len) {
    if (s == null) {
      return null;
    }

    int i = s.length() > len ? len : s.length();
    return s.substring(0, i);
  }


}
