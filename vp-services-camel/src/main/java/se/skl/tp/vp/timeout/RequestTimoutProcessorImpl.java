package se.skl.tp.vp.timeout;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.ApplicationProperties;
import se.skl.tp.vp.constants.VPExchangeProperties;

import static org.apache.camel.component.netty4.NettyConstants.NETTY_REQUEST_TIMEOUT;

@Service
public class RequestTimoutProcessorImpl implements RequestTimoutProcessor {

    TimeoutConfiguration timeoutConfiguration;
    private final String DEFAULT_TJANSTEKONTRAKT;

    @Autowired
    public RequestTimoutProcessorImpl(TimeoutConfiguration timeoutConfiguration, Environment env) {
        this.timeoutConfiguration = timeoutConfiguration;
        DEFAULT_TJANSTEKONTRAKT = env.getProperty(ApplicationProperties.TIMEOUT_JSON_FILE_DEFAULT_TJANSTEKONTRAKT_NAME);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        TimeoutConfig timoutConfig = timeoutConfiguration.getOnTjanstekontrakt(exchange.getProperty(VPExchangeProperties.SERVICECONTRACT_NAMESPACE, String.class));
        if(timoutConfig != null) {
            exchange.getIn().setHeader(NETTY_REQUEST_TIMEOUT, timoutConfig.getProducertimeout());
        } else {
            TimeoutConfig defaultConfig = timeoutConfiguration.getOnTjanstekontrakt(DEFAULT_TJANSTEKONTRAKT);
            exchange.getIn().setHeader(NETTY_REQUEST_TIMEOUT, defaultConfig.getProducertimeout());
        }

    }
}
