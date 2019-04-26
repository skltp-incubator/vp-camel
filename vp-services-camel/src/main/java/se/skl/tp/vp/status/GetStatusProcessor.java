package se.skl.tp.vp.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.camel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.errorhandling.ExceptionUtil;
import se.skl.tp.vp.service.TakCacheService;

import java.util.HashMap;
import java.util.List;


@Service
public class GetStatusProcessor  implements Processor {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    TakCacheService takService;

    @JsonProperty("statusMap")
    private HashMap<String, String> statusMap;

    @Override
    public void process(Exchange exchange) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map = registerInfo(map);
        JSONObject obj = new JSONObject(map);
        exchange.getIn().setBody(obj.toString());
    }

    private HashMap registerInfo(HashMap<String, String> map) {
        map.clear();
        ServiceStatus serviceStatus = camelContext.getStatus();
        map.put("serviceStatus", "" + serviceStatus);
        String uptime = camelContext.getUptime();
        map.put("uptime", uptime);
        String managementName = camelContext.getManagementName();
        map.put("managementName", managementName);
        if (!serviceStatus.equals(ServiceStatus.Started)) {
            return map;
        }
        boolean takserviceInitialized = takService.isInitalized();
        map.put("takserviceInitialized", "" + takserviceInitialized);
        int mb = 1024 * 1024;
        Runtime instance = Runtime.getRuntime();
        map.put("JvmTotalMemory", "" + instance.totalMemory() / mb + " mB");
        map.put("JvmFreeMemory", "" + instance.freeMemory() / mb + " mB");
        map.put("JvmUsedMemory", "" + (instance.totalMemory() - instance.freeMemory()) / mb + " mB");
        map.put("JvmMaxMenory", "" + instance.maxMemory() / mb + " mB");

        return map;
    }

}
