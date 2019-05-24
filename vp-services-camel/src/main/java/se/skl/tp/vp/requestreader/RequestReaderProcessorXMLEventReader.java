package se.skl.tp.vp.requestreader;

import java.util.Stack;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.component.netty4.http.NettyChannelBufferStreamCache;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.VPExchangeProperties;

@Service
@Log4j2
public class RequestReaderProcessorXMLEventReader implements RequestReaderProcessor {

  public static final String RIVTABP_21 = "rivtabp21";
  public static final String RIVTABP_20 = "rivtabp20";

  @Override
  public void process(Exchange exchange) throws Exception {
    NettyChannelBufferStreamCache body = (NettyChannelBufferStreamCache) exchange.getIn().getBody();
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    try {
      XMLEventReader eventReader = inputFactory.createXMLEventReader(body);
      PayloadInfo payloadInfo = parsePayloadForInfo(eventReader);
      exchange.setProperty(VPExchangeProperties.SERVICECONTRACT_NAMESPACE, payloadInfo.getServiceContractNamespace());
      exchange.setProperty(VPExchangeProperties.RECEIVER_ID, payloadInfo.getReceiverId());
      exchange.setProperty(VPExchangeProperties.RIV_VERSION, payloadInfo.getRivVersion());

    } catch (XMLStreamException e) {
      log.error("Failed read Soap", e);
    }
  }

  private PayloadInfo parsePayloadForInfo(XMLEventReader eventReader)
      throws XMLStreamException {

    boolean bodyFound = false;
    Stack<String> elementHierarchy = new Stack<>();
    PayloadInfo payloadInfo = new PayloadInfo();

    while (eventReader.hasNext()) {
      XMLEvent event = eventReader.nextEvent();

      if (event.isStartElement()) {
        elementHierarchy.add(event.asStartElement().getName().getLocalPart());
        if (bodyFound) {
          // Next element after Body should be the Service we looking for
          //    and the namespace is the servicecontract namespace.
          // Since we should have found everything we should break the loop
          String namespace = event.asStartElement().getName().getNamespaceURI();
          payloadInfo.setServiceContractNamespace(namespace);
          break;
        }
        if (elementHierarchy.peek().equalsIgnoreCase("Body")) {
          bodyFound = true;
        }
      } else if (event.isCharacters()) {
        parseForReceiver(elementHierarchy.peek(), payloadInfo, event);
      } else if (event.isEndElement()) {
        elementHierarchy.pop();
      }
    }
    return payloadInfo;
  }

  private void parseForReceiver(String localPart, PayloadInfo payloadInfo, XMLEvent event) {
    if (localPart.equalsIgnoreCase("LogicalAddress")) {
      payloadInfo.setReceiverId(event.asCharacters().getData());
      payloadInfo.setRivVersion(RIVTABP_21);
    } else if (localPart.equalsIgnoreCase("To")) {
      payloadInfo.setReceiverId(event.asCharacters().getData());
      payloadInfo.setRivVersion(RIVTABP_20);
    }
  }

  @Data
  public class PayloadInfo{
    String receiverId;
    String rivVersion;
    String serviceContractNamespace;
  }

}
