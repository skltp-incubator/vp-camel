package se.skl.tp.vp.vagval;

import org.apache.camel.Exchange;
import org.apache.camel.management.event.ExchangeSentEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.springframework.stereotype.Component;

import java.util.EventObject;

@Component
public class VPEventNotifierSupport extends EventNotifierSupport {
    public static final String LAST_ENDPOINT_RESPONSE_TIME = "lastResponseTime";

    @Override
    public boolean isEnabled(EventObject event) {
        return true;
    }

    protected void doStart() throws Exception {
        // filter out unwanted events
        setIgnoreExchangeSentEvents(false);

        setIgnoreExchangeCompletedEvent(true);
        setIgnoreExchangeFailedEvents(true);
        setIgnoreCamelContextEvents(true);
        setIgnoreServiceEvents(true);
        setIgnoreRouteEvents(true);
        setIgnoreExchangeCreatedEvent(true);
        setIgnoreExchangeRedeliveryEvents(true);
    }

    @Override
    public void notify(EventObject event) {
        if (event instanceof ExchangeSentEvent) {
            ExchangeSentEvent sent = (ExchangeSentEvent) event;
            Exchange exchange = sent.getExchange();
            exchange.setProperty(LAST_ENDPOINT_RESPONSE_TIME, sent.getTimeTaken());
            log.info(exchange + " SEND >>> Took " + sent.getTimeTaken() + " millis to send to external system : " + sent.getEndpoint().getEndpointKey());
        }
    }
}
