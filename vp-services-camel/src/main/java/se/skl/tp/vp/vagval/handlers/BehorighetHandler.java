package se.skl.tp.vp.vagval.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.skl.tp.hsa.cache.HsaCache;
import se.skl.tp.vp.logging.LogTraceAppender;
import se.skl.tp.vp.logging.ThreadContextLogTrace;
import se.skl.tp.vp.vagval.util.DefaultRoutingUtil;
import se.skltp.takcache.TakCache;

import java.util.List;

import static se.skl.tp.hsa.cache.HsaCache.DEFAUL_ROOTNODE;

@Component
public class BehorighetHandler {
    private HsaCache hsaCache;
    private TakCache takCache;
    private final String defaultRoutingAddressDelimiter;


    @Autowired
    public BehorighetHandler(HsaCache hsaCache, TakCache takCache, @Value("${vagvalrouter.default.routing.address.delimiter}") String delimiter) {
        this.hsaCache = hsaCache;
        this.takCache = takCache;
        System.out.println("BehorighetHandler Takcache:"+takCache);

        defaultRoutingAddressDelimiter = delimiter;
    }

    public boolean isAuthorized(String senderId, String servicecontractNamespace, String receiverId) {
        LogTraceAppender logTrace = new LogTraceAppender();

        boolean isAuthorized = isAuthorized(senderId, servicecontractNamespace, receiverId, logTrace);

        logTrace.deleteCharIfLast(',');
        ThreadContextLogTrace.put(ThreadContextLogTrace.ROUTER_RESOLVE_ANROPSBEHORIGHET_TRACE, logTrace.toString());
        return isAuthorized;
    }

    public boolean isAuthorized(String senderId, String servicecontractNamespace, String receiverId, LogTraceAppender logTrace) {

        if(DefaultRoutingUtil.useOldStyleDefaultRouting(receiverId, defaultRoutingAddressDelimiter) ){
            return isAuthorizedUsingDefaultRouting(senderId, servicecontractNamespace, receiverId, logTrace);
        }

        logTrace.append(receiverId);
        if( takCache.isAuthorized(senderId,servicecontractNamespace, receiverId ) ){
            return true;
        }

        return isAuthorizedByClimbingHsaTree(senderId, servicecontractNamespace, receiverId, logTrace);
    }

    private boolean isAuthorizedUsingDefaultRouting(String senderId, String servicecontractNamespace, String receiverId, LogTraceAppender logTrace) {
        logTrace.append("(leaf)");
        List<String> receiverAddresses = DefaultRoutingUtil.extractReceiverAdresses(receiverId, defaultRoutingAddressDelimiter);
        for(String receiverAddressTmp : receiverAddresses){
            logTrace.append(receiverAddressTmp,',');
            if(takCache.isAuthorized(senderId, servicecontractNamespace, receiverAddressTmp)){
                return true;
            }
        }
        return false;
    }

    private boolean isAuthorizedByClimbingHsaTree(String senderId, String servicecontractNamespace, String receiverId, LogTraceAppender logTrace) {
        logTrace.append(",(parent)");
        while (receiverId != DEFAUL_ROOTNODE) {
            receiverId = getHsaParent(receiverId);
            logTrace.append(receiverId,',');
            if(takCache.isAuthorized(senderId, servicecontractNamespace, receiverId)){
                return true;
            }
        }
        return false;
    }

    private String getHsaParent(String receiverId) {
        return hsaCache.getParent(receiverId);
    }

}
