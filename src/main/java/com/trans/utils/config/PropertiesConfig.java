package com.trans.utils.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"classpath:spring/applicationContext*.xml"})
public class PropertiesConfig {
}
