package se.skl.tp.vp.timeout;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.ApplicationProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TimeoutConfigurationJson implements TimeoutConfiguration {

    private static Logger LOGGER = LogManager.getLogger(TimeoutConfigurationJson.class);

    private List<TimeoutConfig> wsdlConfigs;
    private HashMap<String, TimeoutConfig> mapOnTjanstekontrakt;

    public TimeoutConfigurationJson(Environment env) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            wsdlConfigs = objectMapper.readValue(new File(env.getProperty(ApplicationProperties.TIMEOUT_JSON_FILE)), new TypeReference<List<TimeoutConfig>>(){});
        } catch(FileNotFoundException e) {
            LOGGER.warn("Json file for timeouts not found at "+ env.getProperty(ApplicationProperties.TIMEOUT_JSON_FILE) +".");
        } catch(JsonParseException e) {
            LOGGER.warn("Json file for timeouts "+ env.getProperty(ApplicationProperties.TIMEOUT_JSON_FILE) +" could not be parsed.");
        }
        if(wsdlConfigs == null) {
            wsdlConfigs = new ArrayList<>();
        }
        boolean defaultTimeoutsExist= false;
        for (TimeoutConfig timeoutConfig : wsdlConfigs) {
             if(timeoutConfig.getTjanstekontrakt().equalsIgnoreCase(env.getProperty(ApplicationProperties.TIMEOUT_JSON_FILE_DEFAULT_TJANSTEKONTRAKT_NAME))){
                 defaultTimeoutsExist=true;
             }
        }
        if(!defaultTimeoutsExist){
            createDefaultTimeoutsWhenMissing(env.getProperty(ApplicationProperties.TIMEOUT_JSON_FILE_DEFAULT_TJANSTEKONTRAKT_NAME));
            LOGGER.warn("Could not find any default timeoutvalues, using producertimeout=29000 and routetimeout=30000 as default timeouts. Please create and configure a timeoutconfig.json file to set this manually.");
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
