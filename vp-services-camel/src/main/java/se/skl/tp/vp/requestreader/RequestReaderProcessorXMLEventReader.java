package se.skl.tp.vp.requestreader;

import static org.apache.commons.lang.CharEncoding.UTF_8;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpTechnicalException;
import se.skl.tp.vp.requestreader.PayloadInfoParser.PayloadInfo;

@Service
@Log4j2
public class RequestReaderProcessorXMLEventReader implements RequestReaderProcessor {

  public static final String RIVTABP_21 = "rivtabp21";
  public static final String RIVTABP_20 = "rivtabp20";

  @Override
  public void process(Exchange exchange) throws Exception {
    try {
      XMLStreamReader reader = toStreamReader(exchange);
      PayloadInfo payloadInfo = PayloadInfoParser.extractInfoFromPayload(reader);

      exchange.setProperty(VPExchangeProperties.SERVICECONTRACT_NAMESPACE, payloadInfo.getServiceContractNamespace());
      exchange.setProperty(VPExchangeProperties.RECEIVER_ID, payloadInfo.getReceiverId());
      exchange.setProperty(VPExchangeProperties.RIV_VERSION, payloadInfo.getRivVersion());
      exchange.setProperty(VPExchangeProperties.XML_REQUEST_ENCODING, payloadInfo.getEncoding());

    } catch (final XMLStreamException e) {
      throw new VpTechnicalException(e);
    }
  }

  private XMLStreamReader toStreamReader(Exchange exchange) throws XMLStreamException {
    try {
      return exchange.getIn().getBody(XMLStreamReader.class);
    } catch (Exception e) {
      log.warn("Failed convert payload to XMLStreamReader. Trying with default encoding UTF-8...");
      exchange.setProperty(Exchange.CHARSET_NAME, UTF_8);
      return exchange.getIn().getBody(XMLStreamReader.class);
    }
  }

}
