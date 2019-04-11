package se.skl.tp.vp.vagval;

import org.apache.camel.Exchange;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeSentEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.springframework.stereotype.Component;
import se.skl.tp.vp.VPRouter;
import se.skl.tp.vp.constants.HttpHeaders;

import java.util.Date;
import java.util.EventObject;

@Component
public class VPEventNotifierSupport extends EventNotifierSupport {
    public static final String PRODUCER_RESPONSE_TIME = "producerResponseTime";
    public static final String FLOW_RESPONSE_TIME = "producerResponseTime";

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

            Long producerResponseTime = exchange.getProperty(PRODUCER_RESPONSE_TIME, Long.class);
            if (producerResponseTime == null) {
                exchange.setProperty(PRODUCER_RESPONSE_TIME, sent.getTimeTaken());
            }
            log.debug(exchange + " SEND >>> Took " + sent.getTimeTaken() + " millis to send to external system : " + sent.getEndpoint().getEndpointKey());
        }
    }
}
