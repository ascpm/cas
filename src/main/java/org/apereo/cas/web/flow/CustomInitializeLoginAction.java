package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.exceptions.CustomFailedLoginByPasswordFailException;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.CustomWebUtils;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.security.auth.login.AccountNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Optional;

/**
 * Created by anbug on 2018. 1. 4..
 */
public class CustomInitializeLoginAction extends InitializeLoginAction {
    @Autowired
    private MessageSource messageSource;

    public CustomInitializeLoginAction(ServicesManager servicesManager) {
        super(servicesManager);
    }

    @Override
    protected Event doExecute(RequestContext requestContext) throws Exception {
        HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        String serviceLogin = Optional.ofNullable(request.getParameter("serviceLogin")).orElse("");
        String service = Optional.ofNullable(request.getParameter("service")).orElse("");
        String eventId = requestContext.getCurrentEvent().getId();
        boolean auto = Optional.ofNullable(request.getParameter("auto")).map(Boolean::valueOf).orElse(false);

        if (auto) {
            return new Event(this, "auto");
        } else if ((CustomFailedLoginByPasswordFailException.class.getSimpleName().equals(eventId)
            || AccountNotFoundException.class.getSimpleName().equals(eventId))
            && !StringUtils.isEmpty(serviceLogin)) {
            CustomWebUtils.putLoginRedirectUrl(requestContext, String.format("%s?check=true" +
                    "&fail=true" +
                    "&failLoginId=%s" +
                    "&failMessage=%s" +
                    "&service=%s",
                serviceLogin,
                WebUtils.getCredential(requestContext).getId(),
                URLEncoder.encode(this.messageSource.getMessage(String.format("authenticationFailure.%s", eventId), null, Locale.getDefault()), "UTF-8"),
                URLEncoder.encode(service, "UTF-8")));
            return new Event(this, "redirect");
        } else {
            if (StringUtils.isEmpty(serviceLogin)) {
                return success();
            } else {
                CustomWebUtils.putLoginRedirectUrl(requestContext, String.format("%s?check=true" +
                        "&service=%s",
                    serviceLogin,
                    URLEncoder.encode(service, "UTF-8")));
                return new Event(this, "redirect");
            }
        }
    }
}
