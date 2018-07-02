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

import static se.skl.tp.vp.constants.ApplicationProperties.HSA_FILES;

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
        log.info("Start a reset of HSA cache using files: {}", Arrays.toString(hsaFiles));
        String result = String.format("Start a reset of HSA cache using files: %s\n", Arrays.toString(hsaFiles));
        try {
            int oldCacheSize = hsaCache.getHSACacheSize();
            HsaCache cache = hsaCache.init(hsaFiles);
            int cacheSize = cache.getHSACacheSize();
            if (cacheSize > 1) {
                log.info("Successfully reset HSA cache. \nHSA cache size was: {} \nHSA cache now is: {}.", oldCacheSize, cacheSize);
                result+=String.format("Successfully reset HSA cache. \nHSA cache size was: %d \nHSA cache now is: %d.", oldCacheSize, cacheSize);
            } else {
                log.warn("Warning: HSA cache reset to {}. Was {} entries!", cacheSize, oldCacheSize);
                result+=String.format("Warning: HSA cache reset to %d. Was %d entries!", cacheSize, oldCacheSize);
            }
        } catch (HsaCacheInitializationException e) {
            log.error("Reset HSA cache failed.", e);
            result += "Reset HSA cache failed." + e.toString();
        }
        return result;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String result = resetCache();
        exchange.getOut().setBody(result);
    }
}

