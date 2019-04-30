package se.skl.tp.vp.vagval;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.skl.tp.vagval.logging.ThreadContextLogTrace;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.errorhandling.ExceptionUtil;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.service.TakCacheService;


@Service
public class BehorighetProcessor implements Processor {

    @Autowired
    TakCacheService takService;

    @Autowired
    ExceptionUtil exceptionUtil;

    @Override
    public void process(Exchange exchange) throws Exception {
        if (!takService.isInitalized()) {
            exceptionUtil.raiseError(VpSemanticErrorCodeEnum.VP008);
        }

        String receiverId = (String) exchange.getProperty(VPExchangeProperties.RECEIVER_ID);
        String senderId = (String) exchange.getProperty(VPExchangeProperties.SENDER_ID);
        String servicecontractNamespace = (String) exchange.getProperty(VPExchangeProperties.SERVICECONTRACT_NAMESPACE);

        validateRequest(senderId, receiverId, servicecontractNamespace);

        boolean isAuthorized = takService.isAuthorized(senderId, servicecontractNamespace, receiverId);
        exchange.setProperty(VPExchangeProperties.ANROPSBEHORIGHET_TRACE, ThreadContextLogTrace.get(ThreadContextLogTrace.ROUTER_RESOLVE_ANROPSBEHORIGHET_TRACE) );
        if( !isAuthorized ){
            exceptionUtil.raiseError( VpSemanticErrorCodeEnum.VP007, "Not authorized call, " + getRequestSummaryString(senderId, servicecontractNamespace, receiverId));
        }
    }

    private void validateRequest(String senderId, String receiverId, String servicecontractNamespace) {
        //TODO Kontrollera servicecontractNamespace ?

        // No sender ID (from_address) found in certificate
        exceptionUtil.raiseError(senderId == null, VpSemanticErrorCodeEnum.VP002, getRequestSummaryString(senderId, servicecontractNamespace, receiverId));

        // No receiver ID (to_address) found in message
        exceptionUtil.raiseError(receiverId == null, VpSemanticErrorCodeEnum.VP003, getRequestSummaryString(senderId, servicecontractNamespace, receiverId));
    }

    private String getRequestSummaryString(String senderId, String serviceNamespace, String logicalAddress) {
        return String.format( "SenderId: %s serviceNamespace: %s, logicalAddress: %s",senderId, serviceNamespace, logicalAddress);
    }

}
