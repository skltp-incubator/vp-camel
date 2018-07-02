package se.skl.tp.vp.vagval;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.vagval.handlers.BehorighetHandler;

import static se.skl.tp.vp.vagval.util.ErrorUtil.raiseError;

@Service
public class BehorighetProcessor implements Processor {

    @Autowired
    BehorighetHandler behorighetHandler;

    @Override
    public void process(Exchange exchange) throws Exception {
        String receiverId = (String) exchange.getProperty(VPExchangeProperties.RECEIVER_ID);
        String senderId = (String) exchange.getProperty(VPExchangeProperties.SENDER_ID);
        String servicecontractNamespace = (String) exchange.getProperty(VPExchangeProperties.SERVICECONTRACT_NAMESPACE);

        validateRequest(senderId, receiverId);

        if( !behorighetHandler.isAuthorized(senderId, servicecontractNamespace, receiverId)){
            raiseError( VpSemanticErrorCodeEnum.VP007, "Not authorized call, " + getRequestSummaryString(senderId, servicecontractNamespace, receiverId));
        }
    }

    private void validateRequest(String senderId, String receiverId) {
        //TODO Kontrollera servicecontractNamespace ?

//        // No RIV version configured
//        raiseError(request.rivVersion == null, VpSemanticErrorCodeEnum.VP001);

        // No sender ID (from_address) found in certificate
        raiseError(senderId == null, VpSemanticErrorCodeEnum.VP002);

        // No receiver ID (to_address) found in message
        raiseError(receiverId == null, VpSemanticErrorCodeEnum.VP003);
    }

    private String getRequestSummaryString(String senderId, String serviceNamespace, String logicalAddress) {
        return String.format( "SenderId: %s serviceNamespace: %s, logicalAddress: %s",senderId, serviceNamespace, logicalAddress);
    }

}
