package se.skl.tp.vp.httpheader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.regex.Pattern;

@Configuration
public class IPWhitelistConfig {

    @Autowired
    Environment env;

    @Bean
    public IPWhitelistHandler ipWhitelistHandler() {
        return new IPWhitelistHandlerImpl(env.getProperty("ip.whitelist"));
    }
}
