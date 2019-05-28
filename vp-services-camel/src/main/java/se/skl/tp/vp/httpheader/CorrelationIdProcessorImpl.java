package se.skl.tp.vp.httpheader;


import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;

import java.util.UUID;

@Service
@Slf4j
public class CorrelationIdProcessorImpl implements CorrelationIdProcessor {

  public void process(Exchange exchange) throws Exception {
    String correlationId = null;

    if (exchange.getIn().getHeaders().containsKey(HttpHeaders.X_SKLTP_CORRELATION_ID)) {
      correlationId = "" + exchange.getIn().getHeaders().get(HttpHeaders.X_SKLTP_CORRELATION_ID);
    }
    if (StringUtils.isEmpty(correlationId)) {
      correlationId = UUID.randomUUID().toString();
    }
    exchange.setProperty(VPExchangeProperties.SKLTP_CORRELATION_ID, correlationId);
  }
}
