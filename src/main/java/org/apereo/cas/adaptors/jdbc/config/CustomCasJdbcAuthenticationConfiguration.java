package org.apereo.cas.adaptors.jdbc.config;

import com.google.common.collect.Multimap;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.jdbc.BindModeSearchDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.CustomQueryDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.QueryAndEncodeDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.SearchModeSearchDatabaseAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jdbc.*;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by anbug on 2017. 12. 5..
 */
@Configuration("CustomCasJdbcAuthenticationConfiguration")
@EnableConfigurationProperties({CasConfigurationProperties.class})
@AutoConfigureBefore(CasSupportActionsConfiguration.class)
@Log4j2
@SuppressWarnings("SpringJavaAutowiringInspection")
public class CustomCasJdbcAuthenticationConfiguration extends CasJdbcAuthenticationConfiguration {
    @Autowired(required = false)
    @Qualifier("queryAndEncodePasswordPolicyConfiguration")
    private PasswordPolicyConfiguration queryAndEncodePasswordPolicyConfiguration;

    @Autowired(required = false)
    @Qualifier("searchModePasswordPolicyConfiguration")
    private PasswordPolicyConfiguration searchModePasswordPolicyConfiguration;

    @Autowired(required = false)
    @Qualifier("queryPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration queryPasswordPolicyConfiguration;

    @Autowired(required = false)
    @Qualifier("bindSearchPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration bindSearchPasswordPolicyConfiguration;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean(name = "jdbcAuthenticationHandlers")
    @RefreshScope
    @Override
    public Collection<AuthenticationHandler> jdbcAuthenticationHandlers() {
        final Collection<AuthenticationHandler> handlers = new HashSet<>();
        final JdbcAuthenticationProperties jdbc = casProperties.getAuthn().getJdbc();
        jdbc.getBind().forEach(b -> handlers.add(bindModeSearchDatabaseAuthenticationHandler(b)));
        jdbc.getEncode().forEach(b -> handlers.add(queryAndEncodeDatabaseAuthenticationHandler(b)));
        jdbc.getQuery().forEach(b -> handlers.add(queryDatabaseAuthenticationHandler(b)));
        jdbc.getSearch().forEach(b -> handlers.add(searchModeSearchDatabaseAuthenticationHandler(b)));
        return handlers;
    }

    private AuthenticationHandler bindModeSearchDatabaseAuthenticationHandler(final BindJdbcAuthenticationProperties b) {
        final BindModeSearchDatabaseAuthenticationHandler h = new BindModeSearchDatabaseAuthenticationHandler(b.getName(), servicesManager,
            jdbcPrincipalFactory(), b.getOrder(), JpaBeans.newDataSource(b));
        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(b.getPasswordEncoder()));
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(b.getPrincipalTransformation()));

        if (bindSearchPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(bindSearchPasswordPolicyConfiguration);
        }

        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(b.getPrincipalTransformation()));

        if (StringUtils.isNotBlank(b.getCredentialCriteria())) {
            h.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(b.getCredentialCriteria()));
        }

        log.debug("Created authentication handler [{}] to handle database url at [{}]", h.getName(), b.getUrl());
        return h;
    }

    private AuthenticationHandler queryAndEncodeDatabaseAuthenticationHandler(final QueryEncodeJdbcAuthenticationProperties b) {
        final QueryAndEncodeDatabaseAuthenticationHandler h = new QueryAndEncodeDatabaseAuthenticationHandler(b.getName(), servicesManager,
            jdbcPrincipalFactory(), b.getOrder(), JpaBeans.newDataSource(b), b.getAlgorithmName(), b.getSql(), b.getPasswordFieldName(),
            b.getSaltFieldName(), b.getExpiredFieldName(), b.getDisabledFieldName(), b.getNumberOfIterationsFieldName(), b.getNumberOfIterations(),
            b.getStaticSalt());

        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(b.getPasswordEncoder()));
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(b.getPrincipalTransformation()));

        if (queryAndEncodePasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(queryAndEncodePasswordPolicyConfiguration);
        }

        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(b.getPrincipalTransformation()));

        if (StringUtils.isNotBlank(b.getCredentialCriteria())) {
            h.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(b.getCredentialCriteria()));
        }

        log.debug("Created authentication handler [{}] to handle database url at [{}]", h.getName(), b.getUrl());
        return h;
    }

    private AuthenticationHandler queryDatabaseAuthenticationHandler(final QueryJdbcAuthenticationProperties b) {
        final Multimap<String, String> attributes = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(b.getPrincipalAttributeList());
        log.debug("Created and mapped principal attributes [{}] for [{}]...", attributes, b.getUrl());

        final CustomQueryDatabaseAuthenticationHandler h = new CustomQueryDatabaseAuthenticationHandler(b.getName(), servicesManager,
            jdbcPrincipalFactory(), b.getOrder(),
            JpaBeans.newDataSource(b), b.getSql(), b.getFieldPassword(),
            b.getFieldExpired(), b.getFieldDisabled(),
            CollectionUtils.wrap(attributes));

        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(b.getPasswordEncoder()));
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(b.getPrincipalTransformation()));

        if (queryPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(queryPasswordPolicyConfiguration);
        }

        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(b.getPrincipalTransformation()));

        if (StringUtils.isNotBlank(b.getCredentialCriteria())) {
            h.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(b.getCredentialCriteria()));
        }

        log.debug("Created authentication handler [{}] to handle database url at [{}]", h.getName(), b.getUrl());
        return h;
    }

    private AuthenticationHandler searchModeSearchDatabaseAuthenticationHandler(final SearchJdbcAuthenticationProperties b) {
        final SearchModeSearchDatabaseAuthenticationHandler h = new SearchModeSearchDatabaseAuthenticationHandler(b.getName(), servicesManager,
            jdbcPrincipalFactory(), b.getOrder(), JpaBeans.newDataSource(b), b.getFieldUser(), b.getFieldPassword(), b.getTableUsers());

        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(b.getPasswordEncoder()));
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(b.getPrincipalTransformation()));
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(b.getPrincipalTransformation()));

        if (searchModePasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(searchModePasswordPolicyConfiguration);
        }

        if (StringUtils.isNotBlank(b.getCredentialCriteria())) {
            h.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(b.getCredentialCriteria()));
        }

        log.debug("Created authentication handler [{}] to handle database url at [{}]", h.getName(), b.getUrl());
        return h;
    }
}
