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

    private final String VAGVALROUTER_SENDER_IP_ADRESS_HTTP_HEADER;

    @Autowired
    public SenderIpExtractorFromHeader(@Value("${" + PropertyConstants.VAGVALROUTER_SENDER_IP_ADRESS_HTTP_HEADER + "}") String vagvalrouter_sender_ip_adress_http_header) {
        VAGVALROUTER_SENDER_IP_ADRESS_HTTP_HEADER = vagvalrouter_sender_ip_adress_http_header;
    }

    @Override
    public String extractSenderIpAdress(Message message) {
        String senderIpAdress  = message.getHeader(VAGVALROUTER_SENDER_IP_ADRESS_HTTP_HEADER, String.class);
        
        if(senderIpAdress == null){
            InetSocketAddress inetSocketAddress = message.getHeader(NettyConstants.NETTY_REMOTE_ADDRESS, InetSocketAddress.class);
            senderIpAdress = inetSocketAddress.getAddress().getHostAddress();
        }
        return senderIpAdress;
    }
}
