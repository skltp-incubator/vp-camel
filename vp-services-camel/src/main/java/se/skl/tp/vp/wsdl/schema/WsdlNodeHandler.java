package se.skl.tp.vp.wsdl.schema;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.dom4j.Node;
import xmlutil.NodeHandler;
import se.skl.tp.vp.wsdl.schema.ForwardedHttpHeadersBaseUrlFactory.BaseUrlModel;

public abstract class WsdlNodeHandler implements NodeHandler {
  private URL urlOrig;
  private BaseUrlModel baseUrl;

  public WsdlNodeHandler(URL urlOrig, BaseUrlModel baseUrl) {
    this.urlOrig = urlOrig;
    this.baseUrl = baseUrl;
  }

  /**
   * @param urlOrig the original full URL
   * @param baseUrl the replacement baseUrl
   * @return the full URL with baseUrl replaced
   */
  protected String replaceBaseUrlParts(URL urlOrig, BaseUrlModel baseUrl, String pQuery) {
    try {

      // use URI to correctly decode/encode URLs, ref:
      // http://docs.oracle.com/javase/7/docs/api/java/net/URL.html
      URI uriOrig = urlOrig.toURI();
      URI uriNew =
          new URI(
              baseUrl.scheme,
              null,
              baseUrl.host,
              Integer.valueOf(baseUrl.port),
              urlOrig.getPath(),
              pQuery,
              uriOrig.getFragment());
      return uriNew.toURL().toExternalForm();
    } catch (URISyntaxException e) {
      throw new WsdlNodeHandlerException("Error transforming url", e);
    } catch (MalformedURLException e) {
      throw new WsdlNodeHandlerException("Error transforming url", e);
    }
  }

  @Override
  public void handle(Node node) {
    node.setText(replaceBaseUrlParts(urlOrig, baseUrl, getQuery(node)));
  }

  abstract String getQuery(Node node);

  public class WsdlNodeHandlerException extends RuntimeException {
    WsdlNodeHandlerException(String msg, Exception e) {
      super(msg, e);
    }
  }
}
