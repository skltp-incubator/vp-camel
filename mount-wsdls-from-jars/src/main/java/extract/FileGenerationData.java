package extract;

import static extract.Constants.FILE_COLON_PREFIX;

import extract.UnpackedWdslFinder.WsdlFoundAndDataExtracted;
import extract.WsdlUrlExtractor.WsdlurlExtraction;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class FileGenerationData {


  private Map<File, WsdlConfigHolder> wsdlConfigurationsMap = new HashMap<>();

  void setTjanstekontrakt(File key, String tjanstekontrakt) {
    if (wsdlConfigurationsMap.containsKey(key)) {
      wsdlConfigurationsMap.get(key).setTjanstekontrakt(tjanstekontrakt);
    } else {
      WsdlConfigHolder holder = new WsdlConfigHolder(key);
      holder.setTjanstekontrakt(tjanstekontrakt);
      wsdlConfigurationsMap.put(key, holder);
    }
  }

  void setWsdlfilepath(File key, String wsdlfilePath) {
    if (wsdlConfigurationsMap.containsKey(key)) {
      wsdlConfigurationsMap.get(key).setWsdlfilepath(wsdlfilePath);
    } else {
      WsdlConfigHolder holder = new WsdlConfigHolder(key);
      holder.setWsdlfilepath(wsdlfilePath);
      wsdlConfigurationsMap.put(key, holder);
    }
  }

  void setWsdlurl(File key, String pWsdlurl) {
    if (wsdlConfigurationsMap.containsKey(key)) {
      wsdlConfigurationsMap.get(key).setWsdlurl(pWsdlurl);
    } else {
      WsdlConfigHolder holder = new WsdlConfigHolder(key);
      holder.setWsdlurl(pWsdlurl);
      wsdlConfigurationsMap.put(key, holder);
    }
  }

  Collection<WsdlConfigHolder> getWsdlConfigurationSource() {
    return wsdlConfigurationsMap.values();
  }

  private void addProblems(File key, Object... problems) {
    if (wsdlConfigurationsMap.containsKey(key)) {
      wsdlConfigurationsMap.get(key).addProblemLine(problems);
    } else {
      WsdlConfigHolder holder = new WsdlConfigHolder(key);
      holder.addProblemLine(problems);
      wsdlConfigurationsMap.put(key, holder);
    }
  }

  void addProblemUnexpectedException(File file, Exception e) {
    addProblems(file, "Unexpected exception");
    addProblems(file, FILE_COLON_PREFIX, file.getName(), " Exception: ", e.getMessage());
  }

  void addProblemWithWsdlurl(WsdlurlExtraction wsdlurlExtraction, File file) {
    if (wsdlurlExtraction == WsdlurlExtraction.COULD_NOT_FIND_WSDL_URL_IN_ERNTRY) {
      addProblems(file, "Unable to extractTjansteKontraktAndPath WsdlUrl");
      addProblems(
          file,
          FILE_COLON_PREFIX,
          file.getName(),
          " has no WsdlUrl in the tp2-service-mule-descriptor.xml ");
    } else {
      addProblems(file, "Found no Mule descriptor");
      addProblems(file, FILE_COLON_PREFIX, file.getName(), " has no tp2-service-mule-descriptor.xml ");
    }
  }

  void AddProblemWithExtractTjansteKontraktAndPath(
      File file, WsdlFoundAndDataExtracted wsdResult) {
    if (wsdResult == WsdlFoundAndDataExtracted.WRONG_NUMBER_OF_CONTRACTS) {
      addProblems(file, "File has wrong number of service contracts in wsdl");
      addProblems(file, FILE_COLON_PREFIX, file.getName(), " contains to many or few service-contracts");
 } else {
      addProblems(file, "File has wrong number of wsdl");
      addProblems(file, FILE_COLON_PREFIX, file.getName(), " contains to many or few wsdl-files");
    }
  }
}
