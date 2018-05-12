package org.apereo.cas.web.support;

import org.springframework.webflow.execution.RequestContext;

/**
 * Created by anbug on 2018. 1. 4..
 */
public final class CustomWebUtils {
    public static void putLoginRedirectUrl(final RequestContext context, final String service) {
        context.getFlowScope().put("loginRedirectUrl", service);
    }
}
