package org.apereo.cas.adaptors.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.CustomFailedLoginByPasswordFailException;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by anbug on 2017. 12. 5..
 */
public class CustomQueryDatabaseAuthenticationHandler extends QueryDatabaseAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomQueryDatabaseAuthenticationHandler.class);

    private final String sql;
    private final String fieldPassword;
    private final String fieldExpired;
    private final String fieldDisabled;
    private final Map<String, Collection<String>> principalAttributeMap;

    public CustomQueryDatabaseAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                                    final PrincipalFactory principalFactory,
                                                    final Integer order, final DataSource dataSource, final String sql,
                                                    final String fieldPassword, final String fieldExpired, final String fieldDisabled,
                                                    final Map<String, Collection<String>> attributes) {
        super(name, servicesManager, principalFactory, order, dataSource, sql, fieldPassword, fieldExpired, fieldDisabled, attributes);
        this.sql = sql;
        this.fieldPassword = fieldPassword;
        this.fieldExpired = fieldExpired;
        this.fieldDisabled = fieldDisabled;
        this.principalAttributeMap = attributes;
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential, final String originalPassword)
        throws GeneralSecurityException, PreventedException {

        if (StringUtils.isBlank(this.sql) || getJdbcTemplate() == null) {
            throw new GeneralSecurityException("Authentication handler is not configured correctly. "
                + "No SQL statement or JDBC template is found.");
        }

        final Map<String, Object> attributes = new LinkedHashMap<>(this.principalAttributeMap.size());
        final String username = credential.getUsername();
        final String password = credential.getPassword();

        try {
            final Map<String, Object> dbFields = getJdbcTemplate().queryForMap(this.sql, username);
            final String dbPassword = String.class.cast(dbFields.get(this.fieldPassword));

            if (StringUtils.isNotBlank(originalPassword) && !matches(originalPassword, dbPassword)
                || StringUtils.isBlank(originalPassword) && !StringUtils.equals(password, dbPassword)) {
                LOGGER.debug("비밀번호를 잘못 입력하셨습니다.");
                throw new CustomFailedLoginByPasswordFailException();
            }

            this.principalAttributeMap.forEach((key, attributeNames) -> {
                final Object attribute = dbFields.get(key);

                if (attribute != null) {
                    LOGGER.debug("Found attribute [{}] from the query results", key);
                    attributeNames.forEach(s -> {
                        LOGGER.debug("Principal attribute [{}] is virtually remapped/renamed to [{}]", key, s);
                        attributes.put(s, CollectionUtils.wrap(attribute.toString()));
                    });
                } else {
                    LOGGER.warn("Requested attribute [{}] could not be found in the query results", key);
                }

            });
        } catch (final IncorrectResultSizeDataAccessException e) {
            if (e.getActualSize() == 0) {
                throw new AccountNotFoundException(username + " not found with SQL query");
            }
            throw new FailedLoginException("Multiple records found for " + username);
        } catch (final DataAccessException e) {
            throw new PreventedException("SQL exception while executing query for " + username, e);
        }
        return createHandlerResult(credential, this.principalFactory.createPrincipal(username, attributes), null);
    }
}
