package se.skl.tp.vp.vagval;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skl.tp.vp.service.TakCacheService;
import se.skltp.takcache.TakCacheLog;

import java.util.Date;
import java.util.Map;

@Component
public class ResetTakCacheProcessor implements Processor {
    private Logger log = LoggerFactory.getLogger(ResetTakCacheProcessor.class);

    private final TakCacheService takService;

    @Autowired
    public ResetTakCacheProcessor(TakCacheService takService) {
        this.takService = takService;
    }

    /*@Autowired
    private CamelContext camelContext;*/

    @Override
    public void process(Exchange exchange) throws Exception {
       /* Map map = camelContext.getGlobalOptions();
        map.put("TAK_CACHE_RESET", new Date().toString());*/
        TakCacheLog result = takService.refresh();
        exchange.getOut().setBody(getResultAsString(result));
        exchange.getOut().setHeader("Content-Type", "text/html;");
        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
    }

    private String getResultAsString(TakCacheLog result) {
        StringBuilder resultAsString = new StringBuilder();
        for (String processingLog : result.getLog()) {
            resultAsString.append("<br>").append(processingLog);
        }
        return resultAsString.toString();
    }
}
