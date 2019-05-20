package se.skl.tp.vp.status;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Route;
import org.apache.camel.ServiceStatus;
import org.apache.camel.impl.EventDrivenConsumerRoute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.service.TakCacheService;
import se.skl.tp.vp.service.TakCacheServiceImpl;

@Service
public class GetStatusProcessor  implements Processor {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    TakCacheService takService;

    @Override
    public void process(Exchange exchange) {

        Map<String, Object> map = registerInfo();
        JSONObject obj = new JSONObject(map);
        try {
            exchange.getIn().setBody(obj.toString(2).replace("\\/","/"));
        } catch (JSONException e) {
            exchange.getIn().setBody(obj.toString());
        }
        exchange.getIn().getHeaders().put(HttpHeaders.HEADER_CONTENT_TYPE, "application/json");
    }

    private Map<String, Object> registerInfo() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        ServiceStatus serviceStatus = camelContext.getStatus();
        map.put("ServiceStatus", "" + serviceStatus);
        map.put("Uptime", camelContext.getUptime());
        map.put("ManagementName", camelContext.getManagementName());
        map.put("JavaVersion", (String) System.getProperties().get("java.version"));
        map.put("CamelVersion", camelContext.getVersion());
        map.put("TakserviceInitialized", "" + takService.isInitalized());
        Date d = TakCacheServiceImpl.getLatestResetDate();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String dateText = format.format(d);
        map.put("LatestTAKcacheReset", dateText);
        int mb = 1024 * 1024;
        Runtime instance = Runtime.getRuntime();
        map.put("JvmTotalMemory", "" + instance.totalMemory() / mb + " mB");
        map.put("JvmFreeMemory", "" + instance.freeMemory() / mb + " mB");
        map.put("JvmUsedMemory", "" + (instance.totalMemory() - instance.freeMemory()) / mb + " mB");
        map.put("JvmMaxMemory", "" + instance.maxMemory() / mb + " mB");
        map.put("Routes", getRoutesInfo());
        return map;
    }

    private HashMap getRoutesInfo(){
        HashMap<String, Object> map = new HashMap<>();
        List<Route> routes = camelContext.getRoutes();
        for(Route route : routes){
            List<String> routeInfos = new ArrayList<>();
            routeInfos.add(route.getEndpoint().getEndpointKey());
            routeInfos.add(((EventDrivenConsumerRoute)route).getStatus().toString());
            map.put( route.getId(), routeInfos);
        }
        return map;
    }

}
