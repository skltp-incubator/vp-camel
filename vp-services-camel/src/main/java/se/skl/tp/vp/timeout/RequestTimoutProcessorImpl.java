package se.skl.tp.vp.timeout;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.VPExchangeProperties;

import static org.apache.camel.component.netty4.NettyConstants.NETTY_REQUEST_TIMEOUT;

@Service
public class RequestTimoutProcessorImpl implements RequestTimoutProcessor {

    TimeoutConfiguration timeoutConfiguration;

    @Autowired
    public RequestTimoutProcessorImpl(TimeoutConfiguration timeoutConfiguration) {
        this.timeoutConfiguration = timeoutConfiguration;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        TimeoutConfig timoutConfig = timeoutConfiguration.getOnTjanstekontrakt(exchange.getProperty(VPExchangeProperties.SERVICECONTRACT_NAMESPACE, String.class));
        if(timoutConfig != null) {
            exchange.getIn().setHeader(NETTY_REQUEST_TIMEOUT, timoutConfig.getProducertimeout());
        } else {
            TimeoutConfig defaultConfig = timeoutConfiguration.getOnTjanstekontrakt("default");
            exchange.getIn().setHeader(NETTY_REQUEST_TIMEOUT, defaultConfig.getProducertimeout());
        }

    }
}
