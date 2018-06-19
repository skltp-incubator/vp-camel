package se.skl.tp.vp.httpheader;

import org.apache.camel.Message;
import org.apache.camel.component.netty4.NettyConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.ApplicationProperties;

import java.net.InetSocketAddress;

@Service
public class SenderIpExtractorFromHeader implements SenderIpExtractor {

    private String vagvalrouterSenderIpAdressHttpHeader;

    @Autowired
    public SenderIpExtractorFromHeader(Environment env) {
        vagvalrouterSenderIpAdressHttpHeader = env.getProperty(ApplicationProperties.VAGVALROUTER_SENDER_IP_ADRESS_HTTP_HEADER);
    }

    @Override
    public String extractSenderIpAdress(Message message) {
        String senderIpAdress  = message.getHeader(vagvalrouterSenderIpAdressHttpHeader, String.class);
        
        if(senderIpAdress == null){
            InetSocketAddress inetSocketAddress = message.getHeader(NettyConstants.NETTY_REMOTE_ADDRESS, InetSocketAddress.class);
            senderIpAdress = inetSocketAddress.getAddress().getHostAddress();
        }
        return senderIpAdress;
    }
}
