package extract;

import static wsdl.PathHelper.subtractDirectoryFromPath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.XPath;
import wsdl.PathHelper;
import xmlutil.XmlHelper;

class UnpackedWdslFinder {

  public enum WsdlFoundAndDataExtracted {
    OK,WRONG_NUMBER_OF_WSDL,WRONG_NUMBER_OF_CONTRACTS
  }
  private WsdlFoundAndDataExtracted lasExcecutionResult;

  WsdlFoundAndDataExtracted lastExcecutionResult(){
    return lasExcecutionResult;
  }

  private String serviceContract;

  String serviceContract() {
    return serviceContract;
  }

  String wsdlPath() {
    return wsdlPath;
  }

  private String wsdlPath;

  private XPath getConTract =
      XmlHelper.createXPath(
          "wsdl:definitions/wsdl:types/xs:schema/xs:import/@namespace[contains(.,'Responder')]",
          "wsdl=http://schemas.xmlsoap.org/wsdl/",
          "soap=http://schemas.xmlsoap.org/wsdl/soap/",
          "xs=http://www.w3.org/2001/XMLSchema",
          "wsa=http://www.w3.org/2005/08/addressing",
          "tjsr=urn:riv:ehr:accesscontrol:AssertCareEngagementResponder:1",
          "tjsi=urn:riv:ehr:accesscontrol:AssertCareEngagementInitiator:1",
          "tns=urn:riv:ehr:accesscontrol:AssertCareEngagement:1:rivtabp20");

  boolean extractTjansteKontraktAndPath(Path destination, String mountIn) throws IOException, DocumentException {
    lasExcecutionResult = WsdlFoundAndDataExtracted.OK;
    List<File> wsdlFiles = PathHelper.findFilesInDirectory(destination.toFile().getPath(), ".*\\.wsdl");
    if (wsdlFiles.size() == 1) {
      wsdlPath = subtractDirectoryFromPath(mountIn, wsdlFiles.get(0));
      List contracts = getConTract.selectNodes(XmlHelper.openDocument(wsdlFiles.get(0).getPath()));
      if(contracts.size()==1){
        serviceContract = ((Node)contracts.get(0)).getStringValue();
      }else{
        lasExcecutionResult = WsdlFoundAndDataExtracted.WRONG_NUMBER_OF_CONTRACTS;
      }
    } else {
      lasExcecutionResult = WsdlFoundAndDataExtracted.WRONG_NUMBER_OF_WSDL;
    }
    return lasExcecutionResult==WsdlFoundAndDataExtracted.OK;
  }
}
