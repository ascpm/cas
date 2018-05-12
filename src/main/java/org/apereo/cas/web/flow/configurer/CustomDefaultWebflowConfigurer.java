package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.exceptions.*;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;

/**
 * Created by anbug on 2017. 12. 4..
 */
public class CustomDefaultWebflowConfigurer extends DefaultWebflowConfigurer {
    /**
     * Instantiates a new Default webflow configurer.
     *
     * @param flowBuilderServices    the flow builder services
     * @param flowDefinitionRegistry the flow definition registry
     * @param applicationContext     the application context
     * @param casProperties          the cas properties
     */
    public CustomDefaultWebflowConfigurer(FlowBuilderServices flowBuilderServices, FlowDefinitionRegistry flowDefinitionRegistry, ApplicationContext applicationContext, org.apereo.cas.configuration.CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    /**
     * Create handle authentication failure action.
     *
     * @param flow the flow
     */
    @Override
    protected void createHandleAuthenticationFailureAction(final Flow flow) {
        final ActionState handler = createActionState(flow, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_AUTHENTICATION_EXCEPTION_HANDLER));
        createTransitionForState(handler, AccountDisabledException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_ACCOUNT_DISABLED);
        createTransitionForState(handler, AccountLockedException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_ACCOUNT_LOCKED);
        createTransitionForState(handler, AccountPasswordMustChangeException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
        createTransitionForState(handler, CredentialExpiredException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_EXPIRED_PASSWORD);
        createTransitionForState(handler, InvalidLoginLocationException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_INVALID_WORKSTATION);
        createTransitionForState(handler, InvalidLoginTimeException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_INVALID_AUTHENTICATION_HOURS);
        createTransitionForState(handler, FailedLoginException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, AccountNotFoundException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, UnauthorizedServiceForPrincipalException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, PrincipalException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, UnsatisfiedAuthenticationPolicyException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, UnauthorizedAuthenticationException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_AUTHENTICATION_BLOCKED);
        // 비밀번호를 잘못 입력하셨습니다.
        createTransitionForState(handler, CustomFailedLoginByPasswordFailException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
        createStateDefaultTransition(handler, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
    }
}
