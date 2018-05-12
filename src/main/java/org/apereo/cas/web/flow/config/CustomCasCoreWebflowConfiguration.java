package org.apereo.cas.web.flow.config;

import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.exceptions.*;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.web.flow.actions.AuthenticationExceptionHandlerAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by anbug on 2017. 12. 4..
 */
@Configuration("CustomCasCoreWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureBefore(CasCoreWebflowConfiguration.class)
public class CustomCasCoreWebflowConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean(name = "authenticationExceptionHandler")
    public Action authenticationExceptionHandler() {
        return new AuthenticationExceptionHandlerAction(customHandledAuthenticationExceptions());
    }

    private Set<Class<? extends Exception>> customHandledAuthenticationExceptions() {
        /*
         * Order is important here; We want the account policy exceptions to be handled
         * first before moving onto more generic errors. In the event that multiple handlers
         * are defined, where one failed due to account policy restriction and one fails
         * due to a bad password, we want the error associated with the account policy
         * to be processed first, rather than presenting a more generic error associated
         */
        final Set<Class<? extends Exception>> errors = new LinkedHashSet<>();
        errors.add(javax.security.auth.login.AccountLockedException.class);
        errors.add(javax.security.auth.login.CredentialExpiredException.class);
        errors.add(javax.security.auth.login.AccountExpiredException.class);
        errors.add(AccountDisabledException.class);
        errors.add(InvalidLoginLocationException.class);
        errors.add(AccountPasswordMustChangeException.class);
        errors.add(InvalidLoginTimeException.class);

        errors.add(javax.security.auth.login.AccountNotFoundException.class);
        errors.add(javax.security.auth.login.FailedLoginException.class);
        errors.add(UnauthorizedServiceForPrincipalException.class);
        errors.add(PrincipalException.class);
        errors.add(UnsatisfiedAuthenticationPolicyException.class);
        errors.add(UnauthorizedAuthenticationException.class);

        errors.add(CustomFailedLoginByPasswordFailException.class);

        errors.addAll(casProperties.getAuthn().getExceptions().getExceptions());

        return errors;
    }
}
