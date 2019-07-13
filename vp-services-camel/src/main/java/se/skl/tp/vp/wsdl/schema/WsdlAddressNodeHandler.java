package se.skl.tp.vp.wsdl.schema;

import java.net.URL;
import org.dom4j.Node;
import se.skl.tp.vp.wsdl.schema.ForwardedHttpHeadersBaseUrlFactory.BaseUrlModel;

public class WsdlAddressNodeHandler extends WsdlNodeHandler{

  public WsdlAddressNodeHandler(URL pOriginalUri, BaseUrlModel baseUrlModel) {
    super(pOriginalUri, baseUrlModel);
  }

  @Override
  String getQuery(Node node) {
    return null;
  }
}
