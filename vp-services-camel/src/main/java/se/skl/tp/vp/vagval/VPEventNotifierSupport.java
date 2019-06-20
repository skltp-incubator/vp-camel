package se.skl.tp.vp.vagval;

import static se.skl.tp.vp.VPRouter.VP_HTTPS_ROUTE;
import static se.skl.tp.vp.VPRouter.VP_HTTP_ROUTE;
import static se.skl.tp.vp.constants.HttpHeaders.X_SKLTP_PRODUCER_RESPONSETIME;
import org.apache.camel.Exchange;
import org.apache.camel.component.netty4.http.NettyHttpEndpoint;
import org.apache.camel.management.event.ExchangeSentEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.springframework.stereotype.Component;

import java.util.EventObject;

@Component
public class VPEventNotifierSupport extends EventNotifierSupport {

  @Override
  public boolean isEnabled(EventObject event) {
    return true;
  }

  @Override
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
      if (isSentToProducer(sent, exchange)) {
        long timeTaken = sent.getTimeTaken();
        if (log.isInfoEnabled()) {
          log.info(
              exchange
                  + " SEND >>> Took "
                  + timeTaken
                  + " millis to send to external system : "
                  + sent.getEndpoint().getEndpointKey());
        }
        exchange.getOut().setHeader(X_SKLTP_PRODUCER_RESPONSETIME, timeTaken);
      }
    }
  }

  /**
   * This method rely on the NettyHttpEndpoint only being used once (when invoking producer) on on
   * each HTTP/S route
   *
   * @param sent
   * @param exchange
   * @return true if end point is producer
   */
  private boolean isSentToProducer(ExchangeSentEvent sent, Exchange exchange) {
    return sent.getEndpoint() instanceof NettyHttpEndpoint
        && (exchange.getFromRouteId().equals(VP_HTTP_ROUTE)
            || exchange.getFromRouteId().equals(VP_HTTPS_ROUTE));
  }
}
