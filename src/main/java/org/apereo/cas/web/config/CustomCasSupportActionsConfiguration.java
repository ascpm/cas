package org.apereo.cas.web.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CustomInitializeLoginAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * Created by anbug on 2018. 1. 4..
 */
@Configuration("CustomCasSupportActionsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureBefore(CasSupportActionsConfiguration.class)
public class CustomCasSupportActionsConfiguration {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Bean(name = "initializeLoginAction")
    @RefreshScope
    public Action initializeLoginAction() {
        return new CustomInitializeLoginAction(servicesManager);
    }
}