package se.skl.tp.vp.status;

import org.apache.camel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.service.TakCacheService;

import java.util.HashMap;

@Service
public class GetStatusProcessor  implements Processor {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    TakCacheService takService;

    @Override
    public void process(Exchange exchange) {
        HashMap<String, String> map = registerInfo();
        JSONObject obj = new JSONObject(map);
        exchange.getIn().setBody(obj.toString());
        exchange.getIn().getHeaders().put(HttpHeaders.HEADER_CONTENT_TYPE, "application/json");
    }

    private HashMap registerInfo() {
        HashMap<String, String> map = new HashMap<>();
        ServiceStatus serviceStatus = camelContext.getStatus();
        map.put("ServiceStatus", "" + serviceStatus);
        map.put("Uptime", camelContext.getUptime());
        map.put("ManagementName", camelContext.getManagementName());
        map.put("JavaVersion", (String) System.getProperties().get("java.version"));
        map.put("CamelVersion", camelContext.getVersion());
        map.put("TakserviceInitialized", "" + takService.isInitalized());
        int mb = 1024 * 1024;
        Runtime instance = Runtime.getRuntime();
        map.put("JvmTotalMemory", "" + instance.totalMemory() / mb + " mB");
        map.put("JvmFreeMemory", "" + instance.freeMemory() / mb + " mB");
        map.put("JvmUsedMemory", "" + (instance.totalMemory() - instance.freeMemory()) / mb + " mB");
        map.put("JvmMaxMenory", "" + instance.maxMemory() / mb + " mB");

        return map;
    }

}
