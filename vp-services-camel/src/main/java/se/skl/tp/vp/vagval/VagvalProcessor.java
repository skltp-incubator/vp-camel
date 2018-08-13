package se.skl.tp.vp.vagval;

import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.errorhandling.ExceptionUtil;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.vagval.handlers.VagvalHandler;
import se.skltp.takcache.RoutingInfo;

@Service
public class VagvalProcessor implements Processor {

    @Autowired
    VagvalHandler vagvalHandler;

    @Autowired
    ExceptionUtil exceptionUtil;

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
            exceptionUtil.raiseError(VpSemanticErrorCodeEnum.VP004, getRequestSummaryString(tjanstegranssnitt, receiverAddress));
        }

        if(routingInfos.size()>1){
            exceptionUtil.raiseError(VpSemanticErrorCodeEnum.VP006, getRequestSummaryString(tjanstegranssnitt, receiverAddress));
        }

        RoutingInfo routingInfo = routingInfos.get(0);

        if (routingInfo.getAddress() == null || routingInfo.getAddress().trim().length() == 0) {
            exceptionUtil.raiseError(VpSemanticErrorCodeEnum.VP010, getRequestSummaryString(tjanstegranssnitt, receiverAddress));
        }

        return routingInfo;
    }
    private void validateRequest(String servicecontractNamespace, String receiverId) {
        //TODO Kontrollera servicecontractNamespace ?

        // No receiver ID (to_address) found in message
        exceptionUtil.raiseError(receiverId == null, VpSemanticErrorCodeEnum.VP003);
    }


    private String getRequestSummaryString(String tjanstegranssnitt, String receiverAddress) {
        return String.format( "serviceNamespace: %s, receiverId: %s",tjanstegranssnitt, receiverAddress);
    }
}
