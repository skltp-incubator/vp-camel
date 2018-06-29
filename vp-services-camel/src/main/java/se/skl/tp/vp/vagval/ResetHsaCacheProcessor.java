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

    private void resetCache() {
        try {
            log.info("Start a reset of HSA cache using files: {}", Arrays.toString(hsaFiles));
            int oldCacheSize = hsaCache.getHSACacheSize();
            HsaCache cache = hsaCache.init(hsaFiles);
            int cacheSize = cache.getHSACacheSize();
            if (cacheSize > 1) {
                log.info("Successfully reset HSA cache. \nHSA cache size was: {} \nHSA cache now is: {}.", oldCacheSize, cacheSize);
            } else {
                log.warn("Warning: HSA cache reset to {}. Was {} entries!", cacheSize, oldCacheSize);
            }
        } catch (HsaCacheInitializationException e) {
            log.error("Reset HSA cache failed.", e);
        }
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        resetCache();
    }
}

