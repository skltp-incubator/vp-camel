package se.skl.tp.vp.requestreader;

import org.apache.camel.Exchange;
import org.apache.camel.component.netty4.http.NettyChannelBufferStreamCache;
import org.springframework.stereotype.Service;

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
public class RequestReaderProcessorXMLEventReader implements RequestReaderProcessor {
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
                        exchange.setProperty("LogicalAddress" , event.asCharacters().getData());
                    }
                    if(elementHierarchy.peek().equalsIgnoreCase("To")) {
                        exchange.setProperty("LogicalAddress" , event.asCharacters().getData());
                    }
                } else if(event.isEndElement()) {
                    elementHierarchy.pop();
                } else if(event.isEndDocument()) {

                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void getTjanstekontrakt(Exchange exchange, StartElement event) {
        StartElement startElement = event;
        Iterator namespaces = startElement.getNamespaces();
        List ns = new ArrayList();
        namespaces.forEachRemaining(o -> ns.add(o));
        for (Object obj: ns) {
            if(((Namespace)obj).getNamespaceURI().toLowerCase().contains("responder")) {
                exchange.setProperty("tjanstekontrakt" , ((Namespace)obj).getNamespaceURI());
            }
        }
    }
}
