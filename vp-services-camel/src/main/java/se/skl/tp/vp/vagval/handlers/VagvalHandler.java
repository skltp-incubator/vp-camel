package se.skl.tp.vp.vagval.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.skl.tp.hsa.cache.HsaCache;
import se.skl.tp.vp.logging.LogTraceAppender;
import se.skl.tp.vp.logging.ThreadContextLogTrace;
import se.skl.tp.vp.vagval.util.DefaultRoutingUtil;
import se.skltp.takcache.RoutingInfo;
import se.skltp.takcache.TakCache;

import java.util.Collections;
import java.util.List;

import static se.skl.tp.hsa.cache.HsaCache.DEFAUL_ROOTNODE;

@Component
public class VagvalHandler {
    private static final Logger LOGGER = LogManager.getLogger(VagvalHandler.class);

    private HsaCache hsaCache;
    private TakCache takCache;
    private final String defaultRoutingAddressDelimiter;

    @Autowired
    public VagvalHandler(HsaCache hsaCache, TakCache takCache,  @Value("${vagvalrouter.default.routing.address.delimiter}") String delimiter) {
        this.hsaCache = hsaCache;
        this.takCache = takCache;
        defaultRoutingAddressDelimiter = delimiter;
    }

    public List<RoutingInfo> getRoutingInfo(String tjanstegranssnitt, String receiverAddress){

        List<RoutingInfo> routingInfos;
        LogTraceAppender logTrace = new LogTraceAppender();

        if( DefaultRoutingUtil.useOldStyleDefaultRouting(receiverAddress, defaultRoutingAddressDelimiter) ){
            routingInfos = getRoutingInfoUsingDefaultRouting(tjanstegranssnitt, receiverAddress, logTrace);
        } else{
            logTrace.append(receiverAddress);
            routingInfos = getRoutingInfoFromTakCache(tjanstegranssnitt, receiverAddress);
            if(routingInfos.isEmpty()) {
                routingInfos = getRoutingInfoByClimbingHsaTree(tjanstegranssnitt, receiverAddress, logTrace);
            }
        }

        logTrace.deleteCharIfLast(',');
        ThreadContextLogTrace.put(ThreadContextLogTrace.ROUTER_RESOLVE_VAGVAL_TRACE, logTrace.toString());

        return routingInfos;
    }

    private List<RoutingInfo> getRoutingInfoByClimbingHsaTree(String tjanstegranssnitt, String receiverAddress, LogTraceAppender logTrace) {
        logTrace.append(",(parent)");
        while (receiverAddress != DEFAUL_ROOTNODE) {
            receiverAddress = getHsaParent(receiverAddress);
            logTrace.append(receiverAddress, ',');
            List<RoutingInfo> routingInfoList = getRoutingInfoFromTakCache(tjanstegranssnitt, receiverAddress);
            if(!routingInfoList.isEmpty()){
               return routingInfoList;
            }
        }
        return  Collections.<RoutingInfo>emptyList();
    }

    private String getHsaParent(String receiverId) {
       return hsaCache.getParent(receiverId);
    }

    private List<RoutingInfo> getRoutingInfoUsingDefaultRouting(String tjanstegranssnitt, String receiverAddress, LogTraceAppender logTrace) {
        logTrace.append("(leaf)");

        List<String> receiverAddresses = DefaultRoutingUtil.extractReceiverAdresses(receiverAddress, defaultRoutingAddressDelimiter);

        for(String receiverAddressTmp : receiverAddresses){
            logTrace.append(receiverAddressTmp,',');
            List<RoutingInfo> routingInfoList = getRoutingInfoFromTakCache(tjanstegranssnitt, receiverAddressTmp);
            if(!routingInfoList.isEmpty()){
                return routingInfoList;
            }
        }

        return Collections.<RoutingInfo>emptyList();
    }

    private List<RoutingInfo> getRoutingInfoFromTakCache(String tjanstegranssnitt, String receiverAddress) {
        return takCache.getRoutingInfo(tjanstegranssnitt, receiverAddress);
    }

}
