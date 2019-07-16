package se.skl.tp.vp.wsdl.schema;

import java.net.URISyntaxException;
import java.net.URL;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.skl.tp.vp.config.ProxyHttpForwardedHeaders;

public class ForwardedHttpHeadersBaseUrlFactory {

  private static Logger log = LoggerFactory.getLogger(ForwardedHttpHeadersBaseUrlFactory.class);

  public BaseUrlModel extractForwardedHttpHeadersForBaseUrl(
      Message message, ProxyHttpForwardedHeaders proxyHttpForwardedHeaders, URL pHttpUri)
      throws URISyntaxException {
    BaseUrlModel baseUrl = new BaseUrlModel();
    baseUrl.scheme = (String) message.getHeader(proxyHttpForwardedHeaders.getProto());
    if (baseUrl.scheme != null && !baseUrl.scheme.isEmpty()) {
      baseUrl.host = (String) message.getHeader(proxyHttpForwardedHeaders.getHost());
      baseUrl.port = (String) message.getHeader(proxyHttpForwardedHeaders.getPort());
      log.debug("Found forwarded HTTP headers for URL parts: {}", baseUrl);
    }else{
      baseUrl.scheme  = pHttpUri.toURI().getScheme();
      baseUrl.port = ""+pHttpUri.getPort();
      baseUrl.host = pHttpUri.getHost();
    }
    return baseUrl;
  }

  /** Represent the base part of an URL. */
  public class BaseUrlModel {
    String scheme;
    String host;
    String port;

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.getClass().getSimpleName());
      sb.append(": scheme: ");
      sb.append(scheme);
      sb.append(", host: ");
      sb.append(host);
      sb.append(", port: ");
      sb.append(port);
      return sb.toString();
    }


  }
}
