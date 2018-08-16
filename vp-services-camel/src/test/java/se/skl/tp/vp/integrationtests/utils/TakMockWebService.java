package se.skl.tp.vp.integrationtests.utils;

import java.net.URL;
import java.util.List;
import javax.xml.ws.Endpoint;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.behorighet.BehorighetPersistentHandler;
import se.skltp.takcache.vagval.VagvalPersistentHandler;


public class TakMockWebService {


  private Endpoint endpoint;
  private String url;
  SokVagvalsServiceSoap11LitDoc sokVagvalsInfo;

  public TakMockWebService(String url) {
    sokVagvalsInfo = new SokVagvalsServiceSoap11LitDoc();
    this.url = url;
    setBehorigheterFromXmlResource("takdata/tak-behorigheter-test.xml");
    setVagvalFromXmlResource("takdata/tak-vagval-test.xml");
  }

  public void setBehorigheterFromXmlResource(String resourceName){
    URL url = TakMockWebService.class.getClassLoader().getResource(resourceName);
    setAnropsBehorigheterResult( BehorighetPersistentHandler.restoreFromLocalCache(url.getFile()));
  }

  public void setVagvalFromXmlResource(String resourceName){
    URL url = TakMockWebService.class.getClassLoader().getResource(resourceName);
    List<VirtualiseringsInfoType> list = VagvalPersistentHandler.restoreFromLocalCache(url.getFile());
    setVirtualiseringarResult(list);
  }

  public void start() {
    endpoint = Endpoint.publish(url, sokVagvalsInfo);
  }

  public void stop() {
    if (endpoint != null) {
      endpoint.stop();
    }
  }

  public void setVirtualiseringarResult(List<VirtualiseringsInfoType> virtualiseringar) {
    sokVagvalsInfo.hamtaAllaVirtualiseringar(null).getVirtualiseringsInfo().clear();
    sokVagvalsInfo.hamtaAllaVirtualiseringar(null).getVirtualiseringsInfo()
        .addAll(virtualiseringar);
  }

  public void setAnropsBehorigheterResult(List<AnropsBehorighetsInfoType> anropsBehorigheter) {
    sokVagvalsInfo.hamtaAllaAnropsBehorigheter(null).getAnropsBehorighetsInfo().clear();
    sokVagvalsInfo.hamtaAllaAnropsBehorigheter(null).getAnropsBehorighetsInfo()
        .addAll(anropsBehorigheter);
  }

}
