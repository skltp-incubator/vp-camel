package se.skl.tp.vp.timeout;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.ApplicationProperties;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Service
public class TimeoutConfigurationJson implements TimeoutConfiguration {

    private List<TimeoutConfig> wsdlConfigs;
    private HashMap<String, TimeoutConfig> mapOnTjanstekontrakt;

    public TimeoutConfigurationJson(Environment env) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        wsdlConfigs = objectMapper.readValue(new File(env.getProperty(ApplicationProperties.TIMEOUT_JSON_FILE)), new TypeReference<List<TimeoutConfig>>(){});

        initMaps();
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
