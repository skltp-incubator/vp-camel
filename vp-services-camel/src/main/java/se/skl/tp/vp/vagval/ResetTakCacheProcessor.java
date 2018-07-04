package se.skl.tp.vp.vagval;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skltp.tak.vagval.wsdl.v2.ResetVagvalCacheResponse;
import se.skltp.takcache.TakCache;
import se.skltp.takcache.TakCacheLog;

import java.util.Arrays;

@Component
public class ResetTakCacheProcessor implements Processor {
    private Logger log = LoggerFactory.getLogger(ResetTakCacheProcessor.class);

    private final TakCache takCache;

    @Autowired
    public ResetTakCacheProcessor(TakCache takCache) {
        this.takCache = takCache;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        log.info("Start a reset of TAKcache.");
        TakCacheLog result = takCache.refresh();
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
