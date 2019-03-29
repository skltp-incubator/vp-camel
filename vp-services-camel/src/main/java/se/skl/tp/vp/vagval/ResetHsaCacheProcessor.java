package se.skl.tp.vp.vagval;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import se.skl.tp.hsa.cache.HsaCache;
import se.skl.tp.hsa.cache.HsaCacheInitializationException;

import java.util.Arrays;

import static se.skl.tp.vp.constants.PropertyConstants.HSA_FILES;

@Component
public class ResetHsaCacheProcessor implements Processor {
    private Logger log = LoggerFactory.getLogger(ResetHsaCacheProcessor.class);

    private final HsaCache hsaCache;
    private String[] hsaFiles;

    @Autowired
    public ResetHsaCacheProcessor(Environment env, HsaCache hsaCache) {
        this.hsaFiles = env.getProperty(HSA_FILES).split(",");
        this.hsaCache = hsaCache;
    }

    private String resetCache() {
        String result = String.format("Start a reset of HSA cache using files: %s\n", Arrays.toString(hsaFiles));
        log.info(result);
        String logData;
        try {
            int oldCacheSize = hsaCache.getHSACacheSize();
            HsaCache cache = hsaCache.init(hsaFiles);
            int cacheSize = cache.getHSACacheSize();
            if (cacheSize > 1) {
                logData = String.format("Successfully reset HSA cache. \nHSA cache size was: %d \nHSA cache now is: %d.", oldCacheSize, cacheSize);
                log.info(logData);
                result+= logData;
            } else {
                logData = String.format("Warning: HSA cache reset to %d. Was %d entries!", cacheSize, oldCacheSize);
                log.warn(logData);
                result+=logData;
            }
        } catch (HsaCacheInitializationException e) {
            logData = "Reset HSA cache failed.";
            log.error(logData, e);
            result += logData + e.toString();
        }
        return result;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String result = resetCache();
        exchange.getOut().setBody(result);
    }
}

