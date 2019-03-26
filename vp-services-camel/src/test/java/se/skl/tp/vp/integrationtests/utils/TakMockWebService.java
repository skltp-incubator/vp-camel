package se.skl.tp.vp.integrationtests.utils;

import java.io.File;
import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.ws.Endpoint;
import lombok.extern.slf4j.Slf4j;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;

@Slf4j
public class TakMockWebService {


  private Endpoint endpoint;
  private String url;
  SokVagvalsServiceSoap11LitDoc sokVagvalsInfo;

  @XmlRootElement(name="persistentCache")
  public static class VirtuliseringCache {
    @XmlElement
    protected List<VirtualiseringsInfoType> virtualiseringsInfo;
  }

  @XmlRootElement(name="persistentCache")
  public static class BehorighetCache {
    @XmlElement
    protected List<AnropsBehorighetsInfoType> anropsBehorighetsInfo;
  }

  public TakMockWebService(String url) {
    sokVagvalsInfo = new SokVagvalsServiceSoap11LitDoc();
    this.url = url;
    setBehorigheterFromXmlResource("takdata/tak-behorigheter-test.xml");
    setVagvalFromXmlResource("takdata/tak-vagval-test.xml");
  }

  public void setBehorigheterFromXmlResource(String resourceName) {
    URL url = TakMockWebService.class.getClassLoader().getResource(resourceName);
    setAnropsBehorigheterResult(restoreFromLocalCache(url.getFile(), BehorighetCache.class));
  }

  public void setVagvalFromXmlResource(String resourceName) {
    URL url = TakMockWebService.class.getClassLoader().getResource(resourceName);
    setVirtualiseringarResult(restoreFromLocalCache(url.getFile(), VirtuliseringCache.class));
  }

  public void start() {
    endpoint = Endpoint.publish(url, sokVagvalsInfo);
  }

  public void stop() {
    if (endpoint != null) {
      endpoint.stop();
    }
  }

  public void setVirtualiseringarResult(VirtuliseringCache virtualiseringar) {
    sokVagvalsInfo.hamtaAllaVirtualiseringar(null).getVirtualiseringsInfo().clear();
    sokVagvalsInfo.hamtaAllaVirtualiseringar(null).getVirtualiseringsInfo()
        .addAll(virtualiseringar.virtualiseringsInfo);
  }

  public void setAnropsBehorigheterResult(BehorighetCache anropsBehorigheter) {
    sokVagvalsInfo.hamtaAllaAnropsBehorigheter(null).getAnropsBehorighetsInfo().clear();
    sokVagvalsInfo.hamtaAllaAnropsBehorigheter(null).getAnropsBehorighetsInfo()
        .addAll(anropsBehorigheter.anropsBehorighetsInfo);
  }


  public static <T> T restoreFromLocalCache(String fileName, Class<T> className) {
    Unmarshaller jaxbUnmarshaller = null;
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(className);
      jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (T) jaxbUnmarshaller.unmarshal(new File(fileName));
    } catch (JAXBException e) {
//      log.error;
      return null;
    }
  }

}
