package se.skl.tp.vp;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"se.skltp.takcache", "se.skl.tp.hsa.cache", "se.skl.tp.behorighet", "se.skl.tp.vagval", "se.skl.tp.vp"})
public class BeansConfiguration {

}
