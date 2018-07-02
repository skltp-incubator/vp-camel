package se.skl.tp.vp.errorhandling;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;

@Service
public class ExceptionMessageProcessorImpl implements ExceptionMessageProcessor{

    @Override
    public void process(Exchange exchange) throws Exception {
        Throwable throwable = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);

        String message = throwable.getMessage();
        String cause = SoapFaultHelper.generateSoap11FaultWithCause(message);
        exchange.getOut().setBody(cause);
        exchange.getOut().setHeader("http.status", 500);
    }


}
