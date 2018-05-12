package org.apereo.cas.configuration;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * Created by anbug on 2017. 12. 6..
 */
@ConfigurationProperties(value = "custom")
@Getter
@Setter
public class CustomConfigurationProperties implements Serializable {
    @NestedConfigurationProperty
    private CustomSessionConfigurationProperties session = new CustomSessionConfigurationProperties();

    @Getter
    @Setter
    public class CustomSessionConfigurationProperties implements Serializable {
        @RequiredProperty
        private boolean single;
    }
}
