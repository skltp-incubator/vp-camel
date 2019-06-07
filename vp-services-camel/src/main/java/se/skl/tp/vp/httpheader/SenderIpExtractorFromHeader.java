package se.skl.tp.vp.httpheader;

import org.apache.camel.Message;
import org.apache.camel.component.netty4.NettyConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.PropertyConstants;

import java.net.InetSocketAddress;

@Service
public class SenderIpExtractorFromHeader implements SenderIpExtractor {

    private final String forwardProxyRemoteIpHeaderName;

    @Autowired
    public SenderIpExtractorFromHeader(@Value("${" + PropertyConstants.VAGVALROUTER_SENDER_IP_ADRESS_HTTP_HEADER + "}") String forwardProxyRemoteIpHeaderName) {
        this.forwardProxyRemoteIpHeaderName = forwardProxyRemoteIpHeaderName;
    }

    @Override
    public String extractSenderIpAdress(Message message) {
        String senderIpAdress  = message.getHeader(forwardProxyRemoteIpHeaderName, String.class);
        
        if(senderIpAdress == null){
            InetSocketAddress inetSocketAddress = message.getHeader(NettyConstants.NETTY_REMOTE_ADDRESS, InetSocketAddress.class);
            senderIpAdress = inetSocketAddress.getAddress().getHostAddress();
        }
        return senderIpAdress;
    }
}
