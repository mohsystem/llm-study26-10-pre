package com.um.springbootprojstructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.repository.config.EnableLdapRepositories;

@Configuration
@EnableLdapRepositories
public class LdapConfig {
    // Boot auto-configures ContextSource + LdapTemplate using spring.ldap.* properties.
}