package se.skl.tp.vp.certificate;

import org.apache.camel.Message;
import org.apache.camel.component.netty4.NettyConstants;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

@Service
public class SenderIpExtractorFromHeader implements SenderIpExtractor {

    @Override
    public String extractSenderIpAdress(Message message) {
        InetSocketAddress inetSocketAddress = message.getHeader(NettyConstants.NETTY_REMOTE_ADDRESS, InetSocketAddress.class);
        String senderIpAdress = inetSocketAddress.getAddress().getHostAddress();

        if(senderIpAdress == null){
            //senderIpAdress = VPUtil.extractIpAddress(message);
        }
        return senderIpAdress;
    }
}
