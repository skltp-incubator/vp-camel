package se.skl.tp.vp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import se.skl.tp.DefaultRoutingConfiguration;
import se.skl.tp.vp.config.DefaultRoutingProperties;

@Configuration
@ComponentScan(basePackages = {"se.skltp.takcache", "se.skl.tp.hsa.cache", "se.skl.tp.behorighet", "se.skl.tp.vagval", "se.skl.tp.vp"})
public class BeansConfiguration {

}
