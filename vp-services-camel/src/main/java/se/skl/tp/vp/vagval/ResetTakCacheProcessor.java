package se.skl.tp.vp.vagval;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skltp.takcache.TakCache;
import se.skltp.takcache.TakCacheLog;

@Component
public class ResetTakCacheProcessor implements Processor {
    private Logger log = LoggerFactory.getLogger(ResetHsaCacheProcessor.class);

    @Autowired
    private TakCache takCache;


    @Override
    public void process(Exchange exchange) throws Exception {
        TakCacheLog result = takCache.refresh();
        exchange.getOut().setBody(result.isRefreshSuccessful());
    }
}
