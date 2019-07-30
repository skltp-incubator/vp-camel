package se.skl.tp.vp.timeout;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.PropertyConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TimeoutConfigurationJson implements TimeoutConfiguration {

  private static final Logger LOGGER = LogManager.getLogger(TimeoutConfigurationJson.class);

  private List<TimeoutConfig> wsdlConfigs;
  private HashMap<String, TimeoutConfig> mapOnTjanstekontrakt;

  public void setMapOnTjansteKontrakt(HashMap<String, TimeoutConfig> map) {
    // Used in tests
    mapOnTjanstekontrakt = map;
  }

  public TimeoutConfigurationJson(
      @Value("${" + PropertyConstants.TIMEOUT_JSON_FILE + "}") String timeoutJsonFile,
      @Value("${" + PropertyConstants.TIMEOUT_JSON_FILE_DEFAULT_TJANSTEKONTRAKT_NAME + "}")
          String timeoutJsonFileDefaultTjanstekontraktName)
      throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      wsdlConfigs = objectMapper.readValue(new File(timeoutJsonFile), new TypeReference<List<TimeoutConfig>>() {});
    } catch (FileNotFoundException e) {
      LOGGER.warn("Json file for timeouts not found at " + timeoutJsonFile + ".");
    } catch (JsonParseException e) {
      LOGGER.warn("Json file for timeouts " + timeoutJsonFile + " could not be parsed.");
    }
    if (wsdlConfigs == null) {
      wsdlConfigs = new ArrayList<>();
    }
    boolean defaultTimeoutsExist = false;
    for (TimeoutConfig timeoutConfig : wsdlConfigs) {
      if (timeoutConfig.getTjanstekontrakt().equalsIgnoreCase(timeoutJsonFileDefaultTjanstekontraktName)) {
        defaultTimeoutsExist = true;
      }
    }
    if (!defaultTimeoutsExist) {
      createDefaultTimeoutsWhenMissing(timeoutJsonFileDefaultTjanstekontraktName);
      LOGGER.warn("Could not find any default timeoutvalues, using producertimeout=29000 and routetimeout=30000 as default timeouts. " +
              "Please create and configure a timeoutconfig.json file to set this manually.");
    }

    initMaps();
  }

  private void createDefaultTimeoutsWhenMissing(String defaultTjanstekontrakt) {
    TimeoutConfig timeoutConfig = new TimeoutConfig();
    timeoutConfig.setTjanstekontrakt(defaultTjanstekontrakt);
    timeoutConfig.setProducertimeout(29000);
    timeoutConfig.setRoutetimeout(30000);
    wsdlConfigs.add(timeoutConfig);
  }

  private void initMaps() {
    mapOnTjanstekontrakt = new HashMap<>();
    for (TimeoutConfig timeoutConfig : wsdlConfigs) {
      mapOnTjanstekontrakt.put(timeoutConfig.getTjanstekontrakt(), timeoutConfig);
    }
  }

  @Override
  public TimeoutConfig getOnTjanstekontrakt(String tjanstekontrakt) {
    return mapOnTjanstekontrakt.get(tjanstekontrakt);
  }
}
