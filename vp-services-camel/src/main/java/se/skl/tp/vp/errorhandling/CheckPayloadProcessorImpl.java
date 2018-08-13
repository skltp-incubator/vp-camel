package se.skl.tp.vp.errorhandling;

import static se.skl.tp.vp.errorhandling.SoapFaultHelper.nvl;

import io.netty.handler.timeout.ReadTimeoutException;
import org.apache.camel.Exchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.certificate.HeaderCertificateHelperImpl;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;

@Service
public class CheckPayloadProcessorImpl implements CheckPayloadProcessor {

    private static Logger LOGGER = LogManager.getLogger(HeaderCertificateHelperImpl.class);

    private static final String nullPayload = "{NullPayload}";

    VpCodeMessages vpCodeMessages;

    @Autowired
    public CheckPayloadProcessorImpl(VpCodeMessages vpCodeMessages){
        this.vpCodeMessages = vpCodeMessages;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        //Kommer in som en Byte Array
        if (!(exchange.getIn().getBody() instanceof String )) {
            LOGGER.error("Wrong type encountered in transformer! Bailing out...");
            //throw new TransformerException(null);
        }

        String status_in = (String)exchange.getIn().getHeader("http.status");
        String addr = (String)exchange.getProperty(VPExchangeProperties.VAGVAL, "<UNKNOWN>");

        try {
            String cause = null;
            String strPayload = (String)exchange.getIn().getBody();
            if (strPayload == null || strPayload.length() == 0 || strPayload.equals(nullPayload)) {

                LOGGER.debug("Found return message with length 0, replace with SoapFault because CXF doesn't like the empty string");
                cause = get(VpSemanticErrorCodeEnum.VP009, addr + ". Empty message when server responded with status code: " + SoapFaultHelper.getStatusMessage(status_in, "NULL"));
            } else if(exchange.getProperty(Exchange.EXCEPTION_CAUGHT) != null) {

                Throwable throwable = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
                String message = throwable.getMessage();
                if(throwable instanceof ReadTimeoutException){
                    message = "Timeout when waiting on response from producer.";
                }

                LOGGER.debug("Exception Caught by Camel when contacting producer. Exception information: " + left(message, 200) + "...");
                cause = get(VpSemanticErrorCodeEnum.VP009 , addr + ". Exception Caught by Camel when contacting producer. Exception information: " + message);
            }

            if(cause != null) {
                setSoapFaultInResponse(exchange, cause, VpSemanticErrorCodeEnum.VP009.toString());
                //logException(message, new VpSemanticException(cause, VpSemanticErrorCodeEnum.VP009));
            }

        } catch (Exception e) {
            LOGGER.error("An error occured in CheckPayloadTransformer!.", e);
        }
    }

    private String left(String s, int len) {
        if(s == null)
            return null;

        int i =  s.length() > len ? len : s.length();
        return s.substring(0, i);
    }

    public String get(VpSemanticErrorCodeEnum errcode, String suffix) {
        String errorMsg = vpCodeMessages.getMessage(errcode);
        return errcode+" "+ String.format(errorMsg, suffix);
    }

    private static void setSoapFaultInResponse(Exchange exchange, String cause, String errorCode){
        String soapFault = SoapFaultHelper.generateSoap11FaultWithCause(cause);
        exchange.getOut().setBody(soapFault);
        //message.setExceptionPayload(null);
        exchange.getOut().setHeader("http.status", 500);
        exchange.setProperty(VPExchangeProperties.SESSION_ERROR, Boolean.TRUE);
        exchange.setProperty(VPExchangeProperties.SESSION_ERROR_CODE, errorCode);
        exchange.setProperty(VPExchangeProperties.SESSION_HTML_STATUS, SoapFaultHelper.getStatusMessage(nvl(exchange.getIn().getHeader("http.status")), null));
    }


}
