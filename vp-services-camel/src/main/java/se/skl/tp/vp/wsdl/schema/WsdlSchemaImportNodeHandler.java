package se.skl.tp.vp.wsdl.schema;

import java.net.URL;
import org.dom4j.Node;
import se.skl.tp.vp.wsdl.schema.ForwardedHttpHeadersBaseUrlFactory.BaseUrlModel;

public class WsdlSchemaImportNodeHandler extends WsdlNodeHandler{

  public WsdlSchemaImportNodeHandler(URL urlOrig,
      BaseUrlModel baseUrl) {
    super(urlOrig, baseUrl);
  }

  @Override
  String getQuery(Node node) {
    return "xsd="+node.getText();
  }
}
