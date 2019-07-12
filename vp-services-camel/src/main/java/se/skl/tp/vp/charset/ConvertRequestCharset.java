package se.skl.tp.vp.charset;

import static org.apache.commons.lang.CharEncoding.UTF_16;
import static org.apache.commons.lang.CharEncoding.UTF_8;

import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.VPExchangeProperties;

@Service
@Log4j2
public class ConvertRequestCharset implements Processor {

  private static final String DEFAULT_ENCODING = UTF_8;

  @Override
  public void process(Exchange exchange) throws Exception {
    String xmlRequestEncoding = exchange.getProperty(VPExchangeProperties.XML_REQUEST_ENCODING, String.class);
    if (!xmlRequestEncoding.toUpperCase().startsWith(DEFAULT_ENCODING)) {
      convertBodyToUTF8String(exchange);
      exchange.setProperty(Exchange.CHARSET_NAME, DEFAULT_ENCODING);
      exchange.setProperty(VPExchangeProperties.ORIGINAL_REQUEST_ENCODING, xmlRequestEncoding);
    }
  }

  private void convertBodyToUTF8String(Exchange exchange) throws TransformerException {
    XMLStreamReader body = exchange.getIn().getBody(XMLStreamReader.class);
    StAXSource source = new StAXSource(body);

    StringWriter stringWriter = new StringWriter();
    StreamResult streamResult = new StreamResult(stringWriter);

    TransformerFactory factory = TransformerFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.newTransformer().transform(source, streamResult);

    exchange.getIn().setBody(stringWriter.getBuffer().toString());
  }

  @SuppressWarnings("unused")
  private String getOriginalEncoding(String xmlEncoding) {
    String encoding = xmlEncoding.toUpperCase();
    if (encoding.startsWith(UTF_8)) {
      return UTF_8;
    } else if (encoding.startsWith(UTF_16)) {
      return UTF_16;
    } else {
      return encoding;
    }
  }


}
