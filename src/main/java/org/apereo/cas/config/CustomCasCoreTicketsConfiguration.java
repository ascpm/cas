package org.apereo.cas.config;

import lombok.extern.log4j.Log4j2;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CustomConfigurationProperties;
import org.apereo.cas.configuration.model.core.ticket.registry.TicketRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.registry.CachingTicketRegistry;
import org.apereo.cas.ticket.registry.CustomDefaultTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by anbug on 2018. 1. 8..
 */
@Configuration("CustomCasCoreTicketsConfiguration")
@EnableConfigurationProperties(value = {CasConfigurationProperties.class, CustomConfigurationProperties.class})
@AutoConfigureBefore(CasCoreTicketsConfiguration.class)
@Log4j2
public class CustomCasCoreTicketsConfiguration {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private CustomConfigurationProperties customConfigurationProperties;

    @Bean(name = "ticketRegistry")
    public TicketRegistry ticketRegistry() {
        log.warn("Runtime memory is used as the persistence storage for retrieving and managing tickets. "
                + "Tickets that are issued during runtime will be LOST upon container restarts. This MAY impact SSO functionality.");
        final TicketRegistryProperties.InMemory mem = casProperties.getTicket().getRegistry().getInMemory();
        final CipherExecutor cipher = Beans.newTicketRegistryCipherExecutor(mem.getCrypto(), "inMemory");
        final LogoutManager logoutManager = applicationContext.getBean("logoutManager", LogoutManager.class);

        if (mem.isCache()) {
            return new CachingTicketRegistry(cipher, logoutManager);
        }

        return new CustomDefaultTicketRegistry(mem.getInitialCapacity(), mem.getLoadFactor(), mem.getConcurrency(), cipher,
                logoutManager, this.customConfigurationProperties.getSession().isSingle());
    }
}
