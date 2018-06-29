package se.skl.tp.vp.vagval;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.exceptions.VpSemanticException;
import se.skl.tp.vp.vagval.handlers.VagvalHandler;
import se.skltp.takcache.RoutingInfo;

import java.util.List;

public class VagvalProcessorImpl  implements VagvalProcessor {

    @Autowired
    VagvalHandler vagvalHandler;

    @Override
    public void process(Exchange exchange) throws Exception {
        String receiverId = (String) exchange.getProperty(VPExchangeProperties.RECEIVER_ID);
        String servicecontractNamespace = (String) exchange.getProperty(VPExchangeProperties.SERVICECONTRACT_NAMESPACE);
        validateRequest(servicecontractNamespace, receiverId);

        RoutingInfo routingInfo = getRoutingAddress(servicecontractNamespace, receiverId);

        exchange.setProperty(VPExchangeProperties.VAGVAL, routingInfo.getAddress() );
        exchange.setProperty(VPExchangeProperties.RIV_VERSION_OUT, routingInfo.getRivProfile() );

    }

    public RoutingInfo getRoutingAddress(String tjanstegranssnitt, String receiverAddress){

        List<RoutingInfo> routingInfos = vagvalHandler.getRoutingInfo(tjanstegranssnitt, receiverAddress);

        if(routingInfos.isEmpty()){
            raiseError(VpSemanticErrorCodeEnum.VP004, getRequestSummaryString(tjanstegranssnitt, receiverAddress));
        }

        if(routingInfos.size()>1){
            raiseError(VpSemanticErrorCodeEnum.VP006, getRequestSummaryString(tjanstegranssnitt, receiverAddress));
        }

        RoutingInfo routingInfo = routingInfos.get(0);

        if (routingInfo.getAddress() == null || routingInfo.getAddress().trim().length() == 0) {
            raiseError(VpSemanticErrorCodeEnum.VP010, getRequestSummaryString(tjanstegranssnitt, receiverAddress));
        }

        return routingInfo;
    }
    private void validateRequest(String servicecontractNamespace, String receiverId) {
        //TODO Kontrollera servicecontractNamespace ?

        // No receiver ID (to_address) found in message
        raiseError(receiverId == null, VpSemanticErrorCodeEnum.VP003);
    }

    private void raiseError(boolean test, VpSemanticErrorCodeEnum codeenum) {
        if(test) {
            raiseError(codeenum, null);
        }
    }

    private void raiseError(VpSemanticErrorCodeEnum codeEnum, String suffix) {
        // TODO fix errmsg
        String errmsg="";
//        String errmsg = MessageProperties.getInstance().get(codeEnum, suffix);
//        LOGGER.error(errmsg);
        throw new VpSemanticException(errmsg, codeEnum);
    }


    private String getRequestSummaryString(String tjanstegranssnitt, String receiverAddress) {
        return String.format( "serviceNamespace: %s, receiverId: %s",tjanstegranssnitt, receiverAddress);
    }
}
