package se.skl.tp.vp.httpheader;


import java.util.UUID;
import org.apache.camel.Exchange;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;

@Service
public class CorrelationIdProcessorImpl implements CorrelationIdProcessor {

  public void process(Exchange exchange) {
    String correlationId = null;

    if (exchange.getIn().getHeaders().containsKey(HttpHeaders.X_SKLTP_CORRELATION_ID)) {
      correlationId = exchange.getIn().getHeader(HttpHeaders.X_SKLTP_CORRELATION_ID, String.class);
    }
    if (StringUtils.isEmpty(correlationId)) {
      correlationId = UUID.randomUUID().toString();
    }
    exchange.setProperty(VPExchangeProperties.SKLTP_CORRELATION_ID, correlationId);
  }
}
