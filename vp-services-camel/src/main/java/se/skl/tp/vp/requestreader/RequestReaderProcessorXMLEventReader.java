package se.skl.tp.vp.requestreader;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.component.netty4.http.NettyChannelBufferStreamCache;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.VPExchangeProperties;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

@Service
@Slf4j
public class RequestReaderProcessorXMLEventReader implements RequestReaderProcessor {

    public static final String RIVTABP_21 = "rivtabp21";
    public static final String RIVTABP_20 = "rivtabp20";

    @Override
    public void process(Exchange exchange) throws Exception {
        NettyChannelBufferStreamCache body = (NettyChannelBufferStreamCache)exchange.getIn().getBody();
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = null;
        Stack<String> elementHierarchy = new Stack<>();
        try {
            eventReader = inputFactory.createXMLEventReader(body);

            while(eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if(event.isStartDocument()) {

                } else if(event.isStartElement()) {
                    elementHierarchy.add(event.asStartElement().getName().getLocalPart());
                    if(elementHierarchy.peek().equalsIgnoreCase("Envelope")) {
                        getTjanstekontrakt(exchange, (StartElement) event);
                    } else if(elementHierarchy.peek().equalsIgnoreCase("Body")) {
                        getTjanstekontrakt(exchange, (StartElement) event);
                        return;
                    }
                } else if(event.isCharacters()) {
                    if(elementHierarchy.peek().equalsIgnoreCase("LogicalAddress")) {
                        exchange.setProperty(VPExchangeProperties.RECEIVER_ID, event.asCharacters().getData());
                        exchange.setProperty(VPExchangeProperties.RIV_VERSION, RIVTABP_21);
                    }
                    if(elementHierarchy.peek().equalsIgnoreCase("To")) {
                        exchange.setProperty(VPExchangeProperties.RECEIVER_ID, event.asCharacters().getData());
                        exchange.setProperty(VPExchangeProperties.RIV_VERSION, RIVTABP_20);
                    }
                } else if(event.isEndElement()) {
                    elementHierarchy.pop();
                } else if(event.isEndDocument()) {

                }
            }
        } catch (XMLStreamException e) {
            log.error("Failed read Soap", e);
        }
    }

    private void getTjanstekontrakt(Exchange exchange, StartElement event) {
        StartElement startElement = event;
        Iterator namespaces = startElement.getNamespaces();
        List ns = new ArrayList();
        namespaces.forEachRemaining(o -> ns.add(o));
        for (Object obj: ns) {
            if(((Namespace)obj).getNamespaceURI().toLowerCase().contains("responder")) {
                exchange.setProperty(VPExchangeProperties.SERVICECONTRACT_NAMESPACE , ((Namespace)obj).getNamespaceURI());
            }
        }
    }
}
