package org.apereo.cas.authentication.exceptions;

import javax.security.auth.login.FailedLoginException;

/**
 * Created by anbug on 2017. 12. 4..
 */
public class CustomFailedLoginByPasswordFailException extends FailedLoginException {
    public CustomFailedLoginByPasswordFailException() {
        super();
    }

    public CustomFailedLoginByPasswordFailException(String msg) {
        super(msg);
    }
}
