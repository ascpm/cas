package org.apereo.cas.web.flow.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CustomDefaultWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.DefaultWebflowConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by anbug on 2017. 12. 4..
 */
@Configuration("CustomCasWebflowContextConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureBefore(CasWebflowContextConfiguration.class)
public class CustomCasWebflowContextConfiguration extends CasWebflowContextConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean(name = "defaultWebflowConfigurer")
    @Override
    public CasWebflowConfigurer defaultWebflowConfigurer() {
        final DefaultWebflowConfigurer c = new CustomDefaultWebflowConfigurer(builder(), loginFlowRegistry(), applicationContext, casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry());
        c.initialize();
        return c;
    }
}
